package io.monetize.kit.sdk.ads.open
//
//import android.app.Activity
//import android.os.Handler
//import android.os.Looper
//import androidx.lifecycle.DefaultLifecycleObserver
//import androidx.lifecycle.LifecycleOwner
//import androidx.lifecycle.ProcessLifecycleOwner
//import com.google.android.gms.ads.AdError
//import com.google.android.gms.ads.AdRequest
//import com.google.android.gms.ads.FullScreenContentCallback
//import com.google.android.gms.ads.LoadAdError
//import com.google.android.gms.ads.appopen.AppOpenAd
//import com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback
//import com.test.compose.setup.AppClass
//
//import com.test.compose.setup.core.utils.OPEN_AD_LOADING_ENABLE
//import com.test.compose.setup.core.utils.INTER_PROGRESS_TIME
//import com.test.compose.setup.core.utils.IS_APP_PAUSE
//import com.test.compose.setup.core.utils.IS_INTERSTITIAL_Ad_SHOWING
//import com.test.compose.setup.core.utils.IS_OPEN_Ad_SHOWING
//import com.test.compose.setup.core.utils.InternetController
//import com.test.compose.setup.core.utils.OPEN_AD_ENABLE
//import com.test.compose.setup.core.utils.PrankSoundAppPrefs
//import io.monetize.kit.sdk.ads.open.AdLoadingDialog
//import com.test.compose.setup.core.utils.canShowOpenAd
//import java.util.Date
//
//class AppOpenManager(
//    private val internetController: InternetController,
//    private val prefHelper: PrankSoundAppPrefs
//) : DefaultLifecycleObserver {
//    private var mAppOpenAd: AppOpenAd? = null
//    private var loadTime: Long = 0
//    private var canRequestAd = true
//    private lateinit var appClass: AppClass
//
//    fun initOpenAd(appClass: AppClass) {
//        this.appClass = appClass
//        try {
//            ProcessLifecycleOwner.get().lifecycle.addObserver(this)
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }
//
//    override fun onStart(owner: LifecycleOwner) {
//        super.onStart(owner)
//        if (::appClass.isInitialized) {
//            IS_APP_PAUSE = false
//            showAd()
//        }
//    }
//
//    private fun showAd() {
//        try {
//            if (canShowOpenAd && appClass.currentActivity != null && !prefHelper.isAppPurchased && OPEN_AD_ENABLE) {
//                showAdIfAvailable()
//            }
//        } catch (ignored: Exception) {
//        }
//    }
//
//    private fun fetchAd() {
//        // Have unused ad, no need to fetch another.
//        if (isAdAvailable || !internetController.isConnected || prefHelper.isAppPurchased || IS_APP_PAUSE) {
//            return
//        }
//        if (!canRequestAd) {
//            return
//        }
//        canRequestAd = false
////        if (BuildConfig.DEBUG) {
////            showToast(appClass, "Open Ad Called")
////        }
//        AppOpenAd.load(
//            appClass,
//            appClass.getString(R.string.app_open_ads),
//            AdRequest.Builder().build(),
//            object : AppOpenAdLoadCallback() {
//                override fun onAdLoaded(appOpenAd: AppOpenAd) {
//                    super.onAdLoaded(appOpenAd)
//                    canRequestAd = true
////                    if (BuildConfig.DEBUG) {
////                        showToast(appClass, "Open Ad Loaded")
////                    }
//                    mAppOpenAd = appOpenAd
//                    mAppOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
//                        override fun onAdDismissedFullScreenContent() {
//                            mAppOpenAd = null
//                            IS_OPEN_Ad_SHOWING = false
//                            fetchAd()
//                        }
//
//                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
//                            IS_OPEN_Ad_SHOWING = false
//                        }
//
//                        override fun onAdShowedFullScreenContent() {
//                            IS_OPEN_Ad_SHOWING = true
//                        }
//                    }
//                    loadTime = Date().time
//                }
//
//                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
//                    super.onAdFailedToLoad(loadAdError)
//                    canRequestAd = true
//                    mAppOpenAd = null
////                    if (BuildConfig.DEBUG) {
////                        showToast(appClass, "Open Ad failed")
////                    }
//                }
//            })
//    }
//
//    private fun showAdIfAvailable() {
//        try {
//            if (!IS_INTERSTITIAL_Ad_SHOWING) {
//                if (!IS_OPEN_Ad_SHOWING && isAdAvailable) {
//                    if (!IS_APP_PAUSE) {
//                        appClass.currentActivity?.let { currentActivity ->
//                            checkProgressShowAd(currentActivity)
//                        }
//                    }
//                } else {
//                    fetchAd()
//                }
//            }
//        } catch (ignored: Exception) {
//        }
//    }
//
//    private fun checkProgressShowAd(activity: Activity) {
//        if (OPEN_AD_LOADING_ENABLE) {
//            try {
//                val adLoadingDialog = AdLoadingDialog(activity)
//                adLoadingDialog.showAlertDialog()
//                Handler(Looper.getMainLooper()).postDelayed({
//                    try {
//                        adLoadingDialog.dismissAlertDialog()
//                    } catch (e: Exception) {
//                        e.printStackTrace()
//                    }
//                    mAppOpenAd?.show(activity)
//                }, INTER_PROGRESS_TIME * 1000)
//            } catch (e: Exception) {
//                mAppOpenAd?.show(activity)
//            }
//        } else {
//            mAppOpenAd?.show(activity)
//        }
//    }
//
//    private fun wasLoadTimeLessThanNHoursAgo(): Boolean {
//        val dateDifference = Date().time - loadTime
//        val numMilliSecondsPerHour: Long = 3600000
//        return dateDifference < numMilliSecondsPerHour * 4L
//    }
//
//    private val isAdAvailable: Boolean
//        get() = mAppOpenAd != null && wasLoadTimeLessThanNHoursAgo()
//}