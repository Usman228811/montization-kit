package io.monetize.kit.sdk.ads.native_ad

import android.app.Activity
import io.monetize.kit.sdk.core.utils.AdSdkInternetController
import io.monetize.kit.sdk.core.utils.AdSdkPref
import io.monetize.kit.sdk.core.utils.adtype.NativeControllerConfig
import io.monetize.kit.sdk.core.utils.consent.AdSdkConsentManager

class AdSdkNativePreloadHelper(
    private val internetController: AdSdkInternetController,
    private val mySharedPreference: AdSdkPref,
    private val mConsent: AdSdkConsentManager,
) {
    fun preLoadNativeAd(mContext: Activity, nativeControllerConfig: NativeControllerConfig) {
        if (nativeControllerConfig.isAdEnable && mConsent.canRequestAds) {
            var index = singleNativeList.indexOfFirst { it.key == nativeControllerConfig.key }
            if (index == -1) {
                singleNativeList.apply {
                    add(
                        NativeAdSingleModel(
                            nativeControllerConfig.key,
                            NativeAdSingleController(
                                mySharedPreference, internetController, consentManager = mConsent
                            )
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