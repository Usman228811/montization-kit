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
import io.monetize.kit.sdk.core.utils.adtype.AdType
import io.monetize.kit.sdk.core.utils.adtype.NativeControllerConfig
import io.monetize.kit.sdk.core.utils.appflyer.postAdImpression
import io.monetize.kit.sdk.core.utils.appflyer.revenueListener
import io.monetize.kit.sdk.core.utils.firebaseBoolean
import io.monetize.kit.sdk.core.utils.firebaseLong
import io.monetize.kit.sdk.core.utils.init.AdKit
import io.monetize.kit.sdk.core.utils.init.AdKit.adKitPref
import io.monetize.kit.sdk.core.utils.init.AdKit.consentManager
import io.monetize.kit.sdk.core.utils.init.AdKit.internetController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


data class NativeAdSingleModel(
    val key: String = "",
    val controller: NativeAdSingleController? = null,
)

val singleNativeList = ArrayList<NativeAdSingleModel>()

class NativeAdSingleController {
    private var canRequestLargeAd = true
    private var largeAndSmallNativeAd: NativeAd? = null
    private var adControllerListener: AdControllerListener? = null
    private var nativeRefreshListener: NativeRefreshListener? = null
    private lateinit var nativeControllerConfig: NativeControllerConfig
    private var isAdEnable = true
    private var onAdClick: (() -> Unit)? = null
    private var canRefreshAd = true


    fun hasLargeAdOrLoading(): Boolean {
        return (largeAndSmallNativeAd != null || !canRequestLargeAd)
    }

    fun hasNativeAd(): Boolean {
        return largeAndSmallNativeAd != null
    }

    fun setNativeControllerListener(listener: AdControllerListener?) {
        adControllerListener?.resetRequesting()
        adControllerListener = listener
    }

    fun setNativeRefreshListener(listener: NativeRefreshListener?) {
        nativeRefreshListener = listener
    }


    fun loadNativeAd(
        context: Context, enable: Boolean
    ) {

        try {
            if (enable && !adKitPref.isAppPurchased && internetController.isConnected && consentManager.canRequestAds) {
                if (largeAndSmallNativeAd == null) {
                    if (!canRequestLargeAd) {
                        return
                    }
                    canRequestLargeAd = false


                    val builder = AdLoader.Builder(
                        context,
                        AdKit.nativeIdManager.getNextNativeId(placement = nativeControllerConfig.adIdKey)
                            ?: ""
                    )
                    builder.forNativeAd { newNativeAd: NativeAd ->
                        canRequestLargeAd = true
                        largeAndSmallNativeAd = newNativeAd
                        CoroutineScope(Dispatchers.Main).launch {
                            largeAndSmallNativeAd?.revenueListener(
                                AdKit.nativeIdManager.getNextNativeId(placement = nativeControllerConfig.adIdKey)
                                    ?: ""
                            )
                        }

                        adControllerListener?.onAdLoaded()
                    }
                    builder.withNativeAdOptions(
                        NativeAdOptions.Builder().setVideoOptions(
                            VideoOptions.Builder().setStartMuted(true).build()
                        ).build()
                    )


                    val adLoader = builder.withAdListener(object : AdListener() {

                        override fun onAdClicked() {
                            super.onAdClicked()
                            onAdClick?.invoke()
                        }

                        override fun onAdImpression() {
                            super.onAdImpression()
                           postAdImpression("NativeAd")
                        }

                        override fun onAdFailedToLoad(p0: LoadAdError) {
                            super.onAdFailedToLoad(p0)
                            canRequestLargeAd = true
                            largeAndSmallNativeAd = null
                            adControllerListener?.onAdFailed("${nativeControllerConfig.placementKey} is failed with code: ${p0.code}, message: ${p0.message}")
                        }
                    }).build()
                    adLoader.loadAd(AdManagerAdRequest.Builder().build())
                }
            } else {
                adControllerListener?.onAdFailed("${nativeControllerConfig.placementKey} can't request ad because of internet connection | consent manager | app purchased | ad is disable in remote config")
            }
        } catch (_: Exception) {
        }
    }


    fun requestNativeAd(
        context: Context,
        nativeControllerConfig: NativeControllerConfig,
    ) {
        this.isAdEnable =
            firebaseBoolean("${nativeControllerConfig.placementKey}_isAdEnable", true)
        this.nativeControllerConfig = nativeControllerConfig
        if (isAdEnable && !adKitPref.isAppPurchased ) {
            if (largeAndSmallNativeAd == null) {
                loadNativeAd(context, isAdEnable)
            }else{
                adControllerListener?.onAdLoaded()
            }
        }
    }

    fun populateNativeAd(
        context: Context,
        adFrame: LinearLayout,
        loadNewAd: Boolean = true,
        onPopulated: (NativeAd) -> Unit,
        onAdClick: () -> Unit,
    ) {

        this.onAdClick = onAdClick
        largeAndSmallNativeAd?.let {
            try {
                try {
                    addNativeAdView(
                        nativeControllerConfig = nativeControllerConfig,
                        adsCustomLayoutHelper = AdKit.nativeCustomLayoutHelper,
                        adType = AdType.entries.filter { entries ->
                            entries.type == firebaseLong(
                                "${nativeControllerConfig.placementKey}_adType",
                                0
                            ).toInt()
                        }[0],
                        context = context,
                        adFrame = adFrame,
                        ad = it,
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                onPopulated.invoke(it)
                largeAndSmallNativeAd = null
                if (loadNewAd) {
                    loadNativeAd(context, isAdEnable)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    fun preloadNativeAd(
        nativeControllerConfig: NativeControllerConfig, context: Context
    ) {
        this.isAdEnable =
            firebaseBoolean("${nativeControllerConfig.placementKey}_isAdEnable", true)
        this.nativeControllerConfig = nativeControllerConfig
        setNativeControllerListener(null)
        loadNativeAd(context, isAdEnable)
    }

    fun startRefreshTime() {
        val refreshTime = firebaseLong(
            "${this@NativeAdSingleController.nativeControllerConfig.placementKey}_refreshTime",
            0
        ) * 1000
        if (refreshTime > 0 &&
            isAdEnable && !adKitPref.isAppPurchased &&
            consentManager.canRequestAds &&
            internetController.isConnected &&
            canRefreshAd
        ) {
            canRefreshAd = false
            CoroutineScope(Dispatchers.IO).launch {
                delay(
                    refreshTime
                )
                largeAndSmallNativeAd?.destroy()
                withContext(Dispatchers.Main) {
                    largeAndSmallNativeAd = null
                    canRefreshAd = true
                    nativeRefreshListener?.refreshNativeAd()
                }
            }
        }
    }
}


