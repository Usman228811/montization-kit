package io.monetize.kit.sdk.data.impl

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.google.android.gms.ads.nativead.NativeAd
import io.monetize.kit.sdk.ads.native_ad.AdControllerListener
import io.monetize.kit.sdk.ads.native_ad.NativeAdSingleController
import io.monetize.kit.sdk.ads.native_ad.NativeAdSingleModel
import io.monetize.kit.sdk.ads.native_ad.NativeRefreshListener
import io.monetize.kit.sdk.ads.native_ad.addNativeAdView
import io.monetize.kit.sdk.ads.native_ad.addNativeShimmerLayout
import io.monetize.kit.sdk.ads.native_ad.singleNativeList
import io.monetize.kit.sdk.core.utils.adtype.AdType
import io.monetize.kit.sdk.core.utils.adtype.NativeControllerConfig
import io.monetize.kit.sdk.core.utils.callbacks.AdCallBack
import io.monetize.kit.sdk.core.utils.firebaseBoolean
import io.monetize.kit.sdk.core.utils.firebaseLong
import io.monetize.kit.sdk.core.utils.init.AdKit
import io.monetize.kit.sdk.core.utils.init.AdKit.adKitPref
import io.monetize.kit.sdk.core.utils.init.AdKit.nativeCustomLayoutHelper
import io.monetize.kit.sdk.domain.repo.GetNativeAdRepo


class GetNativeAdRepoImpl private constructor(
) : GetNativeAdRepo {

    private var largeNativeAd: Any? = null
    private var loadNewAd: Boolean = false
    private var isAdLoadCalled: Boolean = false
    private var isRequesting: Boolean = false
    private var adFrame: LinearLayout? = null
    private var model: NativeAdSingleModel? = null
    private var adType: AdType = AdType.SMALL_NATIVE
    private lateinit var mContext: Activity
    private lateinit var nativeControllerConfig: NativeControllerConfig
    private var canLoadAdAgain = true
    private var isAdEnable: Boolean = true
    private var adCallBack: AdCallBack? = null


    companion object {

        fun getInstance(
        ): GetNativeAdRepoImpl {
            return GetNativeAdRepoImpl()
        }
    }


    override fun init(
        mContext: Activity,
        adFrame: LinearLayout,
        nativeControllerConfig: NativeControllerConfig,
        adCallBack: AdCallBack?,
    ) {
        this.adCallBack = adCallBack
        this.nativeControllerConfig = nativeControllerConfig
        this.mContext = mContext
        this.adFrame = adFrame
        if (AdKit.initializer.getDisableAds()) {
            adCallBack?.onAdFailed("ads are disabled in app class")
            hideAdFrame()
            return
        }

        adType = AdType.entries.filter {
            it.type == firebaseLong(
                "${nativeControllerConfig.placementKey}_adType",
                0
            ).toInt()
        }[0]
        loadNewAd = firebaseBoolean("${nativeControllerConfig.adIdKey}_loadNewAd", false)
        isAdEnable = firebaseBoolean("${nativeControllerConfig.placementKey}_isAdEnable", true)
        isAdLoadCalled = true

        var index = singleNativeList.indexOfFirst { it.key == nativeControllerConfig.adIdKey }
        if (index == -1) {
            singleNativeList.apply {
                add(
                    NativeAdSingleModel(
                        nativeControllerConfig.adIdKey,
                        NativeAdSingleController()
                    )
                )
            }
            index = singleNativeList.indexOfFirst { it.key == nativeControllerConfig.adIdKey }
        }

        if (nativeControllerConfig.consumeAnyAd) {
            model = AdKit.preLoadNative.getControllerWithAd()

        }

        if (index != -1) {
            if (model == null) {
                model = singleNativeList[index]
            }
            loadSingleNativeAd()
        }

    }

    override fun onResume() {
        if (largeNativeAd == null) {
            loadSingleNativeAd()
        } else {
            if (adKitPref.isAppPurchased.not()) {
                attachRefreshListener(true)

                adFrame?.let { adFrame ->
                    addNativeAdView(
                        nativeControllerConfig = nativeControllerConfig,
                        adsCustomLayoutHelper = nativeCustomLayoutHelper,
                        adType = adType,
                        context = mContext,
                        adFrame = adFrame,
                        ad = largeNativeAd as NativeAd,
                    )
                    adCallBack?.onAdShow()
                }
            } else {
                hideAdFrame()
            }
        }
    }

    fun attachRefreshListener(fromResume: Boolean) {
        model?.controller?.let { nativeAdController ->
            nativeAdController.setNativeRefreshListener(object :
                NativeRefreshListener {
                override fun refreshNativeAd() {
                    isRequesting = false
                    if (!mContext.isFinishing && !mContext.isDestroyed && !mContext.isChangingConfigurations) {
                        requestNative(true)
                    }
                }

            })
            if (fromResume) {
                nativeAdController.startRefreshTime()
            }
        }

    }

    override fun onPause() {
        canLoadAdAgain = true
        if (isRequesting) {
            model?.controller?.setNativeControllerListener(null)
        }
        nullRefreshListener()
    }

    override fun onDestroy() {
        try {
            destroyNativeAd()
            nullRefreshListener()
        } catch (_: Exception) {
        }
    }

    private fun nullRefreshListener() {
        model?.controller?.setNativeRefreshListener(null)
    }


    private fun destroyNativeAd() {
        try {
            if (largeNativeAd != null) {
                canLoadAdAgain = true
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
                if (!isAdEnable
                ) {
                    adCallBack?.onAdFailed("${nativeControllerConfig.placementKey} ad is disable or not added in remote config")
                    hideAdFrame()
                } else if (
                    adFrame == null
                    || adKitPref.isAppPurchased
                    || !AdKit.internetController.isConnected
                    || !AdKit.consentManager.canRequestAds
                ) {
                    adCallBack?.onAdFailed("${nativeControllerConfig.placementKey} can't request ad because of internet connection | consent manager | app purchased")
                    hideAdFrame()
                } else {
                    requestNative(false)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun requestNative(forRefresh: Boolean) {
        model?.controller?.let { nativeAdController ->
            adFrame?.let { adFrame ->
                if (canLoadAdAgain) {
                    if (largeNativeAd == null || forRefresh) {

                        if (!isRequesting) {
                            isRequesting = true
                            adFrame.descendantFocusability =
                                ViewGroup.FOCUS_BLOCK_DESCENDANTS
                            if (largeNativeAd == null) {
                                addNativeShimmerLayout(
                                    context = mContext,
                                    adFrame = adFrame,
                                    adType = adType,
                                    customLayoutHelper = nativeCustomLayoutHelper
                                )
                            }
                            nativeAdController.setNativeControllerListener(object :
                                AdControllerListener {

                                override fun onAdLoaded() {
                                    isRequesting = false
                                    if (mContext.isFinishing || mContext.isDestroyed || mContext.isChangingConfigurations) {
                                        return
                                    }
                                    if (largeNativeAd == null || forRefresh) {

                                        nativeAdController.populateNativeAd(
                                            context = mContext,
                                            adFrame = adFrame,
                                            loadNewAd = loadNewAd && nativeControllerConfig.loadNextAd,
                                            onPopulated = { ad ->
                                                isRequesting = false
                                                if (!mContext.isFinishing && !mContext.isDestroyed && !mContext.isChangingConfigurations) {
                                                    nativeAdController.setNativeControllerListener(
                                                        null
                                                    )
                                                    adCallBack?.onAdShow()
                                                    largeNativeAd = ad
                                                    nativeAdController.startRefreshTime()
                                                }
                                            }, onAdClick = {
                                                adCallBack?.onAdClick()
                                            })
                                    }
                                }

                                override fun onAdFailed(reason: String) {
                                    adCallBack?.onAdFailed(reason)

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

                            attachRefreshListener(false)

                            nativeAdController.requestNativeAd(
                                context = mContext,
                                nativeControllerConfig = nativeControllerConfig
                            )
                        }
                    } else {
                        addNativeAdView(
                            nativeControllerConfig = nativeControllerConfig,
                            adsCustomLayoutHelper = nativeCustomLayoutHelper,
                            adType = adType,
                            context = mContext,
                            adFrame = adFrame,
                            ad = largeNativeAd as NativeAd,
                        )
                        adCallBack?.onAdShow()
                    }
                }
            }
        }
    }


}