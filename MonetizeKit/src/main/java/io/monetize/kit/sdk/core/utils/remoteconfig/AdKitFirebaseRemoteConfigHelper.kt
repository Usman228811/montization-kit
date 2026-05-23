package io.monetize.kit.sdk.core.utils.remoteconfig


import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.firebase.remoteconfig.remoteConfig
import io.monetize.kit.sdk.core.utils.firebaseLong
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import java.util.concurrent.ConcurrentHashMap

class AdKitFirebaseRemoteConfigHelper private constructor() {

    private var runnableSplash: Runnable? = null
    private var isHandlerRunning = false
    private val handlerAd = Handler(Looper.getMainLooper())

    private val defaultRemoteConfig = RemoteConfigBuilder.getInstance()
    private val cachedConfig = ConcurrentHashMap<String, Any>()


    private fun startHandler() {
        var configFetchTime = firebaseLong("config_fetch_time", 8)
        if (configFetchTime == 0L) {
            configFetchTime = 8L
        }
        if (!isHandlerRunning) {
            isHandlerRunning = true
            runnableSplash?.let {
                handlerAd.postDelayed(it, configFetchTime * 1000)
            }
        }
    }

    private fun removeCallBacks() {
        try {
            isHandlerRunning = false
            runnableSplash?.let {
                handlerAd.removeCallbacks(it)
            }
        } catch (_: Exception) {
        }
    }

    companion object {
        @Volatile
        private var instance: AdKitFirebaseRemoteConfigHelper? = null


        internal fun getInstance(
        ): AdKitFirebaseRemoteConfigHelper {
            return instance ?: synchronized(this) {
                instance ?: AdKitFirebaseRemoteConfigHelper(

                ).also { instance = it }
            }
        }
    }

    private val _configFetched = Channel<Boolean>()
    val configFetched = _configFetched.receiveAsFlow()


    fun fetchRemoteValues(isDebug: Boolean) {

        runnableSplash = Runnable {
            if (isHandlerRunning) {
                isHandlerRunning = false
                _configFetched.trySend(true)
            }
        }

        try {
            startHandler()
            Firebase.remoteConfig.apply {
                configureRemoteConfig(this, isDebug)
                listenForUpdates(this)
                fetchRemoteConfig(this)
            }
        } catch (e: Exception) {
            configFetched()
        }
    }

    private fun configFetched() {
        if (isHandlerRunning) {
            removeCallBacks()
            _configFetched.trySend(true)
        }
    }

    private fun configureRemoteConfig(
        firebaseRemoteConfig: FirebaseRemoteConfig,
        isDebug: Boolean,
    ) {
        try {
            firebaseRemoteConfig.apply {
                val settings = FirebaseRemoteConfigSettings.Builder()
                    .apply {
                        if (isDebug) {
                            setMinimumFetchIntervalInSeconds(10)
                        }
                        setFetchTimeoutInSeconds(5)
                    }.build()

                setConfigSettingsAsync(settings)
            }
        } catch (e: Exception) {
            configFetched()
        }
    }

    fun setDefaultRemoteConfigs(
        configDefaults: Map<String, Any>
    ) {
        cachedConfig.putAll(configDefaults)
        try {
            Firebase.remoteConfig.apply {
                setDefaultsAsync(configDefaults)
            }
        } catch (e: Exception) {

        }
    }

    private fun fetchRemoteConfig(firebaseRemoteConfig: FirebaseRemoteConfig) {
        try {
            firebaseRemoteConfig.apply {
                fetchAndActivate()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("AdKit_Logs", "Firebase Fetch successful")
                            cacheActivatedValues(this)
                        } else {
                            Log.e("AdKit_Logs", "Firebase Fetch failed", task.exception)
                        }
                        configFetched()
                    }
                    .addOnFailureListener {
                        configFetched()
                    }
                    .addOnCanceledListener {
                        configFetched()
                    }
            }
        } catch (e: Exception) {
            Log.d("AdKit_Logs", "fetchRemoteConfig package error: ")
            configFetched()
        }

    }

    private fun listenForUpdates(firebaseRemoteConfig: FirebaseRemoteConfig) {
        firebaseRemoteConfig.addOnConfigUpdateListener(object : ConfigUpdateListener {
            override fun onUpdate(configUpdate: ConfigUpdate) {
                firebaseRemoteConfig.activate()
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            cacheActivatedValues(firebaseRemoteConfig)
                            Log.d("AdKit_Logs", "Config updated & activated.")
                        }
                    }
            }

            override fun onError(error: FirebaseRemoteConfigException) {
                Log.e("AdKit_Logs", "Config update listener failed", error)
            }
        })
    }

    private fun cacheActivatedValues(firebaseRemoteConfig: FirebaseRemoteConfig) {
        try {
            firebaseRemoteConfig.all.forEach { (key, value) ->
                cachedConfig[key] = value.asString()
            }
        } catch (_: Exception) {
        }
    }

    internal fun getBoolean(key: String, def: Boolean): Boolean {
        return when (val value = cachedConfig[key]) {
            is Boolean -> value
            is String -> value.toBooleanStrictOrNull() ?: defaultRemoteConfig.getDefaultBoolean(key, def)
            is Number -> value.toInt() != 0
            else -> defaultRemoteConfig.getDefaultBoolean(key, def)
        }
    }

    internal fun getLong(key: String, def: Long): Long {
        return when (val value = cachedConfig[key]) {
            is Number -> value.toLong()
            is String -> value.toLongOrNull() ?: defaultRemoteConfig.getDefaultLong(key, def)
            else -> defaultRemoteConfig.getDefaultLong(key, def)
        }
    }

    internal fun getString(key: String, def: String): String {
        return when (val value = cachedConfig[key]) {
            is String -> value
            null -> defaultRemoteConfig.getDefaultString(key, def)
            else -> value.toString()
        }
    }

    fun setDefaultBool(key: String, value: Boolean) {
        defaultRemoteConfig.bool(key, value)
        cachedConfig[key] = value
        try {
            Firebase.remoteConfig.apply {
                setDefaultsAsync(defaultRemoteConfig.configMap)
            }
        } catch (e: Exception) {

        }
    }

    fun setDefaultString(key: String, value: String) {
        defaultRemoteConfig.string(key, value)
        cachedConfig[key] = value
        try {
            Firebase.remoteConfig.apply {
                setDefaultsAsync(defaultRemoteConfig.configMap)
            }
        } catch (e: Exception) {

        }
    }

    fun setDefaultLong(key: String, value: Long) {
        defaultRemoteConfig.long(key, value)
        cachedConfig[key] = value
        try {
            Firebase.remoteConfig.apply {
                setDefaultsAsync(defaultRemoteConfig.configMap)
            }
        } catch (e: Exception) {

        }
    }

}
