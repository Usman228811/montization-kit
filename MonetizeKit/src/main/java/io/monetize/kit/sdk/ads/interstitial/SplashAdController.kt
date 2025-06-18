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
import io.monetize.kit.sdk.core.utils.IS_APP_PAUSE
import io.monetize.kit.sdk.core.utils.IS_INTERSTITIAL_Ad_SHOWING
import io.monetize.kit.sdk.core.utils.MKInternetController
import io.monetize.kit.sdk.core.utils.MkPref
import io.monetize.kit.sdk.core.utils.consent.MKConsentManager


class SplashAdController(
    private val internetController: MKInternetController,
    private val myPref: MkPref,
    private val mConsent: MKConsentManager,
) {
    private val handlerAd = Handler(Looper.getMainLooper())
    private var canRequestAd = true
    private var adLoadingDialog: AdLoadingDialog? = null
    private var interstitialAd: InterstitialAd? = null
    private var runnableSplash: Runnable? = null
    private var mInterstitialControllerListener: InterstitialControllerListener? = null
    private var isHandlerRunning = false
    private var isPauseDone = false
    private var adId: String = ""
    private var interControllerConfig: InterControllerConfig? = null



    fun setSplashId(id: String, interControllerConfig: InterControllerConfig) {
        adId = id
        this.interControllerConfig = interControllerConfig
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
                if (adId.isEmpty()) throw IllegalStateException("Splash Ad IDs not set. Call setSplashId() first.")

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
        if (myPref.isAppPurchased || !enable || IS_APP_PAUSE || IS_INTERSTITIAL_Ad_SHOWING) {
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
        if (interControllerConfig?.interLoadingEnable == true) {
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
                IS_APP_PAUSE -> {
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

    fun initSplashAdmob(
        activity: Activity, enable: Boolean,
        listener: InterstitialControllerListener?,
    ) {
        mInterstitialControllerListener = listener
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
            if (!myPref.isAppPurchased && enable && mConsent.canRequestAds) {
                if (!internetController.isConnected) {
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
        val splashTime = interControllerConfig?.splashTime?: 16L
        if (!isHandlerRunning) {
            isHandlerRunning = true
            runnableSplash?.let {
                handlerAd.postDelayed(it,  splashTime* 1000)
            }
        }
    }


    private fun showSplashAd(activity: Activity) {
        if (!isPauseDone) {
            if (!IS_INTERSTITIAL_Ad_SHOWING && interControllerConfig?.splashInterEnable == true) {
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
