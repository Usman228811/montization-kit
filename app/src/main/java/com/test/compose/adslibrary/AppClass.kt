package com.test.compose.adslibrary

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.content.Context
import android.os.Bundle
import com.test.compose.adslibrary.di.MainModule
import io.monetize.kit.sdk.ads.interstitial.AdSdkInterHelper
import io.monetize.kit.sdk.ads.open.AdKitOpenAdManager
import io.monetize.kit.sdk.core.di.provideMonetizationKitModules
import io.monetize.kit.sdk.core.utils.init.AdSdkInitializer
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class AppClass : Application(), ActivityLifecycleCallbacks {

    private val adSdkInitializer: AdSdkInitializer by inject()
    private val adSdkInterHelper: AdSdkInterHelper by inject()
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

        adSdkInitializer.initMobileAds(
            adMobAppId = "ca-app-pub-3940256099942544~3347511713",
            onInit = {


            }
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
        adSdkInterHelper.setAppInPause(false)
        adKitOpenAdManager.setActivity(activity)
//        canShowOpenAd =
//            (currentActivity !is SplashActivity && currentActivity !is CropImageActivity && currentActivity !is AdActivity)
    }

    override fun onActivityResumed(activity: Activity) {
        handleCurrentActivity(activity)
    }

    override fun onActivityStopped(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {
        adSdkInterHelper.setAppInPause(true)
    }

    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {

        adKitOpenAdManager.setActivity(null)
        adSdkInterHelper.setAppInPause(false)
    }
}
