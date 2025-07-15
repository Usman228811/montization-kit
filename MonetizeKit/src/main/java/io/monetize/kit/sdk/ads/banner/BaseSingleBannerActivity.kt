package io.monetize.kit.sdk.ads.banner


import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.google.android.gms.ads.AdView
import io.monetize.kit.sdk.ads.native_ad.AdControllerListener
import io.monetize.kit.sdk.ads.native_ad.addShimmerLayout
import io.monetize.kit.sdk.core.utils.adtype.AdType
import io.monetize.kit.sdk.core.utils.adtype.BannerControllerConfig
import io.monetize.kit.sdk.core.utils.init.AdKit
import io.monetize.kit.sdk.core.utils.init.AdKit.consentManager
import io.monetize.kit.sdk.core.utils.init.AdKit.internetController

class BaseSingleBannerActivity private constructor(
) {
    private var bannerAd: AdView? = null
    private var adFrame: LinearLayout? = null
    private var model: BannerSingleAdControllerModel? = null

    private var isAdLoadCalled: Boolean = false
    private var isRequesting: Boolean = false
    private lateinit var mContext: Activity
    private lateinit var bannerControllerConfig: BannerControllerConfig

    private var onFail: (() -> Unit)? = null

    companion object {

        fun getInstance(
        ): BaseSingleBannerActivity {
            return BaseSingleBannerActivity()
        }
    }


    fun initSingleBannerData(
        mContext: Activity,
        adFrame: LinearLayout,
        bannerControllerConfig: BannerControllerConfig,
        onFail: () -> Unit
    ) {
        this.bannerControllerConfig = bannerControllerConfig
        this.mContext = mContext
        this.onFail = onFail
        try {
            adFrame.descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
        } catch (_: Exception) {
        }
        this.adFrame = adFrame
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
            adFrame.let {
                it.visibility = View.GONE
                it.removeAllViews()
            }
        }

    }

    private fun destroyBannerAd() {
        try {
            bannerAd?.destroy()
            bannerAd = null
            model?.controller?.setAdControllerListener(null)
        } catch (_: Exception) {
        }
    }

    private fun loadSingleBannerAd() {
        if (isAdLoadCalled) {
            if (adFrame == null ||
                AdKit.firebaseHelper.getBoolean("${bannerControllerConfig.placementKey}_isAdEnable", true).not() ||
                AdKit.adKitPref.isAppPurchased
                || consentManager.canRequestAds.not()
                || internetController.isConnected.not()
            ) {
                adFrame?.let {
                    it.visibility = View.GONE
                    it.removeAllViews()
                }
            } else {
                model?.controller?.let { controller ->

                    adFrame?.let { adFrame ->
                        if (bannerAd == null) {
                            if (!isRequesting) {
                                isRequesting = true
                                addShimmerLayout(
                                    mContext, adFrame, AdType.BANNER
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

                                    override fun onAdFailed() {
                                        onFail?.invoke()
                                        isRequesting = false
                                        if (mContext.isFinishing || mContext.isDestroyed || mContext.isChangingConfigurations) {
                                            return
                                        }
                                        adFrame.let {
                                            it.visibility = View.GONE
                                            it.removeAllViews()
                                        }
                                    }

                                    override fun resetRequesting() {
                                        isRequesting = false
                                    }
                                })
                                controller.populateBannerAd(
                                    context = mContext,
                                    placementKey = bannerControllerConfig.placementKey,
                                    adIdKey = bannerControllerConfig.adIdKey,
                                    enable = AdKit.firebaseHelper.getBoolean("${bannerControllerConfig.placementKey}_isAdEnable", true),
                                    adFrame = adFrame,
                                    loadNewAd = AdKit.firebaseHelper.getBoolean("${bannerControllerConfig.placementKey}_loadNewAd", false)
                                ) { ad ->
                                    isRequesting = false
                                    if (!mContext.isFinishing && !mContext.isDestroyed && !mContext.isChangingConfigurations) {
                                        controller.setAdControllerListener(null)
                                        bannerAd = ad as AdView

                                    }
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
                        }
                    }
                }
            }
        }
    }

    fun onResume() {
        loadSingleBannerAd()
        bannerAd?.resume()
    }

    fun onPause() {
        bannerAd?.pause()
    }

    fun onDestroy() {
        destroyBannerAd()
    }
}