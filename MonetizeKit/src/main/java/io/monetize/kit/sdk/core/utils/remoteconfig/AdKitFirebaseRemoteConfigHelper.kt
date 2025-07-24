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
import io.monetize.kit.sdk.core.utils.init.AdKit
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

class AdKitFirebaseRemoteConfigHelper private constructor() {

    private var runnableSplash: Runnable? = null
    private var isHandlerRunning = false
    private val handlerAd = Handler(Looper.getMainLooper())


    private fun startHandler() {
        var configFetchTime = AdKit.firebaseHelper.getLong("config_fetch_time", 8)
        Log.d("skdjflskfj", "startHandler1: ${configFetchTime}")
        if (configFetchTime == 0L) {
            configFetchTime = 8L
        }
        Log.d("skdjflskfj", "startHandler2: ${configFetchTime}")
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
                            Log.d("AdKit_Logs", "Config updated & activated.")
                        }
                    }
            }

            override fun onError(error: FirebaseRemoteConfigException) {
                Log.e("AdKit_Logs", "Config update listener failed", error)
            }
        })
    }

    fun getBoolean(key: String, def: Boolean): Boolean {
        return try {
            Firebase.remoteConfig.getBoolean(key)
        } catch (e: Exception) {
            def
        }
    }

    fun getLong(key: String, def: Long): Long {
        return try {
            Firebase.remoteConfig.getLong(key)
        } catch (e: Exception) {
            def
        }
    }

    fun getString(key: String, def: String): String {
        return try {
            Firebase.remoteConfig.getString(key)
        } catch (e: Exception) {
            def
        }
    }


}
