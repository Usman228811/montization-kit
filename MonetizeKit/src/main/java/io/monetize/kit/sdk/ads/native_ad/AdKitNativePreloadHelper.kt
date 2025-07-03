package io.monetize.kit.sdk.ads.native_ad

import android.app.Activity
import android.content.Context
import io.monetize.kit.sdk.core.utils.AdKitInternetController
import io.monetize.kit.sdk.core.utils.AdKitPref
import io.monetize.kit.sdk.core.utils.adtype.NativeControllerConfig
import io.monetize.kit.sdk.core.utils.consent.AdKitConsentManager

class AdKitNativePreloadHelper private constructor(
    private val internetController: AdKitInternetController,
    private val mySharedPreference: AdKitPref,
    private val mConsent: AdKitConsentManager,
    private val customLayoutHelper: AdsCustomLayoutHelper,
    private val nativeCommonHelper: AdKitNativeCommonHelper,
) {

    companion object {
        @Volatile
        private var instance: AdKitNativePreloadHelper? = null


        fun getInstance(
            context: Context
        ): AdKitNativePreloadHelper {
            return instance ?: synchronized(this) {
                instance ?: AdKitNativePreloadHelper(
                    AdKitInternetController.getInstance(context),
                    AdKitPref.getInstance(context),
                    AdKitConsentManager.getInstance(context),
                    AdsCustomLayoutHelper.getInstance(),
                    AdKitNativeCommonHelper.getInstance(),

                    ).also { instance = it }
            }
        }
    }

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