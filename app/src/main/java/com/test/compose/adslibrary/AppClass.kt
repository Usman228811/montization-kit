package com.test.compose.adslibrary

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.content.Context
import android.os.Bundle
import android.util.Log
import com.test.compose.adslibrary.navigation.AppRoute
import io.monetize.kit.sdk.ads.open.OpenAdListener
import io.monetize.kit.sdk.core.utils.adtype.BannerAdType
import io.monetize.kit.sdk.core.utils.adtype.NativeAdType
import io.monetize.kit.sdk.core.utils.init.AdKit

class AppClass : Application(), ActivityLifecycleCallbacks {


    companion object {
        var appContext: Context? = null
    }

    override fun onCreate() {
        super.onCreate()
        appContext = this
        val start = System.currentTimeMillis()
        AdKit.init(
            isDebug = true,
            appFlyerSdkKey = "",
            postRevenueOnFireBase = true,
            context = this,
            admobId = getString(R.string.app_id),
            openAdId = "/21775744923/example/app-open",
            mapOfInterIds = mapOf(
//                "splash_inter" to "/21775744923/example/interstitial",
                "splash_inter" to "/21775744923/example/app-open",
                "home_inter" to "/21775744923/example/interstitial",
                "inter_common" to listOf(
                    "/21775744923/example/interstitial",
                    "/21775744923/example/interstitial",
                    "/21775744923/example/interstitial"
                )
            ),
            mapOfRewardIds = mapOf(
                "reward_main" to "ca-app-pub-3940256099942544/5224354917"
            ),
            mapOfNativeIds = mapOf(
                "exit_native" to "/21775744923/example/native-video",
                "lang_native_ad" to "/21775744923/example/native-video",
                "home_native" to "/21775744923/example/native-video",
                "native_common" to listOf(
                    "ca-app-pub-3940256099942544/2247696110",
                ),
                "subscription_native" to "/21775744923/example/native-video",
            ),
            mapOfBannerIds = mapOf(
                //   ca-app-pub-3940256099942544/9214589741  banner
                //   ca-app-pub-3940256099942544/2014213617  collapsible


                "premium_banner" to "ca-app-pub-3940256099942544/9214589741",
//                "home_banner" to "ca-app-pub-3940256099942544/9214589741",
//                "home_banner" to "ca-app-pub-3940256099942544/2014213617",
                "banner_common" to "ca-app-pub-3940256099942544/9214589741",
                "home_banner_top" to "ca-app-pub-3940256099942544/9214589741",
            ),
            defaultRemoteConfigBuilder = {

                bool("OPEN_AD_ENABLE", true)
                bool("splash_inter_isAdOpenAd", true)
                bool("IS_OPEN_AD_INSTANT", false)
                bool("INTER_LOADING_ENABLE", true)
                bool("SPLASH_INTER_LOADING_ENABLE", true)
                bool("OPEN_AD_LOADING_ENABLE", true)
                long("OPEN_AD_INSTANT_TIME", 8)
                long("INTER_INSTANT_TIME", 8)
                long("splash_time", 16)

                native("exit_native") {
                    enable(true)
                    ctaColor("")
                    bgColor("")
                    adType(NativeAdType.SMALL_NATIVE_MINI)
                }
                native("home_native") {
                    enable(true)
//                    ctaColor("#000000")
                    adType(NativeAdType.SMALL_NATIVE_MEDIA_VIEW)
                    refreshTime(0)
                }
                native("subscription_native") {
                    enable(true)
                    ctaColor("")
                    bgColor("")
                    adType(NativeAdType.SMALL_NATIVE_MEDIA_VIEW)
                }
                native("lang_native_ad") {
                    enable(true)
                    adType(NativeAdType.SMALL_NATIVE)
                }

                fullScreen("splash_inter") {
                    enable(true)
                }
                fullScreen("home_inter") {
                    enable(true)
//                    instantInter(true)
                }
                fullScreen("inter_btn_plant") {
                    enable(true)
                    instantReward(false)
                }
                banner("home_banner") {
                    enable(true)
                    bannerType(BannerAdType.ADAPTIVE_BANNER)
                }
                banner("home_banner_top") {
                    enable(false)
                    bannerType(BannerAdType.ADAPTIVE_BANNER)
                }
                banner("premium_banner") {
                    enable(true)
                    bannerType(BannerAdType.BOTTOM_COLLAPSIBLE_BANNER)
                }
//                overAllNativeColor("#964B00", "#FF03DAC5")
            },
            onInitSdk = {

                AdKit.openAdManager.setOpenAdListeners(object : OpenAdListener{
                    override fun onAdShow() {
                        Log.d("opoppp", "onAdShow: ")
                    }

                    override fun onAdLoaded() {
                        Log.d("opoppp", "onAdLoaded: ")
                    }

                    override fun onAdDismissed() {
                        Log.d("opoppp", "onAdDismissed: ")

                    }

                    override fun onAdFailed(error: String) {
                        Log.d("opoppp", "onAdFailed: $error")
                    }

                })

                AdKit.initializer.disableAds(false)
                AdKit.analytics.showToast(false)
                AdKit.nativeCustomLayoutHelper.setNativeCustomLayouts(
                    largeNativeLayout = R.layout.large_native_layout_custom,
                    smallNativeLayout = R.layout.small_native_layout_custom,
//                    smallNativeMediaViewLayout = R.layout.large_native_right_jazz_custom,
                )

                AdKit.openAdManager.excludeNavigationRoutesFromOpenAd(
                    AppRoute.SplashRoute.route,
                    AppRoute.SubscriptionRoute.route
                )
            })

        val end = System.currentTimeMillis()
        Log.d("AdKitInit_sdk", "SDK init time = ${end - start} ms")
    }

    fun initializeAppClass() {
        try {
            registerActivityLifecycleCallbacks(this)
        } catch (_: Exception) {
        }
    }


    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityStarted(activity: Activity) {
        handleCurrentActivity(activity)
    }


    private fun handleCurrentActivity(activity: Activity) {
        AdKit.interHelper.setAppInPause(false)
        AdKit.openAdManager.setActivity(activity)
    }

    override fun onActivityResumed(activity: Activity) {
        handleCurrentActivity(activity)
    }

    override fun onActivityStopped(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {
        AdKit.interHelper.setAppInPause(true)
    }

    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {

        AdKit.openAdManager.setActivity(null)
        AdKit.interHelper.setAppInPause(false)
    }
}
