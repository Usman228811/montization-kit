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
            context = this,
            admobId = "ca-app-pub-3940256099942544~3347511713",
            openAdId = "ca-app-pub-3940256099942544/9257395921",
            mapOfInterIds = mapOf(
//                "splash_inter" to "ca-app-pub-3940256099942544/1033173712",
                "splash_open_ad" to "ca-app-pub-3940256099942544/9257395921",
                "home_inter" to "ca-app-pub-3940256099942544/1033173712",
                "inter_common" to listOf(
                    "ca-app-pub-3940256099942544/1033173712",
                    "ca-app-pub-3940256099942544/1033173712",
                    "ca-app-pub-3940256099942544/1033173712"
                )
            ),
            mapOfRewardIds = mapOf(
                "reward_main" to "ca-app-pub-3940256099942544/5224354917"
            ),
            mapOfNativeIds = mapOf(
                "exit_native" to "ca-app-pub-3940256099942544/2247696110",
                "home_native" to "ca-app-pub-3940256099942544/2247696110",
                "subscription_native" to "ca-app-pub-3940256099942544/2247696110",
            ),
            mapOfBannerIds = mapOf(
                "premium_banner" to "ca-app-pub-3940256099942544/9214589741", //banner
                "home_banner" to "ca-app-pub-3940256099942544/9214589741", //banner
//                "home_banner" to "ca-app-pub-3940256099942544/2014213617", // collapsible
            ),
            defaultRemoteConfigBuilder = {
                bool("exit_native_isAdEnable", true)
                bool("splash_inter_isAdEnable", true)
                bool("inter_btn_plant_isAdEnable", true)
                bool("inter_btn_plant_isInterInstant", true)
                bool("inter_btn_plant_isRewardInstant", true)
                bool("home_native_isAdEnable", true)
                string("home_native_ctaColor", "#FF03DAC5")
                string("overAllNativeCtaColor", "#FFFFFF")
                string("overAllNativeBgColor", "#964B00")
                bool("home_banner_isAdEnable", true)
                bool("home_banner_isCollapsible", false)
                bool("premium_banner_isAdEnable", true)
                bool("subscription_native_isAdEnable", true)
                long("home_native_adType", 1L)
                long("subscription_native_adType", 1L)
            },
            overAllNativeBgColor = "#1B38B4",
            overAllNativeCtaColor = "#FFEB3B",
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
