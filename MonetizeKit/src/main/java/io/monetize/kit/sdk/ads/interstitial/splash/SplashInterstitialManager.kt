package io.monetize.kit.sdk.ads.interstitial.splash

import android.app.Activity
import android.os.Handler
import android.os.Looper
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import io.monetize.kit.sdk.ads.interstitial.InterstitialControllerListener
import io.monetize.kit.sdk.ads.open.AdLoadingDialog
import io.monetize.kit.sdk.core.utils.IS_INTERSTITIAL_Ad_SHOWING
import io.monetize.kit.sdk.core.utils.init.AdKit

internal class SplashInterstitialManager private constructor(
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

    private var splashTime: Long = 16L
    private var isAdEnable: Boolean = false

    private var adIdKey: String = ""
    private var placementKey: String = ""
    private var loadAndShow: Boolean = true

    companion object {
        @Volatile
        private var instance: SplashInterstitialManager? = null


        internal fun getInstance(
        ): SplashInterstitialManager {
            return instance ?: synchronized(this) {
                instance ?: SplashInterstitialManager(
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

    fun resumeAd(activity: Activity) {
        if (isPauseDone) {
            isPauseDone = false
            if (!isAdEnable) {
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
                val adId = AdKit.interIdManager.getNextInterId(adIdKey)
                if (adId.isNullOrEmpty()) throw IllegalStateException("Splash Ad IDs not set. Call setSplashId() first.")

                InterstitialAd.load(
                    context, adId,
                    AdRequest.Builder().build(),
                    object : InterstitialAdLoadCallback() {
                        override fun onAdLoaded(splashAd: InterstitialAd) {
                            super.onAdLoaded(splashAd)
                            interstitialAd = splashAd
                            canRequestAd = true

                            if (isHandlerRunning) {
                                removeCallBacks()
                                mInterstitialControllerListener?.onAdLoaded()
                                if (loadAndShow) {
                                    showSplashAd(context)
                                }
                            }
                        }

                        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                            super.onAdFailedToLoad(loadAdError)
                            interstitialAd = null
                            canRequestAd = true
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
            if (loadAndShow) {
                mInterstitialControllerListener?.onAdClosed()
            } else {
                mInterstitialControllerListener?.onAdLoaded()

            }
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
        interstitialControllerListener: InterstitialControllerListener,
    ) {
        mInterstitialControllerListener = interstitialControllerListener
        if (AdKit.adKitPref.isAppPurchased || !isAdEnable || isAppPause || IS_INTERSTITIAL_Ad_SHOWING) {
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
        if (AdKit.firebaseHelper.getBoolean("INTER_LOADING_ENABLE", false)) {
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
                    setFullScreenContentCallback(activity)
                    mInterstitialControllerListener?.onAdShow()
                    interstitialAd?.show(activity)
                }

                else -> {
                    mInterstitialControllerListener?.onAdClosed()
                }
            }
        } catch (_: Exception) {
            hideProgressAndNullAd()
        } catch (_: OutOfMemoryError) {
            hideProgressAndNullAd()
        }
    }

    fun initAd(

        activity: Activity,
        placementKey: String,
        isAdEnable: Boolean,
        adIdKey: String,
        time: Long,
        loadAndShow: Boolean,
        listener: InterstitialControllerListener?

    ) {
        this.splashTime = time
        this.loadAndShow = loadAndShow
        this.placementKey = placementKey
        this.adIdKey = adIdKey
        this.isAdEnable = isAdEnable
        this.mInterstitialControllerListener = listener

        runnableSplash = Runnable {
            if (mInterstitialControllerListener != null && isHandlerRunning) {
                isHandlerRunning = false
                if (this.loadAndShow) {
                    mInterstitialControllerListener?.onAdClosed()
                } else {
                    mInterstitialControllerListener?.onAdLoaded()
                }
            }
        }
        try {
            startHandler()
            loadNewInterstitialAd(activity)
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

                override fun onAdClicked() {
                    super.onAdClicked()
                    AdKit.analytics.postAnalytics("Splash_inter_click")

                }

                override fun onAdDismissedFullScreenContent() {
                    AdKit.analytics.postAnalytics("Splash_inter_cross")
                    hideProgressAndNullAd(true)
                    super.onAdDismissedFullScreenContent()
                }

                override fun onAdShowedFullScreenContent() {
                    super.onAdShowedFullScreenContent()
                    IS_INTERSTITIAL_Ad_SHOWING = true
                    interstitialAd = null
                    AdKit.analytics.postAnalytics("Splash_inter_show")
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    super.onAdFailedToShowFullScreenContent(adError)
                    hideProgressAndNullAd()
                }

            }
    }

    private fun hideProgressAndNullAd(isInterShowed: Boolean = false) {
        mInterstitialControllerListener?.onAdClosed(isInterShowed)
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