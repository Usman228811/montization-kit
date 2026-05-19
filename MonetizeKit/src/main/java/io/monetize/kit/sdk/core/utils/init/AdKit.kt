package io.monetize.kit.sdk.core.utils.init

import android.app.Application
import android.content.Context
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.analytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.revenuecat.purchases.LogLevel
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
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
import io.monetize.kit.sdk.core.utils.purchase.AdKitPremiumHelper
import io.monetize.kit.sdk.core.utils.remoteconfig.AdKitFirebaseRemoteConfigHelper
import io.monetize.kit.sdk.core.utils.remoteconfig.RemoteConfigBuilder
import io.monetize.kit.sdk.data.impl.RevenueCatBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object AdKit {
    private val logLevel: LogLevel = LogLevel.DEBUG


    private lateinit var mContext: Application
    private var isDebug: Boolean = true
    private var revenueCatBuilder: RevenueCatBuilder? = null
    private var postRevenueOnFireBase: Boolean = false

    internal fun getRevenueCatKey(): String {
        return revenueCatBuilder?.revenueCatKey ?: ""

    }
    internal fun getRevenueCatOfferingKey(): String {
        return revenueCatBuilder?.offeringKey ?: ""

    }


    val initializer: AdKitInitializer
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

    val appsFlyer: AppsFlyer
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


    val premiumHelper: AdKitPremiumHelper
            by lazy {
                AdKitPremiumHelper.getInstance(mContext)
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

    private fun configureRevenueCat(context: Context) {
        Purchases.logLevel = logLevel
        val configuration = PurchasesConfiguration.Builder(
            context,
            getRevenueCatKey()
        ).build()
        Purchases.configure(configuration)
    }


    fun init(
        isDebug: Boolean,
        context: Application,
        openAdId: String,
        mapOfInterIds: Map<String, Any>,
        mapOfRewardIds: Map<String, Any>,
        mapOfNativeIds: Map<String, Any>,
        mapOfBannerIds: Map<String, Any>,
        defaultRemoteConfigBuilder: RemoteConfigBuilder.() -> Unit,
        resetInterKeyForCommonAds: String? = null,
        postRevenueOnFireBase: Boolean = false,
        appFlyerSdkKey: String,
        revenueCatBuilder: RevenueCatBuilder? = null,
        onDefaultConfigGenerated: (String) -> Unit,
        onInitSdk: () -> Unit
    ) {
        mContext = context
        this.revenueCatBuilder = revenueCatBuilder
        this.isDebug = isDebug
        this.postRevenueOnFireBase = postRevenueOnFireBase
        val configBuilder = RemoteConfigBuilder.getInstance().apply(defaultRemoteConfigBuilder)
        val configDefaults = configBuilder.configMap

        val content = configBuilder.mapToKeyValue()
        onDefaultConfigGenerated(content)


        CoroutineScope(Dispatchers.IO).launch {
            try {
                FirebaseApp.initializeApp(context)
                firebaseHelper.setDefaultRemoteConfigs(configDefaults)
                FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = !isDebug
                Firebase.analytics.setAnalyticsCollectionEnabled(!isDebug)
            } catch (_: Exception) {
            }
        }

        if (AdKit.revenueCatBuilder?.revenueCatKey?.isNotEmpty() == true) {
            configureRevenueCat(context.applicationContext)
        }


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

private fun getAppName(context: Context): String {
    val appInfo = context.applicationInfo
    val resId = appInfo.labelRes

    return if (resId != 0) {
        context.getString(resId)
    } else {
        appInfo.nonLocalizedLabel?.toString() ?: "Unknown"
    }
}
