package io.monetize.kit.sdk.core.di

import io.monetize.kit.sdk.ads.interstitial.AdSdkInterHelper
import io.monetize.kit.sdk.ads.interstitial.InterstitialController
import io.monetize.kit.sdk.ads.interstitial.AdSdkSplashAdController
import io.monetize.kit.sdk.ads.native_ad.AdPreLoad
import io.monetize.kit.sdk.ads.open.AdKitOpenAdManager
import io.monetize.kit.sdk.core.utils.AdSdkInternetController
import io.monetize.kit.sdk.core.utils.AdSdkPref
import io.monetize.kit.sdk.core.utils.consent.AdSdkConsentManager
import io.monetize.kit.sdk.core.utils.init.AdSdkInitializer
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module


val AppKitModule = module {

    singleOf(::AdSdkPref)
    singleOf(::AdSdkInternetController)
    singleOf(::AdSdkConsentManager)
    singleOf(::AdSdkInterHelper)
    singleOf(::InterstitialController)
    singleOf(::AdSdkSplashAdController)
    singleOf(::AdPreLoad)
    singleOf(::AdKitOpenAdManager)
    singleOf(::AdSdkInitializer)


}

fun provideMonetizationKitModules() = listOf(
    AppKitModule,
    dataModule,
    domainModule,
    presentationModule
)
