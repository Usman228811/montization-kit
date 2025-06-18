package io.monetize.kit.sdk.core.di

import android.content.Context
import android.util.Log
import com.google.android.gms.ads.MobileAds
import io.monetize.kit.sdk.ads.interstitial.AdKitInterHelper
import io.monetize.kit.sdk.ads.interstitial.InterstitialController
import io.monetize.kit.sdk.ads.interstitial.SplashAdController
import io.monetize.kit.sdk.ads.native_ad.AdPreLoad
import io.monetize.kit.sdk.core.utils.MKInternetController
import io.monetize.kit.sdk.core.utils.MkPref
import io.monetize.kit.sdk.core.utils.consent.MKConsentManager
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module


val AppKitModule = module {

    singleOf(::MkPref)
    singleOf(::MKInternetController)
    singleOf(::MKConsentManager)
    singleOf(::AdKitInterHelper)
    singleOf(::InterstitialController)
    singleOf(::SplashAdController)
    singleOf(::AdPreLoad)


}

fun provideMonetizationKitModules() = listOf(
    AppKitModule,
    dataModule,
    domainModule,
    presentationModule
)

object AdKit {
    fun init(context: Context, adMobAppId: String, onInit:()->Unit) {
        MobileAds.initialize(context) {

        }
        onInit()
    }
}