package io.monetize.kit.sdk.data.impl

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.google.android.gms.ads.nativead.NativeAd
import io.monetize.kit.sdk.ads.native_ad.AdControllerListener
import io.monetize.kit.sdk.ads.native_ad.AdKitNativeCommonHelper
import io.monetize.kit.sdk.ads.native_ad.AdsCustomLayoutHelper
import io.monetize.kit.sdk.ads.native_ad.NativeAdSingleController
import io.monetize.kit.sdk.ads.native_ad.NativeAdSingleModel
import io.monetize.kit.sdk.ads.native_ad.addNativeAdView
import io.monetize.kit.sdk.ads.native_ad.addShimmerLayout
import io.monetize.kit.sdk.ads.native_ad.singleNativeList
import io.monetize.kit.sdk.core.utils.AdKitInternetController
import io.monetize.kit.sdk.core.utils.AdKitPref
import io.monetize.kit.sdk.core.utils.adtype.AdType
import io.monetize.kit.sdk.core.utils.adtype.NativeControllerConfig
import io.monetize.kit.sdk.core.utils.consent.AdKitConsentManager
import io.monetize.kit.sdk.domain.repo.GetNativeAdRepo


class GetNativeAdRepoImpl private constructor(
    private val prefs: AdKitPref,
    private val internetController: AdKitInternetController,
    private val consentManager: AdKitConsentManager,
    private val customLayoutHelper: AdsCustomLayoutHelper,
    private val nativeCommonHelper: AdKitNativeCommonHelper,
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
    private var canLoadAdAgain = true


    companion object {

        fun getInstance(
            context: Context
        ): GetNativeAdRepoImpl {
            return GetNativeAdRepoImpl(
                AdKitPref.getInstance(context),
                AdKitInternetController.getInstance(context),
                AdKitConsentManager.getInstance(context),
                AdsCustomLayoutHelper.getInstance(),
                AdKitNativeCommonHelper.getInstance(),
            )
        }
    }


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
                            consentManager = consentManager,
                            customLayoutHelper = customLayoutHelper,
                            nativeCommonHelper = nativeCommonHelper
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
        canLoadAdAgain = true
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

    private fun loadSingleNativeAd() {
        try {
            if (isAdLoadCalled) {
                if (adFrame == null || !nativeControllerConfig.isAdEnable || prefs.isAppPurchased) {
                    hideAdFrame()
                } else {
                    model?.controller?.let { nativeAdController ->
                        adFrame?.let { adFrame ->
                            if (canLoadAdAgain) {
                                if (largeNativeAd == null) {

                                    if (!isRequesting) {
                                        isRequesting = true
                                        adFrame.descendantFocusability =
                                            ViewGroup.FOCUS_BLOCK_DESCENDANTS
                                        if (largeNativeAd == null) {
                                            addShimmerLayout(
                                                context = mContext,
                                                adFrame = adFrame,
                                                adType = adType,
                                                customLayoutHelper = customLayoutHelper
                                            )
                                        }
                                        nativeAdController.setNativeControllerListener(object :
                                            AdControllerListener {

                                            override fun onAdLoaded() {
                                                isRequesting = false
                                                if (mContext.isFinishing || mContext.isDestroyed || mContext.isChangingConfigurations) {
                                                    return
                                                }
                                                if (largeNativeAd == null) {
                                                    loadSingleNativeAd()
                                                }
                                            }

                                            override fun onAdFailed() {
                                                isRequesting = false
                                                canLoadAdAgain = false
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
                                        customLayoutHelper,
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
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun requestNewNativeId(stopNextRequest: Boolean) {
        if (nativeControllerConfig.isAdEnable) {
            loadNewAd = !stopNextRequest
            if (!isRequesting) {
                loadSingleNativeAd()
            }
        }
    }
}