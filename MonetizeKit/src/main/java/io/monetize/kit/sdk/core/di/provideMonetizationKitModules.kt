package io.monetize.kit.sdk.core.di

import io.monetize.kit.sdk.ads.interstitial.AdKitInterHelper
import io.monetize.kit.sdk.ads.interstitial.AdKitSplashAdController
import io.monetize.kit.sdk.ads.interstitial.InterstitialController
import io.monetize.kit.sdk.ads.native_ad.AdKitNativePreloadHelper
import io.monetize.kit.sdk.ads.native_ad.AdsCustomLayoutHelper
import io.monetize.kit.sdk.ads.open.AdKitOpenAdManager
import io.monetize.kit.sdk.core.utils.AdKitInternetController
import io.monetize.kit.sdk.core.utils.AdKitPref
import io.monetize.kit.sdk.core.utils.consent.AdKitConsentManager
import io.monetize.kit.sdk.core.utils.in_app_update.AdKitInAppUpdateManager
import io.monetize.kit.sdk.core.utils.init.AdKitInitializer
import io.monetize.kit.sdk.core.utils.purchase.AdKitPurchaseHelper
import io.monetize.kit.sdk.core.utils.purchase.AdKitSubscriptionHelper
import io.monetize.kit.sdk.core.utils.remoteconfig.AdKitFirebaseRemoteConfigHelper
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module


val AppKitModule = module {

    singleOf(::AdKitPref)
    singleOf(::AdKitInternetController)
    singleOf(::AdKitConsentManager)
    singleOf(::AdKitInterHelper)
    singleOf(::InterstitialController)
    singleOf(::AdKitSplashAdController)
    singleOf(::AdKitNativePreloadHelper)
    singleOf(::AdKitOpenAdManager)
    singleOf(::AdKitInitializer)
    singleOf(::AdKitInAppUpdateManager)
    singleOf(::AdKitFirebaseRemoteConfigHelper)
    singleOf(::AdKitPurchaseHelper)
    singleOf(::AdKitSubscriptionHelper)
    singleOf(::AdsCustomLayoutHelper)


}

fun provideMonetizationKitModules() = listOf(
    AppKitModule,
    dataModule,
    domainModule,
    presentationModule
)
