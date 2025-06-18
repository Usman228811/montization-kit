package com.test.compose.adslibrary

import android.app.Application
import android.util.Log
import io.monetize.kit.sdk.ads.interstitial.AdKitInterHelper
import io.monetize.kit.sdk.ads.interstitial.InterControllerConfig
import io.monetize.kit.sdk.core.di.AdKit
import io.monetize.kit.sdk.core.di.provideMonetizationKitModules
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class AppClass : Application() {
    val adKitInterHelper: AdKitInterHelper by inject()

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@AppClass)
            modules(
                provideMonetizationKitModules()
            )
        }

        AdKit.init(
            context = this,
            adMobAppId = "ca-app-pub-3940256099942544~3347511713",
            onInit = {

                adKitInterHelper.setAdIds(
                    splashId = "ca-app-pub-3940256099942544/1033173712",
                    appInterIds = listOf(
                        "ca-app-pub-3940256099942544/1033173712",
                        "ca-app-pub-3940256099942544/1033173712"
                    ),
                    interControllerConfig = InterControllerConfig(
                        splashInterEnable = true,
                        splashTime = 16L,
                        interLoadingEnable = true

                    )
                )
                Log.d("MyAdKit", "onCreate: ")
            }
        )


    }
}
