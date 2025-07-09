package io.monetize.kit.sdk.ads.open

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.AdActivity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback
import io.monetize.kit.sdk.core.utils.IS_INTERSTITIAL_Ad_SHOWING
import io.monetize.kit.sdk.core.utils.IS_OPEN_Ad_SHOWING
import io.monetize.kit.sdk.core.utils.init.AdKit.adKitPref
import io.monetize.kit.sdk.core.utils.init.AdKit.internetController
import java.util.Date

class AdKitOpenAdManager private constructor(
    context: Context,
) : DefaultLifecycleObserver {

    private val mContext = context.applicationContext

    private var mAppOpenAd: AppOpenAd? = null
    private var loadTime: Long = 0
    private var canRequestAd = true

    private var isAdEnable = true
    private var isPause = false
    private var isLoadingEnable = true
    private var canShowOpenAd = true
    private var adId = ""
    private var currentActivity: Activity? = null

    private var currentRoute: String? = null

    companion object {
        @Volatile
        private var instance: AdKitOpenAdManager? = null


        internal fun getInstance(
            context: Context,
        ): AdKitOpenAdManager {
            return instance ?: synchronized(this) {
                instance ?: AdKitOpenAdManager(
                    context,

                    ).also { instance = it }
            }
        }

    }

    fun setCurrentComposeRoute(route: String?) {
        currentRoute = route
    }

    private val excludedActivities = mutableSetOf<String>()
    private val excludedComposeRoutes = mutableSetOf<String>()

    fun excludeComposeRoutesFromOpenAd(vararg routes: String) {
        excludedComposeRoutes.addAll(routes)
    }

    fun excludeActivitiesFromOpenAd(vararg activityClasses: Class<out Activity>) {
        excludedActivities.addAll(activityClasses.map { it.name })
    }

    fun setAppInPause(isPause: Boolean) {
        this.isPause = isPause
    }

    fun setCanShowOpenAd(canShowOpenAd: Boolean) {
        this.canShowOpenAd = canShowOpenAd
    }

    fun setOpenAdConfigs(
        adId: String,
        isAdEnable: Boolean,
        isLoadingEnable: Boolean,
    ) {
        this.isAdEnable = isAdEnable
        this.isLoadingEnable = isLoadingEnable
        this.adId = adId
    }

    fun initOpenAd() {
        try {
            ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        isPause = false
        showAd()
    }

    private fun showAd() {
        try {
            if (canShowOpenAd && !adKitPref.isAppPurchased && isAdEnable) {
                showAdIfAvailable()
            }
        } catch (ignored: Exception) {
        }
    }

    fun setActivity(activity: Activity?) {
        currentActivity = activity
    }

    private fun fetchAd() {

        if (adId.isNotEmpty()) {
            // Have unused ad, no need to fetch another.
            if (isAdAvailable || !internetController.isConnected || adKitPref.isAppPurchased || isPause) {
                return
            }
            if (!canRequestAd) {
                return
            }
            canRequestAd = false


            AppOpenAd.load(
                mContext,
                adId,
                AdRequest.Builder().build(),
                object : AppOpenAdLoadCallback() {
                    override fun onAdLoaded(appOpenAd: AppOpenAd) {
                        super.onAdLoaded(appOpenAd)
                        canRequestAd = true
//                    if (BuildConfig.DEBUG) {
//                        showToast(appClass, "Open Ad Loaded")
//                    }
                        mAppOpenAd = appOpenAd
                        mAppOpenAd?.fullScreenContentCallback =
                            object : FullScreenContentCallback() {
                                override fun onAdDismissedFullScreenContent() {
                                    mAppOpenAd = null
                                    IS_OPEN_Ad_SHOWING = false
                                    fetchAd()
                                }

                                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                    IS_OPEN_Ad_SHOWING = false
                                }

                                override fun onAdShowedFullScreenContent() {
                                    IS_OPEN_Ad_SHOWING = true
                                }
                            }
                        loadTime = Date().time
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        super.onAdFailedToLoad(loadAdError)
                        canRequestAd = true
                        mAppOpenAd = null
//                    if (BuildConfig.DEBUG) {
//                        showToast(appClass, "Open Ad failed")
//                    }
                    }
                })
        }
    }

    private fun showAdIfAvailable() {
        try {
            if (!IS_INTERSTITIAL_Ad_SHOWING) {
                if (!IS_OPEN_Ad_SHOWING && isAdAvailable) {
                    if (!isPause) {
                        Log.d("AdKit_Logs", "currentRoute: $currentRoute")

                        val isExcludedRoute = currentRoute in excludedComposeRoutes
                        val isExcludedActivity =
                            currentActivity?.javaClass?.name in excludedActivities

                        if (!isExcludedRoute && !isExcludedActivity && currentActivity !is AdActivity) {
                            currentActivity?.let { checkProgressShowAd(it) }
                        }

//                        currentActivity?.let { currentActivity ->
//                            val className = currentActivity::class.java.name
//                            if (currentActivity !is AdActivity && className !in excludedActivities) {
//                                checkProgressShowAd(currentActivity)
//                            }
//                        }
                    }
                } else {
                    fetchAd()
                }
            }
        } catch (ignored: Exception) {
        }
    }

    private fun checkProgressShowAd(activity: Activity) {
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
                    mAppOpenAd?.show(activity)
                }, 1 * 1000)
            } catch (e: Exception) {
                mAppOpenAd?.show(activity)
            }
        } else {
            mAppOpenAd?.show(activity)
        }
    }

    private fun wasLoadTimeLessThanNHoursAgo(): Boolean {
        val dateDifference = Date().time - loadTime
        val numMilliSecondsPerHour: Long = 3600000
        return dateDifference < numMilliSecondsPerHour * 4L
    }

    private val isAdAvailable: Boolean
        get() = mAppOpenAd != null && wasLoadTimeLessThanNHoursAgo()
}