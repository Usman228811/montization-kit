package io.monetize.kit.sdk.ads.banner

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.google.android.libraries.ads.mobile.sdk.banner.AdView
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRequest
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdValue
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import io.monetize.kit.sdk.ads.native_ad.AdControllerListener
import io.monetize.kit.sdk.core.utils.adtype.BannerAdType
import io.monetize.kit.sdk.core.utils.adtype.BannerControllerConfig
import io.monetize.kit.sdk.core.utils.appflyer.postAdImpression
import io.monetize.kit.sdk.core.utils.appflyer.revenueListener
import io.monetize.kit.sdk.core.utils.firebaseBoolean
import io.monetize.kit.sdk.core.utils.init.AdKit

val singleBannerList = ArrayList<BannerSingleAdControllerModel>()


data class BannerSingleAdControllerModel(
    val controller: AdKitBannerController? = null,
    val key: String = ""
)

class AdKitBannerController {
    private var placementKey: String = ""
    private var adIdKey: String = ""
    private var canRequestBannerAd = true
    private var isAdEnable = false
    private var adView: AdView? = null
    private var adControllerListener: AdControllerListener? = null
    private var onAdClick: (() -> Unit)? = null
    private var bannerType = BannerAdType.ADAPTIVE_BANNER.name

    companion object {
        private const val TAG = "AdKitBannerController"
    }


    fun setAdControllerListener(listener: AdControllerListener?) {
        adControllerListener?.resetRequesting()
        adControllerListener = listener
    }


    fun loadNewBannerAd(
        context: Activity, bannerControllerConfig: BannerControllerConfig,
        bannerType: String
    ) {
        this.isAdEnable = firebaseBoolean(
            "${bannerControllerConfig.placementKey}_isAdEnable",
            false
        )
        this.bannerType = bannerType
        this.placementKey = bannerControllerConfig.placementKey
        setAdControllerListener(null)
        this.adIdKey = bannerControllerConfig.adIdKey
        loadBannerAd(context, isAdEnable)
    }


    private fun loadBannerAd(
        context: Activity, enable: Boolean
    ) {
        try {
            if (enable && !AdKit.adKitPref.isAppPurchased && AdKit.internetController.isConnected && AdKit.consentManager.canRequestAds) {
                if (adView == null) {
                    if (!canRequestBannerAd) {
                        return
                    }
                    canRequestBannerAd = false
                    val id = AdKit.bannerIdManager.getNextBannerId(adIdKey) ?: ""
                    val adSize = getAdSize(context, bannerType)
                    val adRequest = BannerAdRequest.Builder(id, adSize).build()

                    val bannerAd = AdView(context)
                    bannerAd.loadAd(
                        adRequest,
                        object : AdLoadCallback<BannerAd> {

                            override fun onAdLoaded(ad: BannerAd) {

                                context.runOnUiThread {

                                    canRequestBannerAd = true
                                    adView = bannerAd

                                    adControllerListener?.onAdLoaded()
                                    ad.adEventCallback =
                                        object : BannerAdEventCallback {

                                            override fun onAdPaid(value: AdValue) {
                                                super.onAdPaid(value)
                                                revenueListener(id, value, "Banner")
                                            }

                                            override fun onAdImpression() {

                                                context.runOnUiThread {
                                                    postAdImpression("Banner")
                                                }
                                            }

                                            override fun onAdClicked() {

                                                context.runOnUiThread {
                                                    onAdClick?.invoke()
                                                }
                                            }

                                            override fun onAdShowedFullScreenContent() {

                                            }

                                            override fun onAdDismissedFullScreenContent() {

                                            }

                                            override fun onAdFailedToShowFullScreenContent(
                                                fullScreenContentError: FullScreenContentError
                                            ) {

                                            }
                                        }
                                }

                            }


                            override fun onAdFailedToLoad(adError: LoadAdError) {

                                context.runOnUiThread {
                                    canRequestBannerAd = true
                                    adView = null
                                    adControllerListener?.onAdFailed("$placementKey is failed with code: ${adError.code}, message: ${adError.message}")
                                }
                            }
                        },
                    )

//                        .apply {
//                        this.adUnitId = id
//                        this.setAdSize(
//                            getAdSize(
//                                context,
//                                bannerType
//                            )
//                        )
//                        this.loadAd(AdRequest.Builder().build())
//                    }
//                    bannerAd.adListener = object : AdListener() {
//                        override fun onAdLoaded() {
//                            super.onAdLoaded()
//                            canRequestBannerAd = true
//                            adView = bannerAd
//                            adView?.revenueListener(id)
//
//                            adControllerListener?.onAdLoaded()
//                        }
//
//                        override fun onAdClicked() {
//                            super.onAdClicked()
//                            onAdClick?.invoke()
//                        }
//
//                        override fun onAdImpression() {
//                            super.onAdImpression()
//                           postAdImpression("Banner")
//                        }
//
//                        override fun onAdFailedToLoad(p0: LoadAdError) {
//                            super.onAdFailedToLoad(p0)
//                            canRequestBannerAd = true
//                            adView = null
//                            adControllerListener?.onAdFailed("$placementKey is failed with code: ${p0.code}, message: ${p0.message}")
//
//                        }
//                    }
                }
            } else {
                adControllerListener?.onAdFailed("$placementKey can't request ad because of internet connection | consent manager | app purchased | ad is disable in remote config")
            }
        } catch (_: Exception) {
        }
    }

    fun populateBannerAd(
        context: Activity, placementKey: String, adIdKey: String, enable: Boolean,
        adFrame: LinearLayout, loadNewAd: Boolean = false,
        bannerType: String,
        populateCallback: (Any) -> Unit,
        onAdClick: () -> Unit,
    ) {
        try {
            this.bannerType = bannerType
            this.onAdClick = onAdClick
            this.adIdKey = adIdKey
            this.placementKey = placementKey
            if (enable && !AdKit.adKitPref.isAppPurchased && adView != null) {
                adView?.let {
                    try {
                        adFrame.visibility = View.VISIBLE
                        try {
                            it.parent?.let { parent ->
                                (parent as ViewGroup).removeAllViews()
                            }
                            adFrame.removeAllViews()
                        } catch (_: Exception) {
                        }
                        adFrame.addView(it)
                        populateCallback.invoke(it)
                        adView = null
                        if (loadNewAd) {
                            loadBannerAd(context, enable)
                        }
                    } catch (_: Exception) {
                    }
                }
            } else {
                loadBannerAd(context, enable)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
