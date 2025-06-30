package io.monetize.kit.sdk.ads.native_ad

import android.content.Context
import android.widget.LinearLayout
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import io.monetize.kit.sdk.core.utils.AdKitInternetController
import io.monetize.kit.sdk.core.utils.AdKitPref
import io.monetize.kit.sdk.core.utils.adtype.AdType
import io.monetize.kit.sdk.core.utils.adtype.NativeControllerConfig
import io.monetize.kit.sdk.core.utils.consent.AdKitConsentManager


data class NativeAdSingleModel(
    val key: String = "",
    val controller: NativeAdSingleController? = null,
)

val singleNativeList = ArrayList<NativeAdSingleModel>()

class NativeAdSingleController(
    private val prefs: AdKitPref,
    private val internetController: AdKitInternetController,
    private val consentManager: AdKitConsentManager,
    private val customLayoutHelper: AdsCustomLayoutHelper,
    private val nativeCommonHelper: AdKitNativeCommonHelper,
) {
    private var canRequestLargeAd = true
    private var largeAndSmallNativeAd: NativeAd? = null
    private var adControllerListener: AdControllerListener? = null
    private lateinit var nativeControllerConfig: NativeControllerConfig


    fun hasLargeAdOrLoading(): Boolean {
        return (largeAndSmallNativeAd != null || !canRequestLargeAd)
    }

    fun setNativeControllerListener(listener: AdControllerListener?) {
        adControllerListener?.resetRequesting()
        adControllerListener = listener
    }


    fun loadNativeAd(
        context: Context, enable: Boolean
    ) {

        try {
            if (enable && !prefs.isAppPurchased && internetController.isConnected && consentManager.canRequestAds) {
                if (largeAndSmallNativeAd == null) {
                    if (!canRequestLargeAd) {
                        return
                    }
                    canRequestLargeAd = false
//                    if (BuildConfig.DEBUG) {
//                        Toast.makeText(context, "large native ad calling", Toast.LENGTH_SHORT)
//                            .show()
//                    }

                    val builder = AdLoader.Builder(
                        context, if (nativeControllerConfig.key == "native_common") {
                            nativeCommonHelper.getNativeAdId()
                        } else {
                            nativeControllerConfig.adId
                        }
                    )
                    builder.forNativeAd { newNativeAd: NativeAd ->
                        canRequestLargeAd = true
//                        if (BuildConfig.DEBUG) {
////                            Constants.showToast(context, "native ad loaded $admobNativeIdCount  ")
//                        }
                        largeAndSmallNativeAd = newNativeAd
                        adControllerListener?.onAdLoaded()
                    }
                    builder.withNativeAdOptions(
                        NativeAdOptions.Builder().setVideoOptions(
                            VideoOptions.Builder().setStartMuted(true).build()
                        ).build()
                    )


                    val adLoader = builder.withAdListener(object : AdListener() {
                        override fun onAdFailedToLoad(p0: LoadAdError) {
                            super.onAdFailedToLoad(p0)
                            canRequestLargeAd = true
                            largeAndSmallNativeAd = null
//                            if (BuildConfig.DEBUG) {
//                                Toast.makeText(
//                                    context,
//                                    "large native load failed ==> code " + loadAdError.code,
//                                    Toast.LENGTH_SHORT
//                                ).show()
//                            }
                            adControllerListener?.onAdFailed()
                        }
                    }).build()
                    adLoader.loadAd(AdManagerAdRequest.Builder().build())
                }
            } else {
                adControllerListener?.onAdFailed()
            }
        } catch (ignored: Exception) {
        }
    }


    fun populateNativeAd(
        context: Context,
        nativeControllerConfig: NativeControllerConfig,
        adFrame: LinearLayout,
        loadNewAd: Boolean = true,
        populateCallback: (NativeAd) -> Unit

    ) {
        this.nativeControllerConfig = nativeControllerConfig
        if (nativeControllerConfig.isAdEnable && !prefs.isAppPurchased && largeAndSmallNativeAd != null) {
            largeAndSmallNativeAd?.let {
                try {
                    try {
                        addNativeAdView(
                            adsCustomLayoutHelper = customLayoutHelper,
                            adType = AdType.entries.filter { entries -> entries.type == nativeControllerConfig.adType.toInt() }[0],
                            context = context,
                            adFrame = adFrame,
                            ad = it,
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    populateCallback.invoke(it)
                    largeAndSmallNativeAd = null
                    if (loadNewAd) {
                        loadNativeAd(context, nativeControllerConfig.isAdEnable)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            loadNativeAd(context, nativeControllerConfig.isAdEnable)
        }
    }


    fun loadNewNativeAd(
        nativeControllerConfig: NativeControllerConfig, context: Context
    ) {
        this.nativeControllerConfig = nativeControllerConfig
        setNativeControllerListener(null)
        loadNativeAd(context, nativeControllerConfig.isAdEnable)
    }
}


