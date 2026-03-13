package io.monetize.kit.sdk.ads.interstitial.splash

import android.app.Activity
import android.os.Handler
import android.os.Looper
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest
import com.google.android.libraries.ads.mobile.sdk.common.AdValue
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAd
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAdEventCallback
import io.monetize.kit.sdk.ads.interstitial.InterstitialControllerListener
import io.monetize.kit.sdk.ads.open.AdLoadingDialog
import io.monetize.kit.sdk.core.utils.IS_INTERSTITIAL_Ad_SHOWING
import io.monetize.kit.sdk.core.utils.appflyer.postAdImpression
import io.monetize.kit.sdk.core.utils.appflyer.revenueListener
import io.monetize.kit.sdk.core.utils.firebaseBoolean
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

    private var adIdKey: String = ""
    private var adId: String = ""
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
            if (!isHandlerRunning) {
                handlerAd.postDelayed({
                    showSplashAd(activity)
                }, 1000)
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
                adId = AdKit.interIdManager.getNextInterId(adIdKey) ?: ""
//                if (adId.isNullOrEmpty()) throw IllegalStateException("Splash Ad IDs not set. Call setSplashId() first.")

                InterstitialAd.load(
                    AdRequest.Builder(adId).build(),
                    object : AdLoadCallback<InterstitialAd> {
                        override fun onAdLoaded(ad: InterstitialAd) {
                            interstitialAd = ad
//                            interstitialAd?.revenueListener(adId)

                            canRequestAd = true
                            context.runOnUiThread {

                                if (isHandlerRunning) {
                                    removeCallBacks()
                                    mInterstitialControllerListener?.onAdLoaded(
                                        reason = "$placementKey called onAdLoaded because: ad loaded successfully"
                                    )
                                    if (loadAndShow) {
                                        showSplashAd(context)
                                    }
                                }
                            }

                        }

                        override fun onAdFailedToLoad(adError: LoadAdError) {

                            context.runOnUiThread {
                                interstitialAd = null
                                canRequestAd = true
                                handleException(
                                    reason = "ad is failed to load code: ${adError.code}, message:  ${adError.message}"
                                )
                            }
                        }
                    },
                )


//                    object : InterstitialAdLoadCallback() {
//                        override fun onAdLoaded(splashAd: InterstitialAd) {
//                            super.onAdLoaded(splashAd)
//                            interstitialAd = splashAd
//                            interstitialAd?.revenueListener(adId)
//
//                            canRequestAd = true
//
//                            if (isHandlerRunning) {
//                                removeCallBacks()
//                                mInterstitialControllerListener?.onAdLoaded(
//                                    reason = "$placementKey called onAdLoaded because: ad loaded successfully"
//                                )
//                                if (loadAndShow) {
//                                    showSplashAd(context)
//                                }
//                            }
//                        }
//
//                        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
//                            super.onAdFailedToLoad(loadAdError)
//                            interstitialAd = null
//                            canRequestAd = true
//                            handleException(
//                                reason = "ad is failed to load code: ${loadAdError.code}, message:  ${loadAdError.message}"
//                            )
//                        }
//                    })
            } else {
                handleException("")
            }
        } catch (_: Exception) {
            canRequestAd = true
            handleException("Exception")
        } catch (_: OutOfMemoryError) {
            canRequestAd = true
            handleException("Exception")
        }
    }

    private fun handleException(reason: String) {
        if (isHandlerRunning) {
            removeCallBacks()
            if (loadAndShow) {
                mInterstitialControllerListener?.onAdClosed(reason = "$placementKey called onAdClosed because : $reason")
            } else {
                mInterstitialControllerListener?.onAdLoaded(reason = "$placementKey called onAdLoaded because : $reason")

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
        if (isAppPause || IS_INTERSTITIAL_Ad_SHOWING) {
            interstitialControllerListener.onAdClosed(
                reason = "$placementKey called onAdClosed because: App is minimized | Other Ad is showing"
            )
        } else if (interstitialAd != null) {
            adLoadingCheck(activity)
        } else {
            interstitialControllerListener.onAdClosed(
                reason = "$placementKey called onAdClosed because: ad is null"
            )
        }
    }

    private fun adLoadingCheck(
        activity: Activity,
    ) {
        if (firebaseBoolean("SPLASH_INTER_LOADING_ENABLE", false)) {
            try {
                mInterstitialControllerListener?.onAdShow()
                adLoadingDialog = AdLoadingDialog(activity)
                adLoadingDialog?.showAlertDialog()
                handlerAd.postDelayed({
                    showInterAd(activity)
                    hideProgress()
                }, 1000)
            } catch (e: Exception) {
                e.printStackTrace()
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
                    mInterstitialControllerListener?.onAdClosed(
                        reason = "$placementKey called onAdClosed because: App is minimized"
                    )
                }

                interstitialAd != null -> {
                    setFullScreenContentCallback(activity)
                    mInterstitialControllerListener?.onAdShow()
                    interstitialAd?.show(activity)
                }

                else -> {
                    mInterstitialControllerListener?.onAdClosed(reason = "")
                }
            }
        } catch (e: Exception) {
            hideProgressAndNullAd(reason = "Inter Exception ${e.message}")
        } catch (e: OutOfMemoryError) {
            hideProgressAndNullAd(reason = "Inter Exception ${e.message}")
        }
    }

    fun initAd(

        activity: Activity,
        placementKey: String,
        adIdKey: String,
        time: Long,
        loadAndShow: Boolean,
        listener: InterstitialControllerListener?

    ) {
        this.splashTime = time
        this.loadAndShow = loadAndShow
        this.placementKey = placementKey
        this.adIdKey = adIdKey
        this.mInterstitialControllerListener = listener

        runnableSplash = Runnable {
            if (mInterstitialControllerListener != null && isHandlerRunning) {
                isHandlerRunning = false
                if (this.loadAndShow) {
                    mInterstitialControllerListener?.onAdClosed(
                        reason = "called onAdClosed because: splash ad time is completed"
                    )
                } else {
                    mInterstitialControllerListener?.onAdLoaded(
                        reason = "called onAdLoaded because: splash ad time is completed"
                    )
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
            if (!IS_INTERSTITIAL_Ad_SHOWING) {
                if (interstitialAd != null) {
                    adLoadingCheck(activity)
                } else {
                    mInterstitialControllerListener?.onAdClosed(
                        reason = "$placementKey called onAdClosed because: Ad is null"
                    )
                }
            } else {
                mInterstitialControllerListener?.onAdClosed(
                    reason = "$placementKey called onAdClosed because: Other Ad is showing"
                )
            }
        }
    }

    private fun setFullScreenContentCallback(
        activity: Activity,
    ) {
        interstitialAd?.adEventCallback =
            object : InterstitialAdEventCallback {

                override fun onAdPaid(value: AdValue) {
                    super.onAdPaid(value)
                    revenueListener(adId, value, "INTERSTITIAL")
                }

                override fun onAdClicked() {
                    super.onAdClicked()
                    activity.runOnUiThread {
                        AdKit.analytics.postAnalytics("Splash_inter_click")
                    }
                }

                override fun onAdImpression() {
                    super.onAdImpression()
                    postAdImpression("InterstitialAd")
                }

                override fun onAdDismissedFullScreenContent() {
                    activity.runOnUiThread {
                        AdKit.analytics.postAnalytics("Splash_inter_cross")
                    }
                    hideProgressAndNullAd(true, reason = "ad is showed successfully")
                    super.onAdDismissedFullScreenContent()
                }

                override fun onAdShowedFullScreenContent() {
                    super.onAdShowedFullScreenContent()
                    IS_INTERSTITIAL_Ad_SHOWING = true
                    interstitialAd = null
                    activity.runOnUiThread {
                        AdKit.analytics.postAnalytics("Splash_inter_show")
                    }
                }

                override fun onAdFailedToShowFullScreenContent(fullScreenContentError: FullScreenContentError) {
                    super.onAdFailedToShowFullScreenContent(fullScreenContentError)
                    hideProgressAndNullAd(
                        reason = "onAdFailedToShowFullScreenContent code: ${fullScreenContentError.code} message: ${fullScreenContentError.message}"
                    )
                }
            }
    }

    private fun hideProgressAndNullAd(isInterShowed: Boolean = false, reason: String) {
        mInterstitialControllerListener?.onAdClosed(
            isInterShowed,
            "$placementKey called onAdClosed because: $reason"
        )
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