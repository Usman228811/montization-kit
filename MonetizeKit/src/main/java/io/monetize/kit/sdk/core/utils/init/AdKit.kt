package io.monetize.kit.sdk.core.utils.init

import android.content.Context
import com.google.firebase.FirebaseApp
import io.monetize.kit.sdk.ads.banner.BannerIdManager
import io.monetize.kit.sdk.ads.interstitial.AdKitInterHelper
import io.monetize.kit.sdk.ads.interstitial.AdKitSplashAdController
import io.monetize.kit.sdk.ads.interstitial.InterIdManager
import io.monetize.kit.sdk.ads.native_ad.AdKitNativePreloadHelper
import io.monetize.kit.sdk.ads.native_ad.AdsCustomLayoutHelper
import io.monetize.kit.sdk.ads.native_ad.NativeIdManager
import io.monetize.kit.sdk.ads.open.AdKitOpenAdManager
import io.monetize.kit.sdk.core.utils.AdKitInternetController
import io.monetize.kit.sdk.core.utils.AdKitPref
import io.monetize.kit.sdk.core.utils.analytics.AdKitAnalytics
import io.monetize.kit.sdk.core.utils.consent.AdKitConsentManager
import io.monetize.kit.sdk.core.utils.in_app_update.AdKitInAppUpdateManager
import io.monetize.kit.sdk.core.utils.purchase.AdKitPurchaseHelper
import io.monetize.kit.sdk.core.utils.purchase.AdKitSubscriptionHelper
import io.monetize.kit.sdk.core.utils.remoteconfig.AdKitFirebaseRemoteConfigHelper

object AdKit {

    lateinit var initializer: AdKitInitializer
        private set

    lateinit var adKitPref: AdKitPref
        private set

    lateinit var inAppUpdateManager: AdKitInAppUpdateManager
        private set


    lateinit var interHelper: AdKitInterHelper
        private set


    lateinit var internetController: AdKitInternetController
        private set


    lateinit var consentManager: AdKitConsentManager
        private set


    lateinit var firebaseHelper: AdKitFirebaseRemoteConfigHelper
        private set


    lateinit var preLoadNative: AdKitNativePreloadHelper
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


    fun init(
        context: Context, admobId: String,
        openAdId: String,
        mapOfInterIds: Map<String, Any>,
        mapOfNativeIds: Map<String, Any>,
        mapOfBannerIds: Map<String, Any>,
        resetInterKeyForCommonAds: String? = null, onInitSdk: () -> Unit
    ) {
        initializer = AdKitInitializer.getInstance()
        adKitPref = AdKitPref.getInstance(context)
        interHelper = AdKitInterHelper.getInstance()
        inAppUpdateManager = AdKitInAppUpdateManager.getInstance(context)
        internetController = AdKitInternetController.getInstance(context)
        consentManager = AdKitConsentManager.getInstance(context)
        firebaseHelper = AdKitFirebaseRemoteConfigHelper.getInstance()
        preLoadNative = AdKitNativePreloadHelper.getInstance()
        splashAdController = AdKitSplashAdController.getInstance()
        openAdManager = AdKitOpenAdManager.getInstance(context)
        purchaseHelper = AdKitPurchaseHelper.getInstance(context)
        subscriptionHelper = AdKitSubscriptionHelper.getInstance(context)
        nativeCustomLayoutHelper = AdsCustomLayoutHelper.getInstance()
        analytics = AdKitAnalytics.getInstance(context)
        interIdManager = InterIdManager.getInstance()
        nativeIdManager = NativeIdManager.getInstance()
        bannerIdManager = BannerIdManager.getInstance()
        openAdManager.setOpenAdId(
            adId = openAdId
        )

        interIdManager.setInterIds(mapOfInterIds)
        nativeIdManager.setNativeIds(mapOfNativeIds)
        bannerIdManager.setBannerIds(mapOfBannerIds)
        resetInterKeyForCommonAds?.let {
            adKitPref.putInterInt(it, 0)
        }

        initializer.initMobileAds(
            context = context,
            adMobAppId = admobId,
            onInit = {

            }
        )
        onInitSdk()
    }
}
