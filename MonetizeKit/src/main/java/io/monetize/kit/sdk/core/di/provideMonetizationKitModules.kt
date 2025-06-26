package io.monetize.kit.sdk.core.di

import io.monetize.kit.sdk.ads.interstitial.AdSdkInterHelper
import io.monetize.kit.sdk.ads.interstitial.AdSdkSplashAdController
import io.monetize.kit.sdk.ads.interstitial.InterstitialController
import io.monetize.kit.sdk.ads.native_ad.AdSdkNativePreloadHelper
import io.monetize.kit.sdk.ads.native_ad.AdsCustomLayoutHelper
import io.monetize.kit.sdk.ads.open.AdSdkOpenAdManager
import io.monetize.kit.sdk.core.utils.AdSdkInternetController
import io.monetize.kit.sdk.core.utils.AdSdkPref
import io.monetize.kit.sdk.core.utils.consent.AdSdkConsentManager
import io.monetize.kit.sdk.core.utils.in_app_update.AdSdkInAppUpdateManager
import io.monetize.kit.sdk.core.utils.init.AdSdkInitializer
import io.monetize.kit.sdk.core.utils.purchase.AdSdkPurchaseHelper
import io.monetize.kit.sdk.core.utils.purchase.AdSdkSubscriptionHelper
import io.monetize.kit.sdk.core.utils.remoteconfig.AdSdkFirebaseRemoteConfigHelper
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module


val AppKitModule = module {

    singleOf(::AdSdkPref)
    singleOf(::AdSdkInternetController)
    singleOf(::AdSdkConsentManager)
    singleOf(::AdSdkInterHelper)
    singleOf(::InterstitialController)
    singleOf(::AdSdkSplashAdController)
    singleOf(::AdSdkNativePreloadHelper)
    singleOf(::AdSdkOpenAdManager)
    singleOf(::AdSdkInitializer)
    singleOf(::AdSdkInAppUpdateManager)
    singleOf(::AdSdkFirebaseRemoteConfigHelper)
    singleOf(::AdSdkPurchaseHelper)
    singleOf(::AdSdkSubscriptionHelper)
    singleOf(::AdsCustomLayoutHelper)


}

fun provideMonetizationKitModules() = listOf(
    AppKitModule,
    dataModule,
    domainModule,
    presentationModule
)
