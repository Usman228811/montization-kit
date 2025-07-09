package com.test.compose.adslibrary

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.content.Context
import android.os.Bundle
import com.test.compose.adslibrary.navigation.AppRoute
import io.monetize.kit.sdk.core.utils.init.AdKit
import io.monetize.kit.sdk.core.utils.init.AdKit.initializer
import io.monetize.kit.sdk.core.utils.init.AdKit.openAdManager

class AppClass : Application(), ActivityLifecycleCallbacks {


    companion object {
        var appContext: Context? = null
    }

    override fun onCreate() {
        super.onCreate()
        appContext = this


//        startKoin {
//            androidContext(this@AppClass)
//            modules(MainModule)
//            modules(
//                provideMonetizationKitModules()
//            )
//        }

        AdKit.init(this,"cca-app-pub-3940256099942544~3347511713", onInitSdk = {


            initializer.setNativeCustomLayouts(
                bigNativeLayout = R.layout.large_native_layout_custom,
                bigNativeShimmer = R.layout.large_native_layout_shimmer,
            )

            openAdManager.excludeComposeRoutesFromOpenAd(
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
//        canShowOpenAd =
//            (currentActivity !is SplashActivity && currentActivity !is CropImageActivity && currentActivity !is AdActivity)
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
