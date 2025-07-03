package io.monetize.kit.sdk.core.utils.consent


import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import io.monetize.kit.sdk.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class AdKitConsentManager private constructor(context: Context) {
    private val coroutineScope by lazy {
        CoroutineScope(Dispatchers.IO)
    }
    private val consentInformation: ConsentInformation =
        UserMessagingPlatform.getConsentInformation(context)

    private val _googleConsent: Channel<Boolean> = Channel()
    val googleConsent = _googleConsent.receiveAsFlow()
    val canRequestAds: Boolean
        get() = consentInformation.canRequestAds()
    private var isRequestingConsent = false


    companion object {
        @Volatile
        private var instance: AdKitConsentManager? = null


        fun getInstance(
            context: Context,
        ): AdKitConsentManager {
            return instance ?: synchronized(this) {
                instance ?: AdKitConsentManager(
                    context.applicationContext,
                ).also { instance = it }
            }
        }
    }

    fun gatherConsent(activity: Activity) {

        if (isRequestingConsent) {
            return
        }
        isRequestingConsent = true
        try {
            Log.d("AdKit_Logs", "gatherConsent: ${BuildConfig.DEBUG}")
            val params: ConsentRequestParameters = if (BuildConfig.DEBUG) {
                val debugSettings = ConsentDebugSettings.Builder(activity)
                    .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                    .addTestDeviceHashedId("F6A02AFF47CB6BB7BF2AF64A8CC1D411").build()
                ConsentRequestParameters.Builder().setConsentDebugSettings(debugSettings).build()
            } else {
                ConsentRequestParameters.Builder().build()
            }
            consentInformation.requestConsentInfoUpdate(activity, params, {
                if (!activity.isDestroyed && !activity.isFinishing) {
                    try {
                        UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) {
                            callConsent()
                        }
                    } catch (_: Exception) {
                        callConsent()
                    }
                }
            }, { error ->
                error.message
                callConsent()
            })
        } catch (_: Exception) {
            callConsent()
        }
    }

    private fun callConsent() {
        isRequestingConsent = false
        coroutineScope.launch {
            _googleConsent.send(true)
        }
    }
}