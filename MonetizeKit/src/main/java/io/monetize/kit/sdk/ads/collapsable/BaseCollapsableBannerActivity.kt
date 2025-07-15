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
import io.monetize.kit.sdk.ads.native_ad.addShimmerLayout
import io.monetize.kit.sdk.core.utils.adtype.AdType
import io.monetize.kit.sdk.core.utils.adtype.BannerControllerConfig
import io.monetize.kit.sdk.core.utils.init.AdKit

class BaseCollapsableBannerActivity private constructor(
) {
    private var bannerAd: AdView? = null
    private var adFrame: LinearLayout? = null
    private var isAdLoadCalled: Boolean = false
    private var isTop: Boolean = true
    private var isRequesting: Boolean = false
    private lateinit var mContext: Activity
    private lateinit var bannerControllerConfig: BannerControllerConfig

    private var onFail: (() -> Unit)? = null

    companion object {

        fun getInstance(
        ): BaseCollapsableBannerActivity {
            return BaseCollapsableBannerActivity()
        }
    }

    private fun destroyCollapsableBannerAd() {
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
        onFail: () -> Unit,
    ) {
        this.bannerControllerConfig = bannerControllerConfig
        isTop = AdKit.firebaseHelper.getBoolean("${bannerControllerConfig.placementKey}_isCollapsibleTop", false)
        this.onFail = onFail
        this.mContext = mContext
        this.adFrame = adFrame
        this.isAdLoadCalled = true
        loadCollapsableBannerAd()
    }

    private fun loadCollapsableBannerAd() {
        if (isAdLoadCalled) {
            if (AdKit.firebaseHelper.getBoolean("${bannerControllerConfig.placementKey}_isAdEnable", true).not() || AdKit.consentManager.canRequestAds.not() || AdKit.adKitPref.isAppPurchased || (!AdKit.internetController.isConnected && bannerAd == null)) {
                destroyCollapsableBannerAd()
                adFrame?.let {
                    it.visibility = View.GONE
                    it.removeAllViews()
                }
            } else {
                adFrame?.let { adFrame ->
                    if (bannerAd == null) {
                        if (isRequesting) {
                            return
                        }
                        isRequesting = true
                        adFrame.descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
                        addShimmerLayout(
                            mContext, adFrame, AdType.BANNER
                        )
                        /*if (BuildConfig.DEBUG) {
                            Constants.showToast(mContext, "collapse banner ad calling")
                        }*/
                        val collapseBannerAd = AdView(mContext).apply {
                            this.adUnitId =
                                AdKit.bannerIdManager.getNextBannerId(bannerControllerConfig.placementKey)
                                    ?: ""
                            this.setAdSize(getAdSize(mContext))
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
//                                if (BuildConfig.DEBUG) {
//                                    Toast.makeText(
//                                        mContext, "collapse banner ad loaded", Toast.LENGTH_SHORT
//                                    ).show()
//                                }
                            }

                            override fun onAdFailedToLoad(p0: LoadAdError) {
                                super.onAdFailedToLoad(p0)
                                if (mContext.isFinishing || mContext.isDestroyed || mContext.isChangingConfigurations) {
                                    collapseBannerAd.destroy()
                                    return
                                }
                                onFail?.invoke()
                                isRequesting = false
                                bannerAd = null
                                adFrame.removeAllViews()
                                adFrame.visibility = View.GONE
//                                if (BuildConfig.DEBUG) {
//                                    Toast.makeText(
//                                        mContext,
//                                        "collapse banner load failed ==> code " + p0.code,
//                                        Toast.LENGTH_SHORT
//                                    ).show()
//                                }
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

    fun onResume() {
        loadCollapsableBannerAd()
        bannerAd?.resume()
    }

    fun onPause() {
        bannerAd?.pause()
    }

    fun onDestroy() {
        destroyCollapsableBannerAd()
    }
}