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
import io.monetize.kit.sdk.core.utils.adtype.BannerControllerConfig
import io.monetize.kit.sdk.core.utils.appflyer.revenueListener
import io.monetize.kit.sdk.core.utils.callbacks.AdCallBack
import io.monetize.kit.sdk.core.utils.firebaseBoolean
import io.monetize.kit.sdk.core.utils.init.AdKit

class BaseCollapsableBannerActivity private constructor(
) {
    private var bannerAd: AdView? = null
    private var adFrame: LinearLayout? = null
    private var isAdLoadCalled: Boolean = false
    private var isTop: Boolean = true
    private var isRequesting: Boolean = false
    private var canLoadAdAgain = true

    private var bannerType: Long = 0L
    private lateinit var mContext: Activity
    private lateinit var bannerControllerConfig: BannerControllerConfig
    private var adCallBack: AdCallBack? = null


    companion object {

        fun getInstance(
        ): BaseCollapsableBannerActivity {
            return BaseCollapsableBannerActivity()
        }
    }

    private fun destroyCollapsableBannerAd() {
        canLoadAdAgain = true
        bannerAd?.destroy()
        try {
            adFrame?.removeAllViews()
        } catch (_: Exception) {
        }
        bannerAd = null
    }

    fun initCollapsableBannerAd(
        mContext: Activity,
        adFrame: LinearLayout,
        bannerControllerConfig: BannerControllerConfig,
        bannerType: Long,
        adCallBack: AdCallBack?

    ) {
        this.adCallBack = adCallBack
        if (AdKit.initializer.getDisableAds()) {
            adCallBack?.onAdFailed("ads are disabled in app class")
            adFrame.let {
                it.visibility = View.GONE
                it.removeAllViews()
            }
            return
        }
        this.bannerControllerConfig = bannerControllerConfig
        this.bannerType = bannerType

        isTop = bannerType == 4L
        this.mContext = mContext
        this.adFrame = adFrame
        this.isAdLoadCalled = true
        loadCollapsableBannerAd()
    }

    private fun loadCollapsableBannerAd() {
        if (isAdLoadCalled) {
            if (firebaseBoolean(
                    "${bannerControllerConfig.placementKey}_isAdEnable",
                    false
                ).not()
            ) {
                adCallBack?.onAdFailed("${bannerControllerConfig.placementKey} ad is disable or not added in remote config")
                adFrame?.let {
                    it.visibility = View.GONE
                    it.removeAllViews()
                }
            }

            if (AdKit.consentManager.canRequestAds.not() || AdKit.adKitPref.isAppPurchased || (!AdKit.internetController.isConnected && bannerAd == null)
            ) {
                adCallBack?.onAdFailed("${bannerControllerConfig.placementKey} can't request ad because of internet connection | consent manager | app purchased")
                destroyCollapsableBannerAd()
                adFrame?.let {
                    it.visibility = View.GONE
                    it.removeAllViews()
                }
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

                            val collapseBannerAd = AdView(mContext).apply {
                                this.adUnitId =
                                    AdKit.bannerIdManager.getNextBannerId(bannerControllerConfig.adIdKey)
                                        ?: ""
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
                                        AdKit.bannerIdManager.getNextBannerId(bannerControllerConfig.adIdKey)
                                            ?: ""
                                    )
                                }

                                override fun onAdClicked() {
                                    super.onAdClicked()
                                    adCallBack?.onAdClick()
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
                                    adFrame.removeAllViews()
                                    adFrame.visibility = View.GONE
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
        loadCollapsableBannerAd()
        bannerAd?.resume()
    }

    fun onPause() {
        canLoadAdAgain = true
        bannerAd?.pause()
    }

    fun onDestroy() {
        destroyCollapsableBannerAd()
    }
}