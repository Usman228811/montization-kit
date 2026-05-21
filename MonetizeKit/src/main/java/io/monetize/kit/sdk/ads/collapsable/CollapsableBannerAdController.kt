package io.monetize.kit.sdk.ads.collapsable


import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import io.monetize.kit.sdk.ads.banner.getAdSize
import io.monetize.kit.sdk.ads.native_ad.addBannerShimmerLayout
import io.monetize.kit.sdk.core.utils.adtype.BannerAdType
import io.monetize.kit.sdk.core.utils.adtype.BannerControllerConfig
import io.monetize.kit.sdk.core.utils.appflyer.postAdImpression
import io.monetize.kit.sdk.core.utils.appflyer.revenueListener
import io.monetize.kit.sdk.core.utils.callbacks.AdCallBack
import io.monetize.kit.sdk.core.utils.firebaseBoolean
import io.monetize.kit.sdk.core.utils.init.AdKit

class CollapsableBannerAdController private constructor(
) {
    private var bannerAd: AdView? = null
    private var adFrame: LinearLayout? = null
    private var isAdLoadCalled: Boolean = false
    private var isTop: Boolean = true
    private var isRequesting: Boolean = false
    private var canLoadAdAgain = true

    private var bannerType: String = BannerAdType.ADAPTIVE_BANNER.name
    private lateinit var mContext: Activity
    private lateinit var bannerControllerConfig: BannerControllerConfig
    private var adCallBack: AdCallBack? = null


    companion object {

        fun getInstance(
        ): CollapsableBannerAdController {
            return CollapsableBannerAdController()
        }
    }

    private fun destroyCollapsableBannerAd() {
        canLoadAdAgain = true
        isRequesting = false
        isAdLoadCalled = false
        bannerAd?.destroy()
        try {
            adFrame?.removeAllViews()
        } catch (_: Exception) {
        }
        adFrame = null
        bannerAd = null
    }

    fun initCollapsableBannerAd(
        mContext: Activity,
        adFrame: LinearLayout,
        bannerControllerConfig: BannerControllerConfig,
        bannerType: String,
        adCallBack: AdCallBack?

    ) {
        this.adCallBack = adCallBack
        this.adFrame = adFrame
        if (AdKit.initializer.getDisableAds()) {
            adCallBack?.onAdFailed("ads are disabled in app class")
            hideFrame()
            return
        }
        this.bannerControllerConfig = bannerControllerConfig
        this.bannerType = bannerType

        isTop = bannerType == BannerAdType.TOP_COLLAPSIBLE_BANNER.name
        this.mContext = mContext
        this.isAdLoadCalled = true
        loadCollapsableBannerAd()
    }


    fun hideFrame() {
        adFrame?.let {
            it.visibility = View.GONE
            it.removeAllViews()
        }
    }

    private fun loadCollapsableBannerAd() {
        if (isAdLoadCalled) {
            if (firebaseBoolean(
                    "${bannerControllerConfig.placementKey}_isAdEnable",
                    false
                ).not()
            ) {
                adCallBack?.onAdFailed("${bannerControllerConfig.placementKey} ad is disable or not added in remote config")
                hideFrame()
            } else if (/*AdKit.consentManager.canRequestAds.not() ||*/ AdKit.adKitPref.isAppPurchased || (!AdKit.internetController.isConnected && bannerAd == null)
            ) {
                adCallBack?.onAdFailed("${bannerControllerConfig.placementKey} can't request ad because of internet connection | consent manager | app purchased")
                destroyCollapsableBannerAd()
                hideFrame()
            } else {
                adFrame?.let { adFrame ->
                    if (canLoadAdAgain) {
                        if (bannerAd == null) {
                            if (isRequesting) {
                                return
                            }
                            isRequesting = true
                            adFrame.descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
                            addBannerShimmerLayout(
                                mContext, adFrame, bannerType
                            )

                            val id =
                                AdKit.bannerIdManager.getNextBannerId(bannerControllerConfig.adIdKey)
                                    ?: ""
                            val collapseBannerAd = AdView(mContext).apply {
                                this.adUnitId = id

                                this.setAdSize(
                                    getAdSize(
                                        mContext,
                                        bannerType
                                    )
                                )
                                this.loadAd(
                                    AdRequest.Builder()
                                        .addNetworkExtrasBundle(
                                            AdMobAdapter::class.java,
                                            Bundle().apply {
                                                if (isTop.not()) {
                                                    putString("collapsible", "bottom")
                                                } else {
                                                    putString("collapsible", "top")

                                                }
                                            }).build()
                                )
                            }
                            collapseBannerAd.adListener = object : AdListener() {
                                override fun onAdLoaded() {
                                    super.onAdLoaded()
                                    if (mContext.isFinishing || mContext.isDestroyed || mContext.isChangingConfigurations) {
                                        collapseBannerAd.destroy()
                                        return
                                    }
                                    isRequesting = false

                                    bannerAd = collapseBannerAd
                                    adFrame.visibility = View.VISIBLE
                                    adFrame.removeAllViews()
                                    adFrame.addView(bannerAd)
                                    adCallBack?.onAdShow()
                                    bannerAd?.revenueListener(
                                        id
                                    )
                                }

                                override fun onAdClicked() {
                                    super.onAdClicked()
                                    adCallBack?.onAdClick()
                                }

                                override fun onAdImpression() {
                                    super.onAdImpression()
                                    postAdImpression("Banner")
                                }

                                override fun onAdFailedToLoad(p0: LoadAdError) {
                                    super.onAdFailedToLoad(p0)
                                    if (mContext.isFinishing || mContext.isDestroyed || mContext.isChangingConfigurations) {
                                        collapseBannerAd.destroy()
                                        return
                                    }
                                    adCallBack?.onAdFailed("${bannerControllerConfig.placementKey} is failed with code: ${p0.code}, message: ${p0.message}")
                                    isRequesting = false
                                    canLoadAdAgain = false
                                    bannerAd = null
                                    hideFrame()
                                }
                            }
                        } else {
                            try {
                                bannerAd?.parent?.let { parent ->
                                    (parent as ViewGroup).removeAllViews()
                                }
                            } catch (_: Exception) {
                            }
                            adFrame.visibility = View.VISIBLE
                            adFrame.removeAllViews()
                            adFrame.addView(bannerAd)
                            adCallBack?.onAdShow()
                        }
                    }
                }
            }
        }
    }

    fun onResume() {
        adFrame?.let {
            loadCollapsableBannerAd()
        }
        if (AdKit.adKitPref.isAppPurchased.not()) {
            bannerAd?.resume()
        }
    }

    fun onPause() {
        canLoadAdAgain = true
        bannerAd?.pause()
    }

    fun onDestroy() {
        destroyCollapsableBannerAd()
    }
}