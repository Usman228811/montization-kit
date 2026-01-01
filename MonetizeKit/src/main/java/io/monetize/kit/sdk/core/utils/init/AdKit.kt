package io.monetize.kit.sdk.core.utils.init

import android.content.Context
import io.monetize.kit.sdk.ads.banner.AdKitBannerPreloadHelper
import io.monetize.kit.sdk.ads.banner.BannerIdManager
import io.monetize.kit.sdk.ads.interstitial.AdKitInterHelper
import io.monetize.kit.sdk.ads.interstitial.AdKitSplashAdController
import io.monetize.kit.sdk.ads.interstitial.InterIdManager
import io.monetize.kit.sdk.ads.native_ad.AdKitNativePreloadHelper
import io.monetize.kit.sdk.ads.native_ad.AdsCustomLayoutHelper
import io.monetize.kit.sdk.ads.native_ad.NativeIdManager
import io.monetize.kit.sdk.ads.open.AdKitOpenAdManager
import io.monetize.kit.sdk.ads.rewarded.AdKitRewardHelper
import io.monetize.kit.sdk.ads.rewarded.RewardAdIdManager
import io.monetize.kit.sdk.core.utils.AdKitInternetController
import io.monetize.kit.sdk.core.utils.AdKitPref
import io.monetize.kit.sdk.core.utils.analytics.AdKitAnalytics
import io.monetize.kit.sdk.core.utils.appflyer.AppsFlyer
import io.monetize.kit.sdk.core.utils.consent.AdKitConsentManager
import io.monetize.kit.sdk.core.utils.in_app_review.AdKitInAppReviewManager
import io.monetize.kit.sdk.core.utils.in_app_update.AdKitInAppUpdateManager
import io.monetize.kit.sdk.core.utils.locale.LocaleHelper
import io.monetize.kit.sdk.core.utils.purchase.AdKitPurchaseHelper
import io.monetize.kit.sdk.core.utils.purchase.AdKitSubscriptionHelper
import io.monetize.kit.sdk.core.utils.remoteconfig.AdKitFirebaseRemoteConfigHelper
import io.monetize.kit.sdk.core.utils.remoteconfig.RemoteConfigBuilder

object AdKit {

    lateinit var initializer: AdKitInitializer
        private set

    lateinit var adKitPref: AdKitPref
        private set

    lateinit var inAppUpdateManager: AdKitInAppUpdateManager
        private set

    lateinit var inAppReviewManager: AdKitInAppReviewManager
        private set


    lateinit var interHelper: AdKitInterHelper
        private set
    lateinit var localeHelper: LocaleHelper
        private set

    lateinit var rewardHelper: AdKitRewardHelper
        private set


    lateinit var internetController: AdKitInternetController
        private set


    lateinit var consentManager: AdKitConsentManager
        private set


    lateinit var firebaseHelper: AdKitFirebaseRemoteConfigHelper
        private set

    lateinit var appsFlyer: AppsFlyer
        private set


    lateinit var preLoadNative: AdKitNativePreloadHelper
        private set

    lateinit var preloadBanner: AdKitBannerPreloadHelper
        private set


    lateinit var splashAdController: AdKitSplashAdController
        private set


    lateinit var openAdManager: AdKitOpenAdManager
        private set


    lateinit var purchaseHelper: AdKitPurchaseHelper
        private set


    lateinit var subscriptionHelper: AdKitSubscriptionHelper
        private set


    lateinit var nativeIdManager: NativeIdManager
        private set

    lateinit var bannerIdManager: BannerIdManager
        private set


    lateinit var nativeCustomLayoutHelper: AdsCustomLayoutHelper
        private set

    lateinit var analytics: AdKitAnalytics
        private set


    lateinit var interIdManager: InterIdManager
        private set
    lateinit var rewardAdIdManager: RewardAdIdManager
        private set


    fun init(
        isDebug: Boolean,
        context: Context,
        admobId: String,
        openAdId: String,
        mapOfInterIds: Map<String, Any>,
        mapOfRewardIds: Map<String, Any>,
        mapOfNativeIds: Map<String, Any>,
        mapOfBannerIds: Map<String, Any>,
        defaultRemoteConfigBuilder: RemoteConfigBuilder.() -> Unit,
        resetInterKeyForCommonAds: String? = null,
        postRevenueOnFireBase: Boolean = false,
        appFlyerSdkKey: String,
        onInitSdk: () -> Unit
    ) {
        val configBuilder = RemoteConfigBuilder.getInstance().apply(defaultRemoteConfigBuilder)
        val configDefaults = configBuilder.configMap

        initializer = AdKitInitializer.getInstance()
        appsFlyer = AppsFlyer.getInstance()
        adKitPref = AdKitPref.getInstance(context)
        interHelper = AdKitInterHelper.getInstance()
        localeHelper = LocaleHelper.getInstance()
        rewardHelper = AdKitRewardHelper.getInstance()
        inAppUpdateManager = AdKitInAppUpdateManager.getInstance()
        inAppReviewManager = AdKitInAppReviewManager.getInstance()
        internetController = AdKitInternetController.getInstance(context)
        consentManager = AdKitConsentManager.getInstance(context, isDebug = isDebug)
        firebaseHelper = AdKitFirebaseRemoteConfigHelper.getInstance()
        firebaseHelper.setDefaultRemoteConfigs(configDefaults)
        preLoadNative = AdKitNativePreloadHelper.getInstance()
        preloadBanner = AdKitBannerPreloadHelper.getInstance()
        splashAdController = AdKitSplashAdController.getInstance()
        openAdManager = AdKitOpenAdManager.getInstance(context)
        purchaseHelper = AdKitPurchaseHelper.getInstance(context)
        subscriptionHelper = AdKitSubscriptionHelper.getInstance(context)
        nativeCustomLayoutHelper = AdsCustomLayoutHelper.getInstance()
        analytics = AdKitAnalytics.getInstance(context, isDebug, postRevenueOnFireBase)
        interIdManager = InterIdManager.getInstance()
        rewardAdIdManager = RewardAdIdManager.getInstance()
        nativeIdManager = NativeIdManager.getInstance()
        bannerIdManager = BannerIdManager.getInstance()
        openAdManager.setOpenAdId(
            adId = openAdId
        )

        interIdManager.setInterIds(mapOfInterIds)
        rewardAdIdManager.setRewardAdIds(mapOfRewardIds)
        nativeIdManager.setNativeIds(mapOfNativeIds)
        bannerIdManager.setBannerIds(mapOfBannerIds)
        resetInterKeyForCommonAds?.let {
            adKitPref.putInterInt(it, 0)
        }

        initializer.initMobileAds(
            isDebug = isDebug,
            context = context,
            adMobAppId = admobId,
            onInit = {

            }
        )
        appsFlyer.initAppFlyer(
            context,
            appFlyerSdkKey,
            isDebug
        )
        onInitSdk()
    }
}
