package io.monetize.kit.sdk.ads.banner


import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.google.android.gms.ads.AdView
import io.monetize.kit.sdk.ads.native_ad.AdControllerListener
import io.monetize.kit.sdk.ads.native_ad.addBannerShimmerLayout
import io.monetize.kit.sdk.core.utils.adtype.BannerAdType
import io.monetize.kit.sdk.core.utils.adtype.BannerControllerConfig
import io.monetize.kit.sdk.core.utils.callbacks.AdCallBack
import io.monetize.kit.sdk.core.utils.firebaseBoolean
import io.monetize.kit.sdk.core.utils.init.AdKit
import io.monetize.kit.sdk.core.utils.init.AdKit.adKitPref
import io.monetize.kit.sdk.core.utils.init.AdKit.consentManager
import io.monetize.kit.sdk.core.utils.init.AdKit.internetController

class BannerAdController private constructor(
) {
    private var bannerAd: AdView? = null
    private var adFrame: LinearLayout? = null
    private var model: BannerSingleAdControllerModel? = null
    private var canLoadAdAgain = true

    private var isAdLoadCalled: Boolean = false
    private var bannerType: String = BannerAdType.ADAPTIVE_BANNER.name
    private var isRequesting: Boolean = false
    private lateinit var mContext: Activity
    private lateinit var bannerControllerConfig: BannerControllerConfig
    private var adCallBack: AdCallBack? = null


    private var isAdEnable = false

    companion object {

        fun getInstance(
        ): BannerAdController {
            return BannerAdController()
        }
    }


    fun initSingleBannerData(
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
        this.isAdEnable = firebaseBoolean(
            "${bannerControllerConfig.placementKey}_isAdEnable",
            false
        )
        this.bannerControllerConfig = bannerControllerConfig
        this.mContext = mContext
        try {
            adFrame.descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
        } catch (_: Exception) {
        }
        this.bannerType = bannerType

        isAdLoadCalled = true

        var index = singleBannerList.indexOfFirst { it.key == bannerControllerConfig.adIdKey }
        if (index == -1) {
            singleBannerList.add(
                BannerSingleAdControllerModel(
                    AdKitBannerController(
                    ), bannerControllerConfig.adIdKey
                )
            )

            index = singleBannerList.indexOfFirst { it.key == bannerControllerConfig.adIdKey }
        }

        if (index != -1) {
            model = singleBannerList[index]
            loadSingleBannerAd()
        } else {
            hideFrame()
        }

    }

    private fun hideFrame(){
        adFrame?.let {
            it.visibility = View.GONE
            it.removeAllViews()
        }
    }

    private fun destroyBannerAd() {
        try {
            canLoadAdAgain = true
            bannerAd?.destroy()
            bannerAd = null
        } catch (_: Exception) {
        }
    }

    private fun loadSingleBannerAd() {
        if (isAdLoadCalled) {
            if (!isAdEnable
            ) {
                adCallBack?.onAdFailed("${bannerControllerConfig.placementKey} ad is disable or not added in remote config")
                hideFrame()
            } else if (adFrame == null
                || AdKit.adKitPref.isAppPurchased
                || consentManager.canRequestAds.not()
                || internetController.isConnected.not()
            ) {
                adCallBack?.onAdFailed("${bannerControllerConfig.placementKey} can't request ad because of internet connection | consent manager | app purchased")
                hideFrame()
            } else {
                model?.controller?.let { controller ->

                    adFrame?.let { adFrame ->
                        if (canLoadAdAgain) {
                            if (bannerAd == null) {
                                if (!isRequesting) {
                                    isRequesting = true
                                    addBannerShimmerLayout(
                                        mContext, adFrame,
                                        bannerType
                                    )
                                    controller.setAdControllerListener(object :
                                        AdControllerListener {
                                        override fun onAdLoaded() {
                                            isRequesting = false
                                            if (mContext.isFinishing || mContext.isDestroyed || mContext.isChangingConfigurations) {
                                                return
                                            }
                                            if (bannerAd == null) {
                                                loadSingleBannerAd()
                                            }
                                        }

                                        override fun onAdFailed(reason: String) {
                                            adCallBack?.onAdFailed(reason)
                                            isRequesting = false
                                            canLoadAdAgain = false
                                            if (mContext.isFinishing || mContext.isDestroyed || mContext.isChangingConfigurations) {
                                                return
                                            }
                                            hideFrame()
                                        }

                                        override fun resetRequesting() {
                                            isRequesting = false
                                        }
                                    }, "when request")
                                    controller.populateBannerAd(
                                        context = mContext,
                                        placementKey = bannerControllerConfig.placementKey,
                                        adIdKey = bannerControllerConfig.adIdKey,
                                        enable = isAdEnable,
                                        adFrame = adFrame,
                                        bannerType = bannerType,
                                        loadNewAd = firebaseBoolean(
                                            "${bannerControllerConfig.adIdKey}_loadNewAd",
                                            false
                                        ),
                                        populateCallback = { ad ->
                                            isRequesting = false
                                            if (!mContext.isFinishing && !mContext.isDestroyed && !mContext.isChangingConfigurations) {
                                                controller.setAdControllerListener(null, "after populated")
                                                bannerAd = ad as AdView
                                                adCallBack?.onAdShow()

                                            }
                                        }, onAdClick = {
                                            adCallBack?.onAdClick()

                                        }
                                    )
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
    }

    fun onResume() {
        if (bannerAd == null) {
            loadSingleBannerAd()
        } else {
            if (adKitPref.isAppPurchased.not()) {

                adFrame?.let { adFrame ->
                    try {
                        bannerAd?.parent?.let { parent ->
                            (parent as ViewGroup).removeAllViews()
                        }
                    } catch (_: Exception) {
                    }
                    adFrame.visibility = View.VISIBLE
                    adFrame.removeAllViews()
                    adFrame.addView(bannerAd)
                    bannerAd?.resume()
                }
            } else {
                hideFrame()
            }
        }
    }

    fun onPause() {
        canLoadAdAgain = true
        model?.controller?.setAdControllerListener(null, "ondestroy")
        bannerAd?.pause()
    }

    fun onDestroy() {
        destroyBannerAd()
    }
}