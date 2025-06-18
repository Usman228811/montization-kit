package io.monetize.kit.sdk.core.di

import android.content.Context
import com.google.android.gms.ads.MobileAds
import io.monetize.kit.sdk.ads.interstitial.AdKitInterHelper
import io.monetize.kit.sdk.ads.interstitial.InterstitialController
import io.monetize.kit.sdk.ads.interstitial.SplashAdController
import io.monetize.kit.sdk.ads.native_ad.AdPreLoad
import io.monetize.kit.sdk.ads.open.AdKitOpenAdManager
import io.monetize.kit.sdk.core.utils.AdKitInternetController
import io.monetize.kit.sdk.core.utils.AdKPref
import io.monetize.kit.sdk.core.utils.consent.AdKConsentManager
import io.monetize.kit.sdk.core.utils.init.AdKitInit
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module


val AppKitModule = module {

    singleOf(::AdKPref)
    singleOf(::AdKitInternetController)
    singleOf(::AdKConsentManager)
    singleOf(::AdKitInterHelper)
    singleOf(::InterstitialController)
    singleOf(::SplashAdController)
    singleOf(::AdPreLoad)
    singleOf(::AdKitOpenAdManager)
    singleOf(::AdKitInit)


}

fun provideMonetizationKitModules() = listOf(
    AppKitModule,
    dataModule,
    domainModule,
    presentationModule
)
