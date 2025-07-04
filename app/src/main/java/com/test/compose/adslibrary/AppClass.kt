package com.test.compose.adslibrary

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.content.Context
import android.os.Bundle
import com.test.compose.adslibrary.di.MainModule
import com.test.compose.adslibrary.navigation.AppRoute
import io.monetize.kit.sdk.ads.interstitial.AdKitInterHelper
import io.monetize.kit.sdk.ads.open.AdKitOpenAdManager
import io.monetize.kit.sdk.core.di.provideMonetizationKitModules
import io.monetize.kit.sdk.core.utils.init.AdKitInitializer
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class AppClass : Application(), ActivityLifecycleCallbacks {

    private val adKitInitializer: AdKitInitializer by inject()
    private val adKitInterHelper: AdKitInterHelper by inject()
    private val adKitOpenAdManager: AdKitOpenAdManager by inject()

    companion object {
        var appContext: Context? = null
    }

    override fun onCreate() {
        super.onCreate()
        appContext = this

        startKoin {
            androidContext(this@AppClass)
            modules(MainModule)
            modules(
                provideMonetizationKitModules()
            )
        }

        adKitInitializer.initMobileAds(
            adMobAppId = "ca-app-pub-9690615864092002~8960663430",
            onInit = {


            }
        )

        adKitInitializer.setNativeCustomLayouts(
            bigNativeLayout = R.layout.large_native_layout_custom,
            bigNativeShimmer = R.layout.large_native_layout_shimmer,
        )

        adKitOpenAdManager.excludeComposeRoutesFromOpenAd(
            AppRoute.SplashRoute.route,
            AppRoute.SubscriptionRoute.route
        )


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
        adKitInterHelper.setAppInPause(false)
        adKitOpenAdManager.setActivity(activity)
//        canShowOpenAd =
//            (currentActivity !is SplashActivity && currentActivity !is CropImageActivity && currentActivity !is AdActivity)
    }

    override fun onActivityResumed(activity: Activity) {
        handleCurrentActivity(activity)
    }

    override fun onActivityStopped(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {
        adKitInterHelper.setAppInPause(true)
    }

    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {

        adKitOpenAdManager.setActivity(null)
        adKitInterHelper.setAppInPause(false)
    }
}
