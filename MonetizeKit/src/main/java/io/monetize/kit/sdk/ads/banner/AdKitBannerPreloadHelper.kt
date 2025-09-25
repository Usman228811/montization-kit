package io.monetize.kit.sdk.ads.banner

import android.app.Activity
import io.monetize.kit.sdk.core.utils.adtype.BannerControllerConfig
import io.monetize.kit.sdk.core.utils.init.AdKit

class AdKitBannerPreloadHelper private constructor(
) {

    companion object {
        @Volatile
        private var instance: AdKitBannerPreloadHelper? = null


        internal fun getInstance(
        ): AdKitBannerPreloadHelper {
            return instance ?: synchronized(this) {
                instance ?: AdKitBannerPreloadHelper().also { instance = it }
            }
        }
    }

    fun preLoadBanner(mContext: Activity, bannerControllerConfig: BannerControllerConfig) {
        val bannerType =
            AdKit.firebaseHelper.getLong("${bannerControllerConfig.placementKey}_bannerType", 0)


        if (AdKit.firebaseHelper.getBoolean(
                "${bannerControllerConfig.placementKey}_isAdEnable",
                false
            )
            && bannerType != 3L
            && bannerType != 4L
            && AdKit.consentManager.canRequestAds
        ) {
            var index = singleBannerList.indexOfFirst { it.key == bannerControllerConfig.adIdKey }
            if (index == -1) {
                singleBannerList.apply {
                    add(
                        BannerSingleAdControllerModel(
                            AdKitBannerController(),
                            bannerControllerConfig.adIdKey,
                        )
                    )
                }
                index = singleBannerList.indexOfFirst { it.key == bannerControllerConfig.adIdKey }
            }
            if (index in singleBannerList.indices) {
                singleBannerList[index].controller?.loadNewBannerAd(
                    context = mContext,
                    bannerControllerConfig = bannerControllerConfig,
                    bannerType
                )
            }
        }
    }
}