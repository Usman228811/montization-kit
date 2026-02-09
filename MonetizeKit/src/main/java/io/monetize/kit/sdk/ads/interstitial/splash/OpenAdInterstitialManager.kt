package io.monetize.kit.sdk.ads.interstitial.splash

import android.app.Activity
import android.os.Handler
import android.os.Looper
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import io.monetize.kit.sdk.ads.interstitial.InterstitialControllerListener
import io.monetize.kit.sdk.ads.open.AdLoadingDialog
import io.monetize.kit.sdk.core.utils.IS_INTERSTITIAL_Ad_SHOWING
import io.monetize.kit.sdk.core.utils.IS_OPEN_Ad_SHOWING
import io.monetize.kit.sdk.core.utils.appflyer.postAdImpression
import io.monetize.kit.sdk.core.utils.appflyer.revenueListener
import io.monetize.kit.sdk.core.utils.firebaseBoolean
import io.monetize.kit.sdk.core.utils.init.AdKit

internal class OpenAdInterstitialManager private constructor(
) {

    private var adIdKey: String = ""
    private var placementKey: String = ""

    private var mAppOpenAd: AppOpenAd? = null
    private var canRequestAd = true
    private var loadAndShow = true

    private var isAdEnable = true
    private var isAppInPause = false
    private var isPauseDone = false
    private var isLoadingEnable = true
    private var adId = ""

    fun resetSplash() {
        mAppOpenAd = null
        listener = null
        isHandlerAdDelayRunning = false
        isPauseDone = false
    }


    private val handlerAdDelay: Handler = Handler(Looper.getMainLooper())
    private var isHandlerAdDelayRunning = false
    private var runnableHandlerAdDelay: Runnable? = null

    fun setAppInPause(isAppPause: Boolean) {
        this.isAppInPause = isAppPause
    }

    private var listener: InterstitialControllerListener? = null
    private var splashTime = 0L
    fun initOpenAdInterstitial(
        activity: Activity,
        placementKey: String,
        isAdEnable: Boolean,
        adIdKey: String,
        time: Long,
        loadAndShow: Boolean,
        listener: InterstitialControllerListener?
    ) {
        this.isAdEnable = isAdEnable
        this.splashTime = time
        this.isLoadingEnable = firebaseBoolean("SPLASH_INTER_LOADING_ENABLE", false)
        this.loadAndShow = loadAndShow
        this.placementKey = placementKey
        this.adIdKey = adIdKey
        this.adId = AdKit.interIdManager.getNextInterId(adIdKey) ?: ""
        this.listener = listener

        runnableHandlerAdDelay = Runnable {
            if (isHandlerAdDelayRunning) {
                if (this.loadAndShow) {
                    listener?.onAdClosed(
                        reason = "called onAdClosed because: splash ad time is completed"
                    )
                } else {
                    listener?.onAdLoaded(
                        reason = "called onAdLoaded because: splash ad time is completed"
                    )
                }
                isHandlerAdDelayRunning = false
            }
        }
        loadAndShowOpenAd(activity)
    }


    companion object {
        @Volatile
        private var instance: OpenAdInterstitialManager? = null


        internal fun getInstance(): OpenAdInterstitialManager {
            return instance ?: synchronized(this) {
                instance ?: OpenAdInterstitialManager().also { instance = it }
            }
        }
    }

    fun pauseAd() {
        isPauseDone = true
    }

    fun resumeAd(activity: Activity) {
        if (isPauseDone) {
            isPauseDone = false
            if (!isAdEnable) {
                if (!isHandlerAdDelayRunning) {
                    handlerAdDelay.postDelayed({
                        listener?.onAdClosed(
                            reason = "$placementKey called onAdClosed because: ad is diable in remote config"
                        )
                    }, 1000)
                }
            } else {
                if (!isHandlerAdDelayRunning) {
                    handlerAdDelay.postDelayed({
                        checkProgressShowAd(activity)
                    }, 1000)
                }
            }
        }
    }


    private fun startDelayHandler() {
        if (!isHandlerAdDelayRunning) {
            isHandlerAdDelayRunning = true
            runnableHandlerAdDelay?.let {
                handlerAdDelay.postDelayed(
                    it, splashTime * 1000
                )
            }
        }
    }

    private fun loadAndShowOpenAd(activity: Activity) {

        if (hasAd()) {
            if (loadAndShow) {
                showAdIfAvailable(activity)
            } else {
                listener?.onAdLoaded("")
            }
        } else {
            if (!IS_INTERSTITIAL_Ad_SHOWING && !IS_OPEN_Ad_SHOWING && !isAppInPause && !AdKit.adKitPref.isAppPurchased) {
                if (!canRequestAd) {
                    return
                }
                if (adId.isNotEmpty() && isAdEnable && AdKit.consentManager.canRequestAds) {
                    canRequestAd = false
                    startDelayHandler()

                    AppOpenAd.load(
                        activity,
                        adId,
                        AdRequest.Builder().build(),
                        object : AppOpenAd.AppOpenAdLoadCallback() {
                            override fun onAdLoaded(appOpenAd: AppOpenAd) {
                                super.onAdLoaded(appOpenAd)
                                canRequestAd = true
                                mAppOpenAd = appOpenAd
                                mAppOpenAd?.revenueListener(adId)
                                if (isHandlerAdDelayRunning) {
                                    removeCallBacksDelay()
                                    listener?.onAdLoaded(
                                        reason = "$placementKey called onAdLoaded because: ad loaded successfully"
                                    )
                                    if (loadAndShow) {
                                        showAdIfAvailable(activity)
                                    }
                                }
                            }

                            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                                super.onAdFailedToLoad(loadAdError)
                                canRequestAd = true
                                mAppOpenAd = null
                                handleException(
                                    reason = "ad is failed to load code: ${loadAdError.code}, message:  ${loadAdError.message}"

                                )
                            }
                        })

                }

            } else {
                if (loadAndShow) {
                    listener?.onAdLoaded(
                        reason = "$placementKey called onAdLoaded because: App is minimized | Other Ad is showing | App is Purchased"

                    )
                } else {
                    listener?.onAdClosed(
                        reason = "$placementKey called onAdClosed because: App is minimized | Other Ad is showing | App is Purchased"

                    )

                }
            }
        }
    }

    private fun handleException(reason: String) {
        if (isHandlerAdDelayRunning) {
            removeCallBacksDelay()
            if (loadAndShow) {
                listener?.onAdClosed(reason="$placementKey called onAdClosed because: $reason")
            } else {
                listener?.onAdLoaded(reason="$placementKey called onAdLoaded because: $reason")

            }
        }
    }

    fun setFullScreenCallBacks() {
        mAppOpenAd?.fullScreenContentCallback =
            object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    mAppOpenAd = null
                    IS_INTERSTITIAL_Ad_SHOWING = false
                    listener?.onAdClosed(true,
                        reason = "$placementKey called onAdClosed because: ad is showed successfully"
                        )
                }

                override fun onAdImpression() {
                    super.onAdImpression()
                    postAdImpression("AppOpenAd")
                }

                override fun onAdFailedToShowFullScreenContent(
                    adError: AdError
                ) {
                    IS_INTERSTITIAL_Ad_SHOWING = false
                    listener?.onAdClosed(true,
                        reason = "$placementKey called onAdClosed because: onAdFailedToShowFullScreenContent code: ${adError.code} message: ${adError.message}"
                    )
                }

                override fun onAdShowedFullScreenContent() {
                    IS_INTERSTITIAL_Ad_SHOWING = true
                }
            }
    }

    fun removeCallBacksDelay() {
        try {
            isHandlerAdDelayRunning = false
            runnableHandlerAdDelay?.let {
                handlerAdDelay.removeCallbacks(it)
            }
        } catch (ignored: Exception) {
        }
    }

    fun showOpenAd(
        activity: Activity,
        interstitialControllerListener: InterstitialControllerListener,
    ) {
        listener = interstitialControllerListener
        if (AdKit.adKitPref.isAppPurchased || isAdEnable.not() || isAppInPause || IS_INTERSTITIAL_Ad_SHOWING) {
            interstitialControllerListener.onAdClosed(
                reason = "$placementKey called onAdClosed because: App is minimized | Ad is disabled | Other Ad is showing | App is Purchased"

            )
        } else if (hasAd()) {
            checkProgressShowAd(activity)
        } else {
            interstitialControllerListener.onAdClosed(
                reason = "$placementKey called onAdClosed because: ad is null"
            )
        }
    }

    private fun showAdIfAvailable(activity: Activity) {
        try {
            if (!IS_INTERSTITIAL_Ad_SHOWING) {
                if (!IS_OPEN_Ad_SHOWING && hasAd()) {
                    if (!isAppInPause) {
                        setFullScreenCallBacks()
                        checkProgressShowAd(activity)
                    }
                }
            }
        } catch (ignored: Exception) {
        }
    }

    private fun checkProgressShowAd(activity: Activity) {
        setFullScreenCallBacks()
        if (isLoadingEnable) {
            try {
                val adLoadingDialog = AdLoadingDialog(activity)
                adLoadingDialog.showAlertDialog()
                Handler(Looper.getMainLooper()).postDelayed({
                    try {
                        adLoadingDialog.dismissAlertDialog()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    showAppOpenAd(activity)
                }, 1 * 1000)
            } catch (e: Exception) {
                showAppOpenAd(activity)
            }
        } else {
            showAppOpenAd(activity)
        }
    }

    fun showAppOpenAd(activity: Activity){
        mAppOpenAd?.show(activity)
    }

    fun hasAd(): Boolean {
        return mAppOpenAd != null
    }


}