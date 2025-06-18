package io.monetize.kit.sdk.ads.native_ad

import android.app.Activity
import io.monetize.kit.sdk.core.utils.MKInternetController
import io.monetize.kit.sdk.core.utils.MkPref
import io.monetize.kit.sdk.core.utils.adtype.NativeControllerConfig
import io.monetize.kit.sdk.core.utils.consent.MKConsentManager

class AdPreLoad(
    private val internetController: MKInternetController,
    private val mySharedPreference: MkPref,
    private val mConsent: MKConsentManager,
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