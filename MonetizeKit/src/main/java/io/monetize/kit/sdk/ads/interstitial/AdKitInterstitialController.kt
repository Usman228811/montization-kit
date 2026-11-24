package io.monetize.kit.sdk.ads.interstitial

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.libraries.ads.mobile.sdk.common.AdValue
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAd
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAdEventCallback
import io.monetize.kit.sdk.ads.open.AdLoadingDialog
import io.monetize.kit.sdk.ads.preload.AdKitPreloader
import io.monetize.kit.sdk.ads.preload.PreloadFormat
import io.monetize.kit.sdk.core.utils.IS_INTERSTITIAL_Ad_SHOWING
import io.monetize.kit.sdk.core.utils.appflyer.postAdImpression
import io.monetize.kit.sdk.core.utils.appflyer.revenueListener
import io.monetize.kit.sdk.core.utils.firebaseBoolean
import io.monetize.kit.sdk.core.utils.firebaseLong
import io.monetize.kit.sdk.core.utils.init.AdKit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


data class InterAdSingleModel(
    val key: String = "",
    val controller: InterstitialController? = null,
)

val singleInterList = ArrayList<InterAdSingleModel>()

//data class InterAdsConfigs(
//    val openAdEnable: Boolean,
//    val openAdInstant: Boolean = false,
//    val splashTime: Long,
//    val instantInterTime: Long = 8L,
//    val instantOpenAdTime: Long = 8L,
//    val interLoadingEnable: Boolean = false,
//    val openAdLoadingEnable: Boolean = false,
//)

class InterstitialController private constructor(
) {
    companion object {
        fun getInstance(
        ): InterstitialController {
            return InterstitialController()
        }
    }


    private var placementKey: String = ""
    private var adIdKey: String = ""
    private var adUnitId: String = ""
    private var handlerAd = Handler(Looper.getMainLooper())
    private val mainHandler = Handler(Looper.getMainLooper())
    private var canRequestAd = true
    // True while the loading dialog is up and the ad is about to be shown
    private var isShowPending = false
    private var admobInterAd: InterstitialAd? = null
    private var mInterstitialControllerListener: InterstitialControllerListener? = null
    private var adLoadingDialog: AdLoadingDialog? = null

    private val handlerAdDelay: Handler = Handler(Looper.getMainLooper())
    private var isHandlerAdDelayRunning = false
    // Cancels the instant request's wait for the next preloaded ad
    private var cancelWait: (() -> Unit)? = null

    private val runnableHandlerAdDelay = Runnable {
        cancelWait?.invoke()
        cancelWait = null
        canRequestAd = true
        if (mInterstitialControllerListener != null && isHandlerAdDelayRunning) {
            try {
                adLoadingDialog?.dismissAlertDialog()
            } catch (_: Exception) {
            }
            isHandlerAdDelayRunning = false
            mInterstitialControllerListener?.onAdClosed(
                reason = "$placementKey called onAdClosed because: instant ad time is completed"

            )
        }
    }


    private fun startDelayHandler() {
        var instantTime = firebaseLong("INTER_INSTANT_TIME", 8L)
        if (instantTime == 0L) {
            instantTime = 8L
        }
        if (!isHandlerAdDelayRunning) {
            isHandlerAdDelayRunning = true
            handlerAdDelay.postDelayed(
                runnableHandlerAdDelay, instantTime * 1000
            )
        }
    }

    fun removeCallBacksDelay() {
        try {
            isHandlerAdDelayRunning = false
            handlerAdDelay.removeCallbacks(runnableHandlerAdDelay)
        } catch (ignored: Exception) {
        }
    }

    private fun showAdmobAd(activity: Activity, key: String) {
        try {
            if (admobInterAd != null && !AdKit.interHelper.getAppInPause() && !IS_INTERSTITIAL_Ad_SHOWING) {
                mInterstitialControllerListener?.onAdShow()
                if (admobInterAd != null) {
                    setAdmobFullScreen(activity, key)
                    // Consume the ad before showing so a second tap can't show it again
                    val ad = admobInterAd
                    admobInterAd = null
                    ad?.show(activity)
                }
                if (key != "") {
                    setInterCount(key, 0)
                }
            } else {
                mInterstitialControllerListener?.onAdClosed(
                    reason = "$placementKey called onAdClosed because: App is minimized | Other Ad is showing"
                )
            }
        } catch (exception: Exception) {
            mInterstitialControllerListener?.onAdClosed(
                reason = "$placementKey called onAdClosed because: Inter Exception"

            )
        }
    }

    fun loadAndShowWithCounter(
        context: Activity,
        placementKey: String,
        adIdKey: String,
        enable: Boolean,
        listener: InterstitialControllerListener, key: String, counter: Long,
    ) {

        this.placementKey = placementKey
        this.adIdKey = adIdKey
        mInterstitialControllerListener = listener
        val savedCount = getInterCount(key)
        if (AdKit.adKitPref.isAppPurchased || !enable || AdKit.interHelper.getAppInPause() || IS_INTERSTITIAL_Ad_SHOWING) {
            listener.onAdClosed(
                reason = "$placementKey called onAdClosed because: App is minimized | Ad is disabled | Other Ad is showing | App is Purchased"

            )
        } else if (savedCount == -1 || savedCount >= counter) {
            if (takeAd()) {
                checkProgressShowAd(context, key)
            } else {
                loadAndShow(
                    context,
                    this@InterstitialController.placementKey, adIdKey, true, key, listener
                )
            }
        } else if ((savedCount + 1).toLong() >= counter) {
            listener.onAdClosed(
                reason = "$placementKey called onAdClosed because: counter is not completed"

            )
            setInterCount(key, savedCount + 1)
        } else {
            listener.onAdClosed(
                reason = "$placementKey called onAdClosed because: counter is not completed"
            )
            setInterCount(key, savedCount + 1)
        }
    }

    fun showWithoutCounter(
        context: Activity,
        placementKey: String,
        adIdKey: String,
        enable: Boolean,
        listener: InterstitialControllerListener
    ) {
        mInterstitialControllerListener = listener
        this.placementKey = placementKey
        this.adIdKey = adIdKey

        if (AdKit.adKitPref.isAppPurchased || !enable || AdKit.interHelper.getAppInPause() || IS_INTERSTITIAL_Ad_SHOWING) {
            listener.onAdClosed(
                reason = "$placementKey called onAdClosed because: App is minimized | Ad is disabled | Other Ad is showing | App is Purchased"
            )
        } else {
            if (takeAd()) {
                checkProgressShowAd(context)
            } else {
                listener.onAdClosed(
                    reason = "$placementKey called onAdClosed because: Ad is null"
                )
                loadInterIfNoAd(context)
            }
        }
    }

    fun showWithCounter(
        context: Activity,
        placementKey: String,
        adIdKey: String,
        enable: Boolean,
        listener: InterstitialControllerListener, key: String, counter: Long,
    ) {
        mInterstitialControllerListener = listener
        this.placementKey = placementKey
        this.adIdKey = adIdKey

        val savedCount = getInterCount(key)
        if (AdKit.adKitPref.isAppPurchased || !enable || AdKit.interHelper.getAppInPause() || IS_INTERSTITIAL_Ad_SHOWING) {
            listener.onAdClosed(
                reason = "$placementKey called onAdClosed because: App is minimized | Ad is disabled | Other Ad is showing | App is Purchased"
            )
        } else if (savedCount == -1 || savedCount >= counter) {
            if (takeAd()) {
                checkProgressShowAd(context, key)
            } else {

                if ((savedCount + 2).toLong() >= counter) {
                    loadInterIfNoAd(context)
                }

                if (savedCount == -1) {
                    setInterCount(key, 1)
                }
                listener.onAdClosed(
                    reason = "$placementKey called onAdClosed because: counter is complete but no ad is available"
                )

            }
        } else if ((savedCount + 2).toLong() >= counter) {
            listener.onAdClosed(
                reason = "$placementKey called onAdClosed because: counter is not completed"
            )
            loadInterIfNoAd(context)
            setInterCount(key, savedCount + 1)
        } else {
            listener.onAdClosed(
                reason = "$placementKey called onAdClosed because: counter is not completed"
            )
            setInterCount(key, savedCount + 1)
        }
    }

    private fun initAdMobCounter(context: Context, key: String, counter: Long) {
        val canLoad = AdKit.internetController.isConnected && !AdKit.adKitPref.isAppPurchased
        if (/*AdKit.consentManager.canRequestAds &&*/ canLoad) {
            val savedCount = getInterCount(key)
            if (savedCount == -1 || savedCount >= counter) {
                loadInterIfNoAd(context)
            }
        }
    }

    fun preLoadInter(
        context: Context,
        placementKey: String,
        adIdKey: String,
        enable: Boolean, counterKey: String = "", counter: Long = -1
    ) {
        if (!enable) {
            return
        }
        this.placementKey = placementKey
        this.adIdKey = adIdKey
        if (admobInterAd != null) {
            return
        }
        val isForCounter = counterKey.isNotEmpty() && counter.toInt() != -1
        if (isForCounter) {
            initAdMobCounter(context, counterKey, counter)
        } else {
            val canLoad = AdKit.internetController.isConnected && !AdKit.adKitPref.isAppPurchased
            if (/*AdKit.consentManager.canRequestAds &&*/ canLoad) {
                loadInterIfNoAd(context)
            }
        }
    }

    // Starts the SDK preloader for every ad unit of the ad ID key; it keeps them filled from then on
    private fun loadInter(context: Context) {
        if (AdKit.adKitPref.isAppPurchased) return
        AdKitPreloader.start(PreloadFormat.INTERSTITIAL, AdKit.interIdManager.getInterIds(adIdKey))
    }

    // Counter placements: two clicks before the counter completes, start preloading only if no ad is
    // already waiting (held, or in the preloader of any ad unit of the ad ID key)
    private fun loadInterIfNoAd(context: Context) {
        if (admobInterAd != null) return
        if (AdKitPreloader.isAvailable(PreloadFormat.INTERSTITIAL, AdKit.interIdManager.getInterIds(adIdKey))) return
        loadInter(context)
    }

    // Takes a preloaded ad (next ad unit in rotation first) unless one is already held
    private fun takeAd(): Boolean {
        if (admobInterAd != null) return true
        val ids = AdKit.interIdManager.getInterIds(adIdKey)
        val (id, ad) = AdKitPreloader.pollInterstitial(ids.size) { AdKit.interIdManager.getNextInterId(adIdKey) }
            ?: return false
        adUnitId = id
        admobInterAd = ad
        return true
    }


    fun loadAndShow(
        context: Activity,
        placementKey: String,
        adIdKey: String,
        enable: Boolean = true,
        key: String = "",
        listener: InterstitialControllerListener,
    ) {

        this.placementKey = placementKey
        this.adIdKey = adIdKey
        mInterstitialControllerListener = listener
        try {
            if (!AdKit.adKitPref.isAppPurchased && AdKit.internetController.isConnected && enable /*&& AdKit.consentManager.canRequestAds*/) {
                if (!canRequestAd) {
                    mInterstitialControllerListener?.onAdClosed(
                        reason = "$placementKey called onAdClosed because: Other ad is being request"
                    )
                    return
                }
                if (takeAd()) {
                    Log.d("usman", "loadAndShow: Ad taken successfully")
                    checkProgressShowAd(context, key)
                } else {
                    Log.d("usman", "loadAndShow: Ad load instant successfully")
                    val ids = AdKit.interIdManager.getInterIds(adIdKey)
                    if (ids.isEmpty()) {
                        mInterstitialControllerListener?.onAdClosed(
                            reason = "$placementKey called onAdClosed because: no ad unit ID for $adIdKey"
                        )
                        return
                    }
                    canRequestAd = false
                    dismissLoadingDialog()
                    adLoadingDialog = AdLoadingDialog(context)
                    adLoadingDialog?.showAlertDialog()
                    startDelayHandler()
                    // Wait (up to INTER_INSTANT_TIME) for the preloader's next ad, then show it
                    cancelWait = AdKitPreloader.awaitNext(PreloadFormat.INTERSTITIAL, ids) { loaded, reason ->
                        cancelWait = null
                        canRequestAd = true
                        if (!isHandlerAdDelayRunning) return@awaitNext
                        if (loaded && takeAd()) {
                            dismissLoadingDialog()
                            removeCallBacksDelay()
                            showAdmobAd(context, key)
                        } else {
                            handlerRemoveCallback(reason = "$placementKey $reason")
                        }
                    }
                    AdKitPreloader.start(PreloadFormat.INTERSTITIAL, ids)
                }

            } else {
                mInterstitialControllerListener?.onAdClosed(
                    reason = "$placementKey called onAdClosed because: App is minimized | Ad is disabled | Other Ad is showing | App is Purchased"
                )
            }
        } catch (e: Exception) {
            canRequestAd = true
            handlerRemoveCallback("$placementKey called onAdClosed because: Inter Exception")
        } catch (e: OutOfMemoryError) {
            canRequestAd = true
            handlerRemoveCallback("$placementKey called onAdClosed because: Inter Exception")
        }
    }

    private fun handlerRemoveCallback(reason: String) {
        if (isHandlerAdDelayRunning) {
            dismissLoadingDialog()
            removeCallBacksDelay()
            mInterstitialControllerListener?.onAdClosed(
                reason = reason
            )
        }
    }


    private fun dismissLoadingDialog() {
        try {
            adLoadingDialog?.dismissAlertDialog()
        } catch (_: Exception) {
        }
    }

    private fun checkProgressShowAd(
        activity: Activity, key: String = "",
    ) {
        // A show is already scheduled (e.g. double tap); it reports to the latest listener,
        // so a second show would only show the ad twice or fail
        if (isShowPending) return
        if (firebaseBoolean("INTER_LOADING_ENABLE", false)) {
            try {
                // onAdShow is sent by showAdmobAd when the ad is actually shown
                val adLoadingDialog = AdLoadingDialog(activity)
                adLoadingDialog.showAlertDialog()
                isShowPending = true
                handlerAd.postDelayed({
                    isShowPending = false
                    if (activity.isFinishing || activity.isDestroyed) {
                        // The screen closed during the delay; don't show the ad over the next screen
                        mInterstitialControllerListener?.onAdClosed(
                            reason = "$placementKey called onAdClosed because: screen was closed before the ad was shown"
                        )
                    } else {
                        showAdmobAd(activity, key)
                    }
                    adLoadingDialog.dismissAlertDialog()
                }, 1000)
            } catch (e: Exception) {
                isShowPending = false
                showAdmobAd(activity, key)
            }
        } else {
            showAdmobAd(activity, key)
        }
    }


    private fun setAdmobFullScreen(activity: Activity, key: String) {

        admobInterAd?.adEventCallback =
            object : InterstitialAdEventCallback {

                override fun onAdPaid(value: AdValue) {
                    super.onAdPaid(value)
                    revenueListener(adUnitId, value, "INTERSTITIAL")
                }
                override fun onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent()
                    mainHandler.post {
                        dismissLoadingDialog()
                        // Reset before notifying, so an ad requested from inside onAdClosed
                        // isn't rejected as "Other Ad is showing"
                        IS_INTERSTITIAL_Ad_SHOWING = false
                        admobInterAd = null
                        mInterstitialControllerListener?.onAdClosed(
                            isInterShowed = true,
                            reason = "$placementKey called onAdClosed because: ad is showed successfully"
                        )
                        if (key.isEmpty() && !firebaseBoolean(
                                "${placementKey}_isInterInstant",
                                false
                            )
                        ) {
//                            loadInter(activity)
                        }
                    }
                }

                override fun onAdImpression() {
                    super.onAdImpression()
                    postAdImpression("InterstitialAd")
                }

                override fun onAdShowedFullScreenContent() {
                    super.onAdShowedFullScreenContent()
                    IS_INTERSTITIAL_Ad_SHOWING = true
                    admobInterAd = null
                }

                override fun onAdFailedToShowFullScreenContent(fullScreenContentError: FullScreenContentError) {
                    super.onAdFailedToShowFullScreenContent(fullScreenContentError)
                    mainHandler.post {
                        dismissLoadingDialog()
                        // Reset before notifying, so the caller can show another ad from onAdClosed
                        admobInterAd = null
                        IS_INTERSTITIAL_Ad_SHOWING = false
                        mInterstitialControllerListener?.onAdClosed(
                            reason = "$placementKey called onAdClosed because: onAdFailedToShowFullScreenContent code: ${fullScreenContentError.code} message: ${fullScreenContentError.message}"
                        )
                    }
                }

            }
    }

    private fun getInterCount(key: String, defValue: Int = 0): Int {
        return AdKit.adKitPref.getInterInt(key, defValue)
    }

    private fun setInterCount(key: String = "APP_INTER_COUNTER", count: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            AdKit.adKitPref.putInterInt(key, count)
        }
    }


}