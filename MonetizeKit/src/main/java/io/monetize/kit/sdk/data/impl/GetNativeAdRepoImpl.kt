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
import io.monetize.kit.sdk.core.utils.adtype.NativeAdType
import io.monetize.kit.sdk.core.utils.adtype.NativeControllerConfig
import io.monetize.kit.sdk.core.utils.callbacks.AdCallBack
import io.monetize.kit.sdk.core.utils.firebaseBoolean
import io.monetize.kit.sdk.core.utils.firebaseString
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
    private var nativeAdType: NativeAdType = NativeAdType.SMALL_NATIVE
    private lateinit var mContext: Activity
    private lateinit var nativeControllerConfig: NativeControllerConfig
    private var canLoadAdAgain = true
    private var isAdEnable: Boolean = true
    private var adCallBack: AdCallBack? = null

    // Keep listener identities so this repository removes only its own callbacks
    // from the globally shared native controller.
    private var controllerListener: AdControllerListener? = null
    private var refreshListener: NativeRefreshListener? = null


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

        NativeAdType.entries.filter {
            it.name == firebaseString(
                "${nativeControllerConfig.placementKey}_nativeAdType",
                NativeAdType.LARGE_NATIVE.name
            )
        }.apply {
            nativeAdType = if (this.isNotEmpty()) {
                this[0]
            } else {
                NativeAdType.SMALL_NATIVE
            }
        }


        loadNewAd = firebaseBoolean("${nativeControllerConfig.adIdKey}_loadNewAd", false)
        isAdEnable = firebaseBoolean("${nativeControllerConfig.placementKey}_isAdEnable", false)
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
            adFrame?.let {
                loadSingleNativeAd()
            }
        } else {
            if (adKitPref.isAppPurchased.not()) {
                attachRefreshListener(true)

                adFrame?.let { adFrame ->
                    addNativeAdView(
                        nativeControllerConfig = nativeControllerConfig,
                        adsCustomLayoutHelper = nativeCustomLayoutHelper,
                        nativeAdType = nativeAdType,
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
            // Replace only this repository's previous refresh listener.
            clearRefreshListener()

            val listener = object : NativeRefreshListener {
                override fun refreshNativeAd() {
                    isRequesting = false
                    if (!mContext.isFinishing && !mContext.isDestroyed && !mContext.isChangingConfigurations) {
                        requestNative(true)
                    }
                }
            }

            refreshListener = listener
            nativeAdController.setNativeRefreshListener(listener)
            if (fromResume) {
                nativeAdController.startRefreshTime()
            }
        }

    }

    override fun onPause() {
        canLoadAdAgain = true
        if (isRequesting) {
            // The ad request may continue in the shared controller. Detach only this
            // screen's callback so a newly created screen can safely take ownership.
            clearControllerListener()
            isRequesting = false
        }
        clearRefreshListener()
    }

    override fun onDestroy() {
        try {
            // Ownership-aware cleanup avoids clearing callbacks registered by a
            // replacement Activity or navigation destination.
            clearControllerListener()
            clearRefreshListener()
            isRequesting = false
            canLoadAdAgain = true
            destroyNativeAd()
//            hideAdFrame()
            adFrame = null
            adCallBack = null
            isAdLoadCalled = false
            model = null
        } catch (_: Exception) {
        }
    }

    private fun clearControllerListener() {
        model?.controller?.clearNativeControllerListener(controllerListener)
        controllerListener = null
    }

    private fun clearRefreshListener() {
        model?.controller?.clearNativeRefreshListener(refreshListener)
        refreshListener = null
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
                    /*|| !AdKit.consentManager.canRequestAds*/
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
                                    nativeAdType = nativeAdType,
                                    customLayoutHelper = nativeCustomLayoutHelper
                                )
                            }
                            val listener = object : AdControllerListener {

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
                                            nativeAdType = nativeAdType,
                                            onPopulated = { ad ->
                                                isRequesting = false
                                                if (!mContext.isFinishing && !mContext.isDestroyed && !mContext.isChangingConfigurations) {
                                                    clearControllerListener()
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
                                    clearControllerListener()
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
                            }

                            // Store the exact listener instance before registering it
                            // because the controller is shared across screen instances.
                            controllerListener = listener
                            nativeAdController.setNativeControllerListener(listener)

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
                            nativeAdType = nativeAdType,
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
