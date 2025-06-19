package io.monetize.kit.sdk.core.utils.init

import android.content.Context
import com.google.android.gms.ads.MobileAds
import io.monetize.kit.sdk.ads.interstitial.AdSdkInterHelper
import io.monetize.kit.sdk.ads.interstitial.InterControllerConfig
import io.monetize.kit.sdk.ads.open.AdKitOpenAdManager

class AdSdkInitializer(
    private val context: Context,
    private val adSdkInterHelper: AdSdkInterHelper,
    private val adKitOpenAdManager: AdKitOpenAdManager
) {

    fun initMobileAds(adMobAppId: String, onInit: () -> Unit) {
        MobileAds.initialize(context) {

        }
        onInit()
    }

    fun init(
        interControllerConfig: InterControllerConfig,
    ) {
        adSdkInterHelper.setAdIds(
            splashId = interControllerConfig.splashId,
            appInterIds = interControllerConfig.appInterIds,
            interControllerConfig = interControllerConfig
        )
        adKitOpenAdManager.setOpenAdConfigs(
            adId = interControllerConfig.openAdId,
            isAdEnable = interControllerConfig.openAdEnable,
            isLoadingEnable = interControllerConfig.openAdLoadingEnable
        )
        adKitOpenAdManager.initOpenAd()

    }
}