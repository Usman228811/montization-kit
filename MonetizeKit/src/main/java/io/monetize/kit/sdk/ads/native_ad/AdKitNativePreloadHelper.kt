package io.monetize.kit.sdk.ads.native_ad

import android.app.Activity
import io.monetize.kit.sdk.core.utils.adtype.NativeControllerConfig
import io.monetize.kit.sdk.core.utils.firebaseBoolean
import io.monetize.kit.sdk.core.utils.init.AdKit

class AdKitNativePreloadHelper private constructor(
) {

    companion object {
        @Volatile
        private var instance: AdKitNativePreloadHelper? = null


        internal fun getInstance(
        ): AdKitNativePreloadHelper {
            return instance ?: synchronized(this) {
                instance ?: AdKitNativePreloadHelper().also { instance = it }
            }
        }
    }

    fun preLoadNativeAd(mContext: Activity, nativeControllerConfig: NativeControllerConfig) {

        if (firebaseBoolean("${nativeControllerConfig.placementKey}_isAdEnable", true) && AdKit.consentManager.canRequestAds) {
            var index = singleNativeList.indexOfFirst { it.key == nativeControllerConfig.adIdKey }
            if (index == -1) {
                singleNativeList.apply {
                    add(
                        NativeAdSingleModel(
                            nativeControllerConfig.adIdKey,
                            NativeAdSingleController()
                        )
                    )
                }
                index = singleNativeList.indexOfFirst { it.key == nativeControllerConfig.adIdKey }
            }
            if (index in singleNativeList.indices) {
                singleNativeList[index].controller?.preloadNativeAd(
                    context = mContext,
                    nativeControllerConfig = nativeControllerConfig
                )
            }
        }
    }

    fun getControllerWithAd(): NativeAdSingleModel? {
        return singleNativeList
            .firstOrNull { it.controller?.hasNativeAd() == true }
    }
}