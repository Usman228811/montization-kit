package io.monetize.kit.sdk.ads.native_ad

import android.app.Activity
import io.monetize.kit.sdk.core.utils.adtype.NativeControllerConfig
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
        if (nativeControllerConfig.isAdEnable && AdKit.consentManager.canRequestAds) {
            var index = singleNativeList.indexOfFirst { it.key == nativeControllerConfig.key }
            if (index == -1) {
                singleNativeList.apply {
                    add(
                        NativeAdSingleModel(
                            nativeControllerConfig.key,
                            NativeAdSingleController()
                        )
                    )
                }
                index = singleNativeList.indexOfFirst { it.key == nativeControllerConfig.key }
            }
            if (index in singleNativeList.indices) {
                singleNativeList[index].controller?.loadNewNativeAd(
                    context = mContext,
                    nativeControllerConfig = nativeControllerConfig
                )
            }
        }
    }
}