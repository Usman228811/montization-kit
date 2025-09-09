package io.monetize.kit.sdk.ads.interstitial

import android.app.Activity
import android.os.Handler
import android.os.Looper
import io.monetize.kit.sdk.ads.interstitial.splash.OpenAdInterstitialManager
import io.monetize.kit.sdk.ads.interstitial.splash.SplashInterstitialManager
import io.monetize.kit.sdk.core.utils.init.AdKit


class AdKitSplashAdController private constructor(
) {
    private var openAdInterstitialManager: OpenAdInterstitialManager? = null
    private var mInterstitialControllerListener: InterstitialControllerListener? = null
    private var splashInterstitialManager: SplashInterstitialManager? = null
    private var handlerAd: Handler = Handler(Looper.getMainLooper())
    private var loadAndShow = true
    private var isAdEnable = true
    private var isForOpenAd = false

    init {

        openAdInterstitialManager = OpenAdInterstitialManager.getInstance()
        splashInterstitialManager = SplashInterstitialManager.getInstance()
    }

    companion object {
        @Volatile
        private var instance: AdKitSplashAdController? = null


        internal fun getInstance(
        ): AdKitSplashAdController {
            return instance ?: synchronized(this) {
                instance ?: AdKitSplashAdController(
                ).also { instance = it }
            }
        }
    }

    private fun closeCallBack() {
        if (loadAndShow) {
            mInterstitialControllerListener?.onAdClosed()
        } else {
            mInterstitialControllerListener?.onAdLoaded()
        }
    }

    fun showInterstitial(activity: Activity, listener: InterstitialControllerListener?) {
        this.mInterstitialControllerListener = listener
        mInterstitialControllerListener?.let {
            if (isAdEnable) {
                if (isForOpenAd) {
                    openAdInterstitialManager?.showOpenAd(activity, it)
                } else {
                    splashInterstitialManager?.showInterstitial(activity, it)
                }
            } else {
                it.onAdClosed(false)
            }
        }
    }

    fun hasAd(): Boolean {
        return if (isForOpenAd) {
            openAdInterstitialManager?.hasAd() ?: false
        } else {
            splashInterstitialManager?.hasAd() ?: false
        }

    }


    fun initSplashInterstitial(
        activity: Activity,
        placementKey: String,
        adIdKey: String,
        loadAndShow: Boolean = true,
        interAdsConfigs: InterAdsConfigs,
        listener: InterstitialControllerListener?,
    ) {
        mInterstitialControllerListener = listener
        this.loadAndShow = loadAndShow


        AdKit.initializer.initAdsConfigs(
            interAdsConfigs = interAdsConfigs
        )

        isAdEnable = AdKit.firebaseHelper.getBoolean("${placementKey}_isAdEnable", true)
        var time = interAdsConfigs.splashTime
        if (time == 0L) {
            time = 16L
        }

        if (AdKit.adKitPref.isAppPurchased ||
            !isAdEnable ||
            !AdKit.internetController.isConnected ||
            AdKit.initializer.getDisableAds() ||
            AdKit.consentManager.canRequestAds.not()
        ) {
            handlerAd.postDelayed({
                closeCallBack()
            }, 2000)
        } else {
            isForOpenAd = AdKit.firebaseHelper.getBoolean("${placementKey}_isAdOpenAd", false)
            if (isForOpenAd) {
                openAdInterstitialManager?.initOpenAdInterstitial(
                    activity = activity,
                    placementKey = placementKey,
                    isAdEnable = isAdEnable,
                    adIdKey = adIdKey,
                    time = time,
                    loadAndShow = loadAndShow,
                    listener = mInterstitialControllerListener
                )

            } else {
                splashInterstitialManager?.initAd(
                    activity = activity,
                    placementKey = placementKey,
                    isAdEnable = isAdEnable,
                    adIdKey = adIdKey,
                    time = time,
                    loadAndShow = loadAndShow,
                    listener = listener
                )
            }
        }
    }

    fun resetSplash() {
        splashInterstitialManager?.resetSplash()
        openAdInterstitialManager?.resetSplash()
    }

    fun pauseAd() {
        if (isForOpenAd) {
            openAdInterstitialManager?.pauseAd()
        } else {
            splashInterstitialManager?.pauseAd()

        }
    }

    fun resumeAd(activity: Activity) {
        if (loadAndShow) {
            if (isForOpenAd) {
                openAdInterstitialManager?.resumeAd(activity)
            } else {
                splashInterstitialManager?.resumeAd(activity)
            }
        }
    }

    fun setAppInPause(isAppPause: Boolean) {
        splashInterstitialManager?.setAppInPause(isAppPause)
        openAdInterstitialManager?.setAppInPause(isAppPause)
    }


}
