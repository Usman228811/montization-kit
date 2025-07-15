package io.monetize.kit.sdk.ads.interstitial

import android.app.Activity
import android.os.Handler
import android.os.Looper
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import io.monetize.kit.sdk.ads.open.AdLoadingDialog
import io.monetize.kit.sdk.core.utils.IS_INTERSTITIAL_Ad_SHOWING
import io.monetize.kit.sdk.core.utils.init.AdKit


class AdKitSplashAdController private constructor(
) {
    private val handlerAd = Handler(Looper.getMainLooper())
    private var canRequestAd = true
    private var adLoadingDialog: AdLoadingDialog? = null
    private var interstitialAd: InterstitialAd? = null
    private var runnableSplash: Runnable? = null
    private var mInterstitialControllerListener: InterstitialControllerListener? = null
    private var isHandlerRunning = false
    private var isPauseDone = false
    private var isAppPause = false
    private var splashTime :Long = 16L
    private var isAdEnable :Boolean = false

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


    fun setAppInPause(isAppPause: Boolean) {
        this.isAppPause = isAppPause
    }


    fun resetSplash() {
        interstitialAd = null
        mInterstitialControllerListener = null
        isHandlerRunning = false
        isPauseDone = false
    }

    fun hasAd(): Boolean {
        return interstitialAd != null
    }

    fun pauseAd() {
        isPauseDone = true
    }

    fun resumeAd(activity: Activity, enable: Boolean) {
        if (isPauseDone) {
            isPauseDone = false
            if (!enable) {
                if (!isHandlerRunning) {
                    handlerAd.postDelayed({
                        mInterstitialControllerListener?.onAdClosed()
                    }, 1000)
                }
            } else {
                if (!isHandlerRunning) {
                    handlerAd.postDelayed({
                        showSplashAd(activity)
                    }, 1000)
                }
            }
        }
    }

    private fun loadNewInterstitialAd(context: Activity) {
        try {
            if (interstitialAd == null) {
                if (!canRequestAd) {
                    return
                }
                canRequestAd = false
//                if (isDebug) {
//                    context.showToast("Splash Ad Calling")
//
//                }
                val adId = AdKit.interIdManager.getNextInterId(placementKey)
                if (adId.isNullOrEmpty()) throw IllegalStateException("Splash Ad IDs not set. Call setSplashId() first.")

                InterstitialAd.load(
                    context, adId,
                    AdRequest.Builder().build(),
                    object : InterstitialAdLoadCallback() {
                        override fun onAdLoaded(splashAd: InterstitialAd) {
                            super.onAdLoaded(splashAd)
                            interstitialAd = splashAd
                            canRequestAd = true
//                            if (isDebug) {
//                                context.showToast("Splash Ad Loaded")
//                            }

                            if (isHandlerRunning) {
                                removeCallBacks()
                                mInterstitialControllerListener?.onAdLoaded()
                                showSplashAd(context)
                            }
                        }

                        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                            super.onAdFailedToLoad(loadAdError)
                            interstitialAd = null
                            canRequestAd = true
//                            if (isDebug) {
//                                context.showToast("Splash Ad Failed: ${loadAdError.code}")
//                            }
                            handleException()
                        }
                    })
            } else {
                handleException()
            }
        } catch (_: Exception) {
            canRequestAd = true
            handleException()
        } catch (_: OutOfMemoryError) {
            canRequestAd = true
            handleException()
        }
    }

    private fun handleException() {
        if (isHandlerRunning) {
            removeCallBacks()
            mInterstitialControllerListener?.onAdClosed()
        }
    }


    private fun hideProgress() {
        try {
            adLoadingDialog?.dismissAlertDialog()
            adLoadingDialog = null
        } catch (_: Exception) {
        }
    }

    fun showInterstitial(
        activity: Activity,
        enable: Boolean,
        interstitialControllerListener: InterstitialControllerListener,
    ) {
        mInterstitialControllerListener = interstitialControllerListener
        if (AdKit.adKitPref.isAppPurchased || !enable || isAppPause || IS_INTERSTITIAL_Ad_SHOWING) {
            interstitialControllerListener.onAdClosed()
        } else if (interstitialAd != null) {
            adLoadingCheck(activity)
        } else {
            interstitialControllerListener.onAdClosed()
        }
    }

    private fun adLoadingCheck(
        activity: Activity,
    ) {
        if (AdKit.interHelper.getInterAdsConfigs()?.interLoadingEnable == true) {
            try {
                mInterstitialControllerListener?.onAdShow()
                adLoadingDialog = AdLoadingDialog(activity)
                adLoadingDialog?.showAlertDialog()
                handlerAd.postDelayed({
                    showInterAd(activity)
                    hideProgress()
                }, 1000)
            } catch (_: Exception) {
                hideProgress()
                showInterAd(activity)
            }
        } else {
            showInterAd(activity)
        }
    }


    private fun showInterAd(
        activity: Activity,
    ) {
        try {
            when {
                isAppPause -> {
                    mInterstitialControllerListener?.onAdClosed()
                }

                interstitialAd != null -> {
//                    if (isDebug) {
//                        activity.showToast("Splash Ad Showing")
//                    }
                    setFullScreenContentCallback(activity)
                    mInterstitialControllerListener?.onAdShow()
                    interstitialAd?.show(activity)
                }

                else -> {
                    mInterstitialControllerListener?.onAdClosed()
                }
            }
        } catch (_: Exception) {
            hideProgressAndNullAd(activity)
        } catch (_: OutOfMemoryError) {
            hideProgressAndNullAd(activity)
        }
    }

    private var placementKey: String = ""

    fun initSplashAdmob(
        activity: Activity,
        placementKey: String,
        interAdsConfigs: InterAdsConfigs,
        listener: InterstitialControllerListener?,
    ) {


        AdKit.initializer.initAdsConfigs(
            interAdsConfigs = interAdsConfigs
        )

        this.isAdEnable = AdKit.firebaseHelper.getBoolean("${placementKey}_isAdEnable", true)
        this.splashTime = AdKit.firebaseHelper.getLong("SPLASH_TIME", 16)
        mInterstitialControllerListener = listener
        this.placementKey = placementKey
        canRequestAd = true
        interstitialAd = null
        isHandlerRunning = false
        runnableSplash = Runnable {
            if (mInterstitialControllerListener != null && isHandlerRunning) {
                isHandlerRunning = false
                mInterstitialControllerListener?.onAdClosed()
            }
        }
        try {
            if (!AdKit.adKitPref.isAppPurchased && isAdEnable && AdKit.consentManager.canRequestAds) {
                if (!AdKit.internetController.isConnected) {
                    handlerAd.postDelayed({ mInterstitialControllerListener?.onAdClosed() }, 5000)
                    return
                }
                startHandler()
                loadNewInterstitialAd(activity)
            } else {
                handlerAd.postDelayed({
                    mInterstitialControllerListener?.onAdClosed()
                }, 5000)
            }
        } catch (_: Exception) {
        }
    }


    private fun startHandler() {
        val splashTime = splashTime
        if (!isHandlerRunning) {
            isHandlerRunning = true
            runnableSplash?.let {
                handlerAd.postDelayed(it, splashTime * 1000)
            }
        }
    }


    private fun showSplashAd(activity: Activity) {
        if (!isPauseDone) {
            if (!IS_INTERSTITIAL_Ad_SHOWING && isAdEnable) {
                if (interstitialAd != null) {
                    adLoadingCheck(activity)
                } else {
                    mInterstitialControllerListener?.onAdClosed()
                }
            } else {
                mInterstitialControllerListener?.onAdClosed()
            }
        }
    }

    private fun setFullScreenContentCallback(
        activity: Activity,
    ) {
        interstitialAd?.fullScreenContentCallback =
            object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    hideProgressAndNullAd(activity)
                    super.onAdDismissedFullScreenContent()
//                    activity.userAnalytics("Splash_Ad_Close")
                }

                override fun onAdShowedFullScreenContent() {
                    super.onAdShowedFullScreenContent()
                    IS_INTERSTITIAL_Ad_SHOWING = true
                    interstitialAd = null
//                    activity.userAnalytics("Splash_Ad_Show")
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    super.onAdFailedToShowFullScreenContent(adError)
                    hideProgressAndNullAd(activity)
//                    activity.userAnalytics("Splash_Ad_Show_Failed")
                }

            }
    }

    private fun hideProgressAndNullAd(activity: Activity) {
        mInterstitialControllerListener?.onAdClosed()
        IS_INTERSTITIAL_Ad_SHOWING = false
        interstitialAd = null
        hideProgress()
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

    fun destroyAd() {
        if (isHandlerRunning) {
            removeCallBacks()
        }
    }
}
