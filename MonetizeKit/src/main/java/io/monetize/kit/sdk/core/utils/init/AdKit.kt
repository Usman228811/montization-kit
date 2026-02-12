package io.monetize.kit.sdk.core.utils.init

import android.app.Application
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.analytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
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

    private lateinit var mContext: Application
    private var isDebug: Boolean = true
    private var postRevenueOnFireBase: Boolean = false


    val  initializer: AdKitInitializer
            by lazy {
                AdKitInitializer.getInstance()
            }

    val adKitPref: AdKitPref
            by lazy {
                AdKitPref.getInstance(mContext)
            }

    val inAppUpdateManager: AdKitInAppUpdateManager
            by lazy {
                AdKitInAppUpdateManager.getInstance()
            }

    val inAppReviewManager: AdKitInAppReviewManager
            by lazy {
                AdKitInAppReviewManager.getInstance()
            }


    val interHelper: AdKitInterHelper
            by lazy {
                AdKitInterHelper.getInstance()
            }
    val localeHelper: LocaleHelper
            by lazy {
                LocaleHelper.getInstance()
            }

    val rewardHelper: AdKitRewardHelper
            by lazy {
                AdKitRewardHelper.getInstance()
            }


    val internetController: AdKitInternetController
            by lazy {
                AdKitInternetController.getInstance(mContext)
            }


    val consentManager: AdKitConsentManager
            by lazy {
                AdKitConsentManager.getInstance(mContext, isDebug = isDebug)
            }


    val firebaseHelper: AdKitFirebaseRemoteConfigHelper
            by lazy {
                AdKitFirebaseRemoteConfigHelper.getInstance()
            }

    val  appsFlyer: AppsFlyer
            by lazy {
                AppsFlyer.getInstance()
            }


    val preLoadNative: AdKitNativePreloadHelper
            by lazy {
                AdKitNativePreloadHelper.getInstance()
            }

    val preloadBanner: AdKitBannerPreloadHelper
            by lazy {
                AdKitBannerPreloadHelper.getInstance()
            }


    val splashAdController: AdKitSplashAdController
            by lazy {
                AdKitSplashAdController.getInstance()
            }


    val openAdManager: AdKitOpenAdManager
            by lazy {
                AdKitOpenAdManager.getInstance(mContext)
            }


    val purchaseHelper: AdKitPurchaseHelper
            by lazy {
                AdKitPurchaseHelper.getInstance(mContext)
            }


    val subscriptionHelper: AdKitSubscriptionHelper
            by lazy {
                AdKitSubscriptionHelper.getInstance(mContext)
            }


    val nativeIdManager: NativeIdManager
            by lazy {
                NativeIdManager.getInstance()
            }

    val bannerIdManager: BannerIdManager
            by lazy {
                BannerIdManager.getInstance()
            }


    val nativeCustomLayoutHelper: AdsCustomLayoutHelper
            by lazy {
                AdsCustomLayoutHelper.getInstance()
            }

    val analytics: AdKitAnalytics
            by lazy {
                AdKitAnalytics.getInstance(mContext, isDebug, postRevenueOnFireBase)
            }


    val interIdManager: InterIdManager
            by lazy {
                InterIdManager.getInstance()
            }
    val rewardAdIdManager: RewardAdIdManager
            by lazy {
                RewardAdIdManager.getInstance()
            }


    fun init(
        isDebug: Boolean,
        context: Application,
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
        mContext = context
        this.isDebug = isDebug
        this.postRevenueOnFireBase = postRevenueOnFireBase
        val configBuilder = RemoteConfigBuilder.getInstance().apply(defaultRemoteConfigBuilder)
        val configDefaults = configBuilder.configMap

        try {
            FirebaseApp.initializeApp(context)
            FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = !isDebug
            Firebase.analytics.setAnalyticsCollectionEnabled(!isDebug)
        } catch (_: Exception) {
        }

        firebaseHelper.setDefaultRemoteConfigs(configDefaults)

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
