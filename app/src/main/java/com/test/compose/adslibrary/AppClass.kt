package com.test.compose.adslibrary

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.content.Context
import android.os.Bundle
import com.test.compose.adslibrary.navigation.AppRoute
import io.monetize.kit.sdk.core.utils.init.AdKit

class AppClass : Application(), ActivityLifecycleCallbacks {


    companion object {
        var appContext: Context? = null
    }

    override fun onCreate() {
        super.onCreate()
        appContext = this

        AdKit.init(
            isDebug = BuildConfig.DEBUG,
            appFlyerSdkKey = "",
            context = this,
            admobId = "ca-app-pub-3940256099942544~3347511713",
            openAdId = "/21775744923/example/app-open",
            mapOfInterIds = mapOf(
                "splash_inter" to "/21775744923/example/interstitial",
                "splash_open_ad" to "/21775744923/example/app-open",
                "home_inter" to "/21775744923/example/interstitial",
                "inter_common" to listOf(
                    "/21775744923/example/interstitial",
                    "/21775744923/example/interstitial",
                    "/21775744923/example/interstitial"
                )
            ),
            mapOfRewardIds = mapOf(
                "reward_main" to "/21775744923/example/rewarded"
            ),
            mapOfNativeIds = mapOf(
                "exit_native" to "/21775744923/example/native",
                "home_native" to "/21775744923/example/native",
                "subscription_native" to "/21775744923/example/native",
            ),
            mapOfBannerIds = mapOf(
                "premium_banner" to "/21775744923/example/adaptive-banner", //banner
//                "home_banner" to "/21775744923/example/adaptive-banner", //banner
                "home_banner" to "/21775744923/example/adaptive-banner", // collapsible
                "home_banner_top" to "/21775744923/example/adaptive-banner", // collapsible
            ),
            defaultRemoteConfigBuilder = {

                bool("OPEN_AD_ENABLE", true)
                bool("splash_inter_isAdOpenAd", false)
                bool("IS_OPEN_AD_INSTANT", false)
                bool("INTER_LOADING_ENABLE", true)
                bool("OPEN_AD_LOADING_ENABLE", true)
                long("OPEN_AD_INSTANT_TIME", 8)
                long("INTER_INSTANT_TIME", 8)
                long("splash_time", 16)

                native("exit_native"){
                    enable(true)
                    ctaColor("")
                    bgColor("")
                    adType(1)
                }
                native("home_native"){
                    enable(true)
                    ctaColor("#000000")
                    adType(2)
                }
                native("subscription_native"){
                    enable(true)
                    ctaColor("")
                    bgColor("")
                    adType(2)
                }

                fullScreen("splash_inter"){
                    enable(false)
                }
                fullScreen("home_inter"){
                    enable(true)
                    instantInter(true)
                }
                fullScreen("inter_btn_plant"){
                    enable(true)
                }
                fullScreen("inter_btn_plant"){
                    enable(true)
                    instantReward(true)
                }
                banner("home_banner"){
                    enable(true)
                    bannerType(0)
                }
                banner("home_banner_top"){
                    enable(true)
                    bannerType(0)
                }
                banner("premium_banner"){
                    enable(true)
                }
//                overAllNativeColor("#964B00", "#FF03DAC5")
            },
            onInitSdk = {

                AdKit.initializer.disableAds(false)
                AdKit.analytics.showToast(false)
                AdKit.nativeCustomLayoutHelper.setNativeCustomLayouts(
                    largeNativeLayout = R.layout.large_native_layout_custom,
                    smallNativeLayout = R.layout.small_native_layout_custom,
                    smallNativeMediaViewLayout = R.layout.large_native_right_jazz_custom,
                )

                AdKit.openAdManager.excludeComposeRoutesFromOpenAd(
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
