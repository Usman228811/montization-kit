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
        AdKit.init(
            isDebug = true,
            appFlyerSdkKey = "",
            postRevenueOnFireBase = true,
            context = this,
            openAdId = "/21775744923/example/app-open",
            mapOfInterIds = mapOf(
                "splash_inter" to "/21775744923/example/interstitial",
                "splash_open_ad" to "/21775744923/example/app-open",

                "inter_common" to listOf(
                    "/21775744923/example/interstitial",
                    "/21775744923/example/interstitial",
                    "/21775744923/example/interstitial"
                )
            ),
            mapOfRewardIds = mapOf(
                "reward_main" to "/21775744923/example/rewarded",
                "reward_common" to "/21775744923/example/rewarded"
            ),
            mapOfNativeIds = mapOf(
                "large_native" to "/21775744923/example/native",
                "small_native_media_view" to "/21775744923/example/native",
                "small_native" to "/21775744923/example/native",
                "small_native_mini" to "/21775744923/example/native",
                "full_native" to "/21775744923/example/native",

                "native_common" to listOf(
                    "/21775744923/example/native",
                ),
            ),
            mapOfBannerIds = mapOf(
                "bottom_banner_collapsable" to "ca-app-pub-3940256099942544/9214589741",
                "top_banner_collapsable" to "ca-app-pub-3940256099942544/9214589741",
                "adaptive_banner" to "ca-app-pub-3940256099942544/9214589741",
                "large_banner" to "ca-app-pub-3940256099942544/9214589741",
                "med_rec_banner" to "ca-app-pub-3940256099942544/9214589741",
                "large_anchored_banner" to "ca-app-pub-3940256099942544/9214589741",
                "banner_common" to "ca-app-pub-3940256099942544/9214589741",
            ),
            defaultRemoteConfigBuilder = {

                bool("OPEN_AD_ENABLE", true)
                bool("splash_inter_isAdOpenAd", false)
                bool("IS_OPEN_AD_INSTANT", false)
                bool("INTER_LOADING_ENABLE", false)
                bool("SPLASH_INTER_LOADING_ENABLE", true)
                bool("OPEN_AD_LOADING_ENABLE", true)
                long("OPEN_AD_INSTANT_TIME", 8)
                long("INTER_INSTANT_TIME", 8)
                long("splash_time", 16)

                //native ads
                native("large_native") {
                    enable(true)
                    adType(NativeAdType.LARGE_NATIVE)
                }
                native("small_native_media_view") {
                    enable(true)
                    adType(NativeAdType.SMALL_NATIVE_MEDIA_VIEW)
                }
                native("small_native") {
                    enable(true)
                    adType(NativeAdType.SMALL_NATIVE)
                }
                native("small_native_mini") {
                    enable(true)
                    adType(NativeAdType.SMALL_NATIVE_MINI)
                }
                native("full_native") {
                    enable(true)
                    adType(NativeAdType.FULL_NATIVE)
                }



                native("exit_native") {
                    enable(true)
                    adType(NativeAdType.SMALL_NATIVE_MINI)
                }
                native("subscription_native") {
                    enable(true)
                    ctaColor("")
                    bgColor("")
                    adType(NativeAdType.SMALL_NATIVE)
                }


                //inter or reward ads
                fullScreen("splash_inter") {
                    enable(true)
                }

                fullScreen("inter_instant_with_counter") {
                    enable(true)
                    instantInter(true)
                }

                fullScreen("inter_preload_with_counter") {
                    enable(true)
                }
                fullScreen("inter_instant") {
                    enable(true)
                    instantInter(true)
                }
                fullScreen("inter_preload") {
                    enable(true)
                }
                fullScreen("reward_ad") {
                    enable(true)
                    instantReward(true)
                }

                //Banner Ads

                banner(placementKey = "exit_banner") {
                    enable(true)
                    bannerType(BannerAdType.ADAPTIVE_BANNER)
                }

                banner(placementKey = "bottom_banner_collapsable") {
                    enable(true)
                    bannerType(BannerAdType.BOTTOM_COLLAPSIBLE_BANNER)
                }

                banner(placementKey = "top_banner_collapsable") {
                    enable(true)
                    bannerType(BannerAdType.TOP_COLLAPSIBLE_BANNER)
                }
                banner(placementKey = "adaptive_banner") {
                    enable(true)
                    bannerType(BannerAdType.ADAPTIVE_BANNER)
                }
                banner(placementKey = "large_banner") {
                    enable(true)
                    bannerType(BannerAdType.LARGE_BANNER)
                }
                banner(placementKey = "med_rec_banner") {
                    enable(true)
                    bannerType(BannerAdType.MEDIUM_RECTANGLE_BANNER)
                }
                banner(placementKey = "large_anchored_banner") {
                    enable(true)
                    bannerType(BannerAdType.LARGE_ANCHORED_ADAPTIVE_BANNER)
                }
                banner(placementKey = "premium_banner") {
                    enable(true)
                    bannerType(BannerAdType.ADAPTIVE_BANNER)
                }


//                overAllNativeColor("#964B00", "#FF03DAC5")
            },
            onDefaultConfigGenerated = { defaultConfigs ->
                Log.d("opoppp", "onDefaultConfigGenerated: $defaultConfigs")
            },
            onInitSdk = {

                AdKit.openAdManager.setOpenAdListeners(object : OpenAdListener {
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
                    smallNativeMediaViewLayout = R.layout.large_native_right_jazz_custom,
                )

                AdKit.openAdManager.excludeNavigationRoutesFromOpenAd(
                    AppRoute.SplashRoute.route,
                    AppRoute.SubscriptionRoute.route
                )
            })
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
