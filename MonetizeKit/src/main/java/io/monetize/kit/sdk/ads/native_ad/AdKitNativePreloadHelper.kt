package io.monetize.kit.sdk.ads.native_ad

import android.app.Activity
import io.monetize.kit.sdk.core.utils.AdKitInternetController
import io.monetize.kit.sdk.core.utils.AdKitPref
import io.monetize.kit.sdk.core.utils.adtype.NativeControllerConfig
import io.monetize.kit.sdk.core.utils.consent.AdKitConsentManager

class AdKitNativePreloadHelper(
    private val internetController: AdKitInternetController,
    private val mySharedPreference: AdKitPref,
    private val mConsent: AdKitConsentManager,
    private val customLayoutHelper: AdsCustomLayoutHelper,
    private val nativeCommonHelper: AdKitNativeCommonHelper,
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
                                prefs = mySharedPreference,
                                internetController = internetController,
                                consentManager = mConsent,
                                customLayoutHelper = customLayoutHelper,
                                nativeCommonHelper = nativeCommonHelper
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