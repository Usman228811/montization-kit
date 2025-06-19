package io.monetize.kit.sdk.data.impl

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.google.android.gms.ads.nativead.NativeAd
import io.monetize.kit.sdk.ads.native_ad.AdControllerListener
import io.monetize.kit.sdk.ads.native_ad.NativeAdSingleController
import io.monetize.kit.sdk.ads.native_ad.NativeAdSingleModel
import io.monetize.kit.sdk.ads.native_ad.addNativeAdView
import io.monetize.kit.sdk.ads.native_ad.addShimmerLayout
import io.monetize.kit.sdk.ads.native_ad.singleNativeList
import io.monetize.kit.sdk.core.utils.AdSdkInternetController
import io.monetize.kit.sdk.core.utils.AdSdkPref
import io.monetize.kit.sdk.core.utils.adtype.AdType
import io.monetize.kit.sdk.core.utils.adtype.NativeControllerConfig
import io.monetize.kit.sdk.core.utils.consent.AdSdkConsentManager
import io.monetize.kit.sdk.domain.repo.GetNativeAdRepo


class GetNativeAdRepoImpl(
    private val prefs: AdSdkPref,
    private val internetController: AdSdkInternetController,
    private val consentManager: AdSdkConsentManager,
) : GetNativeAdRepo {

    private var largeNativeAd: Any? = null
    private var loadNewAd: Boolean = false
    private var isAdLoadCalled: Boolean = false
    private var isRequesting: Boolean = false
    private var adFrame: LinearLayout? = null
    private var model: NativeAdSingleModel? = null
    private var adType: AdType = AdType.SMALL_BOTTOM_BUTTON
    private lateinit var mContext: Activity
    private lateinit var nativeControllerConfig: NativeControllerConfig


    override fun init(
        mContext: Activity,
        adFrame: LinearLayout,
        nativeControllerConfig: NativeControllerConfig,
        loadNewAd: Boolean
    ) {
        this.nativeControllerConfig = nativeControllerConfig
        this.mContext = mContext
        this.adType = AdType.entries.filter { it.type == nativeControllerConfig.adType.toInt() }[0]
        this.loadNewAd = loadNewAd
        this.adFrame = adFrame
        isAdLoadCalled = true

        var index = singleNativeList.indexOfFirst { it.key == nativeControllerConfig.key }
        if (index == -1) {
            singleNativeList.apply {
                add(
                    NativeAdSingleModel(
                        nativeControllerConfig.key,
                        NativeAdSingleController(
                            prefs = prefs,
                            internetController = internetController,
                            consentManager = consentManager
                        )
                    )
                )
            }
            index = singleNativeList.indexOfFirst { it.key == nativeControllerConfig.key }
        }
        if (index != -1) {
            model = singleNativeList[index]
            loadSingleNativeAd()
        }
    }

    override fun onResume() {
        loadSingleNativeAd()
    }

    override fun onPause() {
        if (isRequesting) {
            model?.controller?.setNativeControllerListener(null)
        }
    }

    override fun onDestroy() {
        try {
            destroyNativeAd()
        } catch (_: Exception) {
        }
    }


    private fun destroyNativeAd() {
        try {
            if (largeNativeAd != null) {
                destroyAd(largeNativeAd!!)
                largeNativeAd = null
            }
        } catch (_: Exception) {
        }
    }

    private fun destroyAd(largeNativeAd: Any) {
        try {
            if (largeNativeAd is NativeAd) {
                largeNativeAd.destroy()
            }

        } catch (_: Exception) {
        }
    }

    private fun hideAdFrame() {
        try {
            adFrame?.let {
                it.visibility = View.GONE
                it.removeAllViews()
            }
        } catch (_: Exception) {
        }
    }

    private fun loadSingleNativeAd(isForRefresh: Boolean = false) {
        try {
            if (isAdLoadCalled) {
                if (adFrame == null || !nativeControllerConfig.isAdEnable || prefs.isAppPurchased) {
                    hideAdFrame()
                } else {
                    model?.controller?.let { nativeAdController ->
                        adFrame?.let { adFrame ->
                            if (largeNativeAd == null || isForRefresh) {
                                if (!isRequesting) {
                                    isRequesting = true
                                    adFrame.descendantFocusability =
                                        ViewGroup.FOCUS_BLOCK_DESCENDANTS
                                    if (largeNativeAd == null) {
                                        addShimmerLayout(
                                            mContext, adFrame, adType
                                        )
                                    }
                                    nativeAdController.setNativeControllerListener(object :
                                        AdControllerListener {

                                        override fun onAdLoaded() {
                                            isRequesting = false
                                            if (mContext.isFinishing || mContext.isDestroyed || mContext.isChangingConfigurations) {
                                                return
                                            }
                                            if (largeNativeAd == null || isForRefresh) {
                                                loadSingleNativeAd(isForRefresh)
                                            }
                                        }

                                        override fun onAdFailed() {
                                            isRequesting = false
                                            if (mContext.isFinishing || mContext.isDestroyed || mContext.isChangingConfigurations) {
                                                return
                                            }
                                            if (largeNativeAd == null) {
                                                hideAdFrame()
                                            }
                                        }

                                        override fun resetRequesting() {
                                            isRequesting = false
                                        }
                                    })
                                    nativeAdController.populateNativeAd(
                                        context = mContext,
                                        nativeControllerConfig = nativeControllerConfig,
                                        adFrame = adFrame,
                                        loadNewAd = loadNewAd,
                                    ) { ad ->
                                        isRequesting = false
                                        if (!mContext.isFinishing && !mContext.isDestroyed && !mContext.isChangingConfigurations) {
                                            nativeAdController.setNativeControllerListener(null)
                                            largeNativeAd = ad
//                                            event?.setFromScreen(
//                                                adIdNativeReference.replace(
//                                                    "_NATIVE_ID", ""
//                                                )
//                                            )
                                        }
                                    }
                                }
                            } else {
                                addNativeAdView(
                                    adType,
                                    mContext,
                                    adFrame,
                                    largeNativeAd as NativeAd,
                                )
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun requestNewNativeId(stopNextRequest: Boolean) {
        if (nativeControllerConfig.isAdEnable) {
            loadNewAd = !stopNextRequest
            if (!isRequesting) {
                loadSingleNativeAd(true)
            }
        }
    }
}