package io.monetize.kit.sdk.core.utils.consent


import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.ads.mediation.pangle.PangleMediationAdapter
import com.google.android.gms.ads.AdRequest
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.mbridge.msdk.MBridgeConstans
import com.mbridge.msdk.out.MBridgeSDKFactory
import com.vungle.ads.VunglePrivacySettings
import io.monetize.kit.sdk.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import net.premiumads.sdk.admob.PremiumBannerAd
import net.premiumads.sdk.admob.PremiumInterstitialAd
import net.premiumads.sdk.admob.PremiumRewardedAd


class AdKitConsentManager private constructor(
    context: Context,
    private val isDebug: Boolean
) {
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


        internal fun getInstance(
            context: Application,
            isDebug: Boolean
        ): AdKitConsentManager {
            return instance ?: synchronized(this) {
                instance ?: AdKitConsentManager(
                    context.applicationContext,
                    isDebug
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
            val params: ConsentRequestParameters = if (isDebug) {
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
                            onConsent(activity)
                        }
                    } catch (_: Exception) {
                        onConsent(activity)
                    }
                }
            }, { error ->
                error.message
                onConsent(activity)
            })
        } catch (_: Exception) {
            onConsent(activity)
        }
    }

    fun onConsent(activity: Activity){
        try {
            MBridgeSDKFactory.getMBridgeSDK().apply {
                setConsentStatus(
                    activity,
                    if (canRequestAds) {
                        MBridgeConstans.IS_SWITCH_ON
                    } else {
                        MBridgeConstans.IS_SWITCH_OFF
                    }
                )
            }
            PangleMediationAdapter.setGDPRConsent(if (canRequestAds) 1 else 0)
            PangleMediationAdapter.setPAConsent(if (canRequestAds) 1 else 0)
            VunglePrivacySettings.setGDPRStatus(canRequestAds, "v1.0.0")
            VunglePrivacySettings.setCCPAStatus(canRequestAds)

            val builder = AdRequest.Builder()
            val extras = Bundle()
            extras.putString("npa", "1")
            builder.addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
            builder.addNetworkExtrasBundle(PremiumBannerAd::class.java, extras)
            builder.addNetworkExtrasBundle(PremiumInterstitialAd::class.java, extras)
            builder.addNetworkExtrasBundle(PremiumRewardedAd::class.java, extras)

            callConsent()
        } catch (e: Exception){
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