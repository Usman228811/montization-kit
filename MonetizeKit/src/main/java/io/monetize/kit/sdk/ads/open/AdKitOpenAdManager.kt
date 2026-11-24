package io.monetize.kit.sdk.ads.open

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAd
import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdActivity
import com.google.android.libraries.ads.mobile.sdk.common.AdValue
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import io.monetize.kit.sdk.ads.preload.AdKitPreloader
import io.monetize.kit.sdk.ads.preload.PreloadFormat
import io.monetize.kit.sdk.core.utils.IS_INTERSTITIAL_Ad_SHOWING
import io.monetize.kit.sdk.core.utils.IS_OPEN_Ad_SHOWING
import io.monetize.kit.sdk.core.utils.REMOTE_OPEN_AD_ID_KEY
import io.monetize.kit.sdk.core.utils.appflyer.postAdImpression
import io.monetize.kit.sdk.core.utils.appflyer.revenueListener
import io.monetize.kit.sdk.core.utils.firebaseBoolean
import io.monetize.kit.sdk.core.utils.firebaseLong
import io.monetize.kit.sdk.core.utils.firebaseString
import io.monetize.kit.sdk.core.utils.init.AdKit
import io.monetize.kit.sdk.core.utils.init.AdKit.adKitPref
import io.monetize.kit.sdk.core.utils.init.AdKit.internetController
import java.util.Date

interface OpenAdListener {
    fun onAdShow()
    fun onAdLoaded()
    fun onAdDismissed()
    fun onAdFailed(error: String)
}

class AdKitOpenAdManager private constructor(
    private val mContext: Context,
) : DefaultLifecycleObserver {


    private var mAppOpenAd: AppOpenAd? = null
    private var openAdListener: OpenAdListener? = null
    private var loadTime: Long = 0
    private var canRequestAd = true

    private var isAdEnable = true
    private var isPause = false
    private var isLoadingEnable = true
    private var localAdId = ""

    // Remote config "OPEN_AD_ID" overrides the ID passed to AdKit.init
    private val adId: String
        get() = firebaseString(REMOTE_OPEN_AD_ID_KEY, "").trim().ifEmpty { localAdId }

    private var currentActivity: Activity? = null

    private var currentRoute: String? = null

    private var canShowOpenAd = true
    private var isOpenAdInstant = false

    private var adLoadingDialog: AdLoadingDialog? = null

    private val handlerAdDelay: Handler = Handler(Looper.getMainLooper())
    private var isHandlerAdDelayRunning = false

    // Cancels the instant open ad's wait for the next preloaded ad
    private var cancelWait: (() -> Unit)? = null

    private val runnableHandlerAdDelay = Runnable {
        cancelWait?.invoke()
        cancelWait = null
        canRequestAd = true
        if (isHandlerAdDelayRunning) {
            try {
                adLoadingDialog?.dismissAlertDialog()
            } catch (_: Exception) {
            }
            isHandlerAdDelayRunning = false
        }
    }

    fun setOpenAdListeners(openAdListener: OpenAdListener) {
        this.openAdListener = openAdListener
    }


    fun canShowOpenAd(canShowOpenAd: Boolean) {
        this.canShowOpenAd = canShowOpenAd
    }

    companion object {
        @Volatile
        private var instance: AdKitOpenAdManager? = null


        internal fun getInstance(
            context: Application,
        ): AdKitOpenAdManager {
            return instance ?: synchronized(this) {
                instance ?: AdKitOpenAdManager(
                    context,
                ).also { instance = it }
            }
        }

    }

    fun setCurrentNavigationRoute(route: String?) {
        currentRoute = route
    }

    private val excludedActivities = mutableSetOf<String>()
    private val excludedComposeRoutes = mutableSetOf<String>()

    fun excludeNavigationRoutesFromOpenAd(vararg routes: String) {
        excludedComposeRoutes.addAll(routes)
    }

    fun excludeActivitiesFromOpenAd(vararg activityClasses: Class<out Activity>) {
        excludedActivities.addAll(activityClasses.map { it.name })
    }

    fun setAppInPause(isPause: Boolean) {
        this.isPause = isPause
    }


    fun setOpenAdId(adId: String) {
        this.localAdId = adId
    }

    fun setOpenAdConfigs() {
        this@AdKitOpenAdManager.isOpenAdInstant = firebaseBoolean("IS_OPEN_AD_INSTANT", false)
        this@AdKitOpenAdManager.isAdEnable = firebaseBoolean("OPEN_AD_ENABLE", false)
        this@AdKitOpenAdManager.isLoadingEnable = firebaseBoolean("OPEN_AD_LOADING_ENABLE", false)
    }

    fun initOpenAd() {
        // The preloader tells when an open ad is ready, for OpenAdListener.onAdLoaded
        AdKitPreloader.setOnPreloaded(PreloadFormat.APP_OPEN) { openAdListener?.onAdLoaded() }
        try {
            ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        isPause = false

        if (AdKit.initializer.getDisableAds()) {
            return
        }

        if (isOpenAdInstant.not()) {
            showAd()
        } else {
            loadAndShowOpenAd()
        }
    }

    private fun showAd() {
        try {
            if (!adKitPref.isAppPurchased && isAdEnable) {
                showAdIfAvailable(true)
            }
        } catch (ignored: Exception) {
        }
    }

    fun setActivity(activity: Activity?) {
        currentActivity = activity
    }

    private fun startDelayHandler() {
        var instantTime = firebaseLong("OPEN_AD_INSTANT_TIME", 8)
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


    private fun loadAndShowOpenAd() {

        if (isAdAvailable) {
            showAdIfAvailable(true)
        } else {
            if (!IS_INTERSTITIAL_Ad_SHOWING && !IS_OPEN_Ad_SHOWING && !isPause && !adKitPref.isAppPurchased && AdKit.internetController.isConnected) {
                if (!canRequestAd) {
                    return
                }
                val isExcludedRoute = currentRoute in excludedComposeRoutes
                val isExcludedActivity =
                    currentActivity?.javaClass?.name in excludedActivities

                if (canShowOpenAd && !isExcludedRoute && !isExcludedActivity && currentActivity !is AdActivity) {
                    currentActivity?.let { activity ->

                        if (adId.isNotEmpty() && isAdEnable && isOpenAdInstant /*&& AdKit.consentManager.canRequestAds*/) {
                            canRequestAd = false
                            adLoadingDialog = AdLoadingDialog(activity)
                            adLoadingDialog?.showAlertDialog()
                            startDelayHandler()
                            Log.d("AdKit_Logs", "instant open ad called")

                            // Wait (up to OPEN_AD_INSTANT_TIME) for the preloader's next ad, then show it
                            cancelWait = AdKitPreloader.awaitNext(
                                PreloadFormat.APP_OPEN,
                                listOf(adId)
                            ) { loaded, reason ->
                                cancelWait = null
                                canRequestAd = true
                                Log.d(
                                    "AdKit_Logs",
                                    "instant open ad ${if (loaded) "loaded" else "failed"}"
                                )
                                if (!loaded) {
                                    openAdListener?.onAdFailed("OpenAd is failed: $reason")
                                    if (isHandlerAdDelayRunning) {
                                        dismissLoadingDialog()
                                        removeCallBacksDelay()
                                    }
                                    return@awaitNext
                                }
                                if (isHandlerAdDelayRunning) {
                                    dismissLoadingDialog()
                                    removeCallBacksDelay()
                                    takeOpenAd()
                                    setFullScreenCallBacks(activity)
                                    showAdIfAvailable(false)
                                }
                            }
                            AdKitPreloader.start(PreloadFormat.APP_OPEN, listOf(adId))

//                            AppOpenAd.load(
//                                mContext,
//                                adId,
//                                AdRequest.Builder().build(),
//                                object : AppOpenAdLoadCallback() {
//                                    override fun onAdLoaded(appOpenAd: AppOpenAd) {
//                                        super.onAdLoaded(appOpenAd)
//                                        Log.d("AdKit_Logs", "instant open ad loaded")
//                                        canRequestAd = true
//                                        mAppOpenAd = appOpenAd
//                                        mAppOpenAd?.revenueListener(adId)
//                                        openAdListener?.onAdLoaded()
//
//                                        setFullScreenCallBacks()
//                                        loadTime = Date().time
//
//                                        if (isHandlerAdDelayRunning) {
//                                            dismissLoadingDialog()
//                                            removeCallBacksDelay()
//                                            showAdIfAvailable(false)
//
//                                        }
//                                    }
//
//                                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
//                                        super.onAdFailedToLoad(loadAdError)
//
//                                        Log.d("AdKit_Logs", "instant open ad failed")
//
//                                        canRequestAd = true
//                                        mAppOpenAd = null
//                                        openAdListener?.onAdFailed("OpenAd is failed with code: ${loadAdError.code}, message: ${loadAdError.message}")
//
//
//                                        if (isHandlerAdDelayRunning) {
//                                            dismissLoadingDialog()
//                                            removeCallBacksDelay()
//                                        }
//                                    }
//                                })

                        }

                    }
                }
            }
        }
    }

    fun setFullScreenCallBacks(activity: Activity) {
        mAppOpenAd?.adEventCallback =
            object : AppOpenAdEventCallback {

                override fun onAdPaid(value: AdValue) {
                    super.onAdPaid(value)
                    revenueListener(adId, adValue = value, "APP_OPEN")
                }

                override fun onAdDismissedFullScreenContent() {
                    activity.runOnUiThread {
                        mAppOpenAd = null
                        IS_OPEN_Ad_SHOWING = false
                        openAdListener?.onAdDismissed()
                        if (isOpenAdInstant.not()) {
//                            preloadOpenAd()
                        }
                    }
                }

                override fun onAdImpression() {
                    super.onAdImpression()
                    activity.runOnUiThread {
                        postAdImpression("AppOpenAd")
                    }
                }

                override fun onAdFailedToShowFullScreenContent(
                    fullScreenContentError: FullScreenContentError
                ) {
                    activity.runOnUiThread {
                        IS_OPEN_Ad_SHOWING = false
                    }
                }

                override fun onAdShowedFullScreenContent() {
                    activity.runOnUiThread {
                        IS_OPEN_Ad_SHOWING = true
                        openAdListener?.onAdShow()
                    }
                }
            }
    }

    fun removeCallBacksDelay() {
        try {
            isHandlerAdDelayRunning = false
            handlerAdDelay.removeCallbacks(runnableHandlerAdDelay)
        } catch (ignored: Exception) {
        }
    }

    private fun dismissLoadingDialog() {
        try {
            adLoadingDialog?.dismissAlertDialog()
        } catch (_: Exception) {
        }
    }

    private fun preloadOpenAd() {

        if (adId.isNotEmpty() && isAdEnable && !isOpenAdInstant) {
            if (isAdAvailable || !internetController.isConnected || adKitPref.isAppPurchased || isPause) {
                return
            }
            Log.d("AdKit_Logs", "preload open ad called")
            AdKitPreloader.start(PreloadFormat.APP_OPEN, listOf(adId))

//            AppOpenAd.load(
//                mContext,
//                adId,
//                AdRequest.Builder().build(),
//                object : AppOpenAdLoadCallback() {
//                    override fun onAdLoaded(appOpenAd: AppOpenAd) {
//                        super.onAdLoaded(appOpenAd)
//
//                        Log.d("AdKit_Logs", "preload open ad loaded")
//                        canRequestAd = true
//                        mAppOpenAd = appOpenAd
//                        mAppOpenAd?.revenueListener(adId)
//                        openAdListener?.onAdLoaded()
//
//
//                        loadTime = Date().time
//                    }
//
//                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
//                        super.onAdFailedToLoad(loadAdError)
//
//
//                        Log.d("AdKit_Logs", "preload open ad failed")
//                        canRequestAd = true
//                        mAppOpenAd = null
//                    }
//                })
        }
    }

    private fun showAdIfAvailable(showWithProgress: Boolean) {
        try {
            if (!IS_INTERSTITIAL_Ad_SHOWING) {
                if (!IS_OPEN_Ad_SHOWING && isAdAvailable) {
                    if (!isPause) {
                        val isExcludedRoute = currentRoute in excludedComposeRoutes
                        val isExcludedActivity =
                            currentActivity?.javaClass?.name in excludedActivities

                        if (canShowOpenAd && !isExcludedRoute && !isExcludedActivity && currentActivity !is AdActivity) {
                            currentActivity?.let {
                                takeOpenAd()
                                Log.d("AdKit_Logs", "open ad progress $showWithProgress")
                                if (showWithProgress) {
                                    setFullScreenCallBacks(it)
                                    checkProgressShowAd(it)
                                } else {
                                    showAppOpenAd(it)
                                }

                            }
                        }
                    }
                } else {
                    preloadOpenAd()
                }
            }
        } catch (ignored: Exception) {
        }
    }

    // True while the loading dialog is up and the ad is about to be shown
    private var isShowPending = false

    private fun checkProgressShowAd(activity: Activity) {
        // A show is already scheduled (e.g. the app came back to the foreground twice quickly)
        if (isShowPending) return
        if (isLoadingEnable) {
            try {
                val adLoadingDialog = AdLoadingDialog(activity)
                adLoadingDialog.showAlertDialog()
                isShowPending = true
                Handler(Looper.getMainLooper()).postDelayed({
                    isShowPending = false
                    try {
                        adLoadingDialog.dismissAlertDialog()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    // The user left during the delay: keep the ad for the next time the app opens
                    if (!isPause && !activity.isFinishing && !activity.isDestroyed) {
                        showAppOpenAd(activity)
                    }
                }, 1 * 1000)
            } catch (e: Exception) {
                isShowPending = false
                showAppOpenAd(activity)
            }
        } else {
            showAppOpenAd(activity)
        }
    }

    fun showAppOpenAd(activity: Activity) {
        takeOpenAd()
        // Always bind callbacks right before show so no show path misses impression/revenue
        setFullScreenCallBacks(activity)
        mAppOpenAd?.show(activity)
    }

    private fun wasLoadTimeLessThanNHoursAgo(): Boolean {
        val dateDifference = Date().time - loadTime
        val numMilliSecondsPerHour: Long = 3600000
        return dateDifference < numMilliSecondsPerHour * 4L
    }

    // Takes the preloaded ad when none is held (or the held one is too old)
    private fun takeOpenAd() {
        if (mAppOpenAd != null && wasLoadTimeLessThanNHoursAgo()) return
        AdKitPreloader.pollAppOpen(adId)?.let {
            mAppOpenAd = it
            loadTime = Date().time
        }
    }

    // A held ad, or one waiting in the preloader (which replaces expired ads itself)
    private val isAdAvailable: Boolean
        get() = (mAppOpenAd != null && wasLoadTimeLessThanNHoursAgo()) ||
                (adId.isNotEmpty() && AdKitPreloader.isAvailable(
                    PreloadFormat.APP_OPEN,
                    listOf(adId)
                ))
}