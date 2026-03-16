package io.monetize.kit.sdk.data.impl

import android.app.Activity
import android.widget.LinearLayout
import io.monetize.kit.sdk.ads.banner.BannerAdController
import io.monetize.kit.sdk.ads.collapsable.CollapsableBannerAdController
import io.monetize.kit.sdk.core.utils.adtype.BannerAdType
import io.monetize.kit.sdk.core.utils.adtype.BannerControllerConfig
import io.monetize.kit.sdk.core.utils.callbacks.AdCallBack
import io.monetize.kit.sdk.core.utils.firebaseString
import io.monetize.kit.sdk.domain.repo.GetBannerAdRepo

class GetBannerAdRepoImpl private constructor(
    private val bannerAdController: BannerAdController,
    private val collapsableBannerAdController: CollapsableBannerAdController
) : GetBannerAdRepo {


    companion object {

        fun getInstance(
        ): GetBannerAdRepoImpl {
            return GetBannerAdRepoImpl(
                bannerAdController = BannerAdController.getInstance(),
                collapsableBannerAdController = CollapsableBannerAdController.getInstance(),
            )
        }
    }

    private var isForCollapse = false


    override fun init(
        mContext: Activity,
        adFrame: LinearLayout,
        bannerControllerConfig: BannerControllerConfig,
        adCallBack: AdCallBack?

    ) {
        var bannerType = firebaseString(
            "${bannerControllerConfig.placementKey}_bannerAdType",
            BannerAdType.ADAPTIVE_BANNER.name
        )
        if (bannerType.isEmpty()) {
            bannerType = BannerAdType.ADAPTIVE_BANNER.name
        }
        isForCollapse = bannerType == BannerAdType.BOTTOM_COLLAPSIBLE_BANNER.name || bannerType == BannerAdType.TOP_COLLAPSIBLE_BANNER.name
        if (isForCollapse) {
            collapsableBannerAdController.initCollapsableBannerAd(
                mContext = mContext,
                bannerControllerConfig = bannerControllerConfig,
                adFrame = adFrame,
                bannerType = bannerType,
                adCallBack = adCallBack,
            )

        } else {
            bannerAdController.initSingleBannerData(
                mContext = mContext,
                bannerControllerConfig = bannerControllerConfig,
                adFrame = adFrame,
                bannerType = bannerType,
                adCallBack = adCallBack
            )
        }
    }


    override fun onResume() {
        if (isForCollapse) {
            collapsableBannerAdController.onResume()
        } else {
            bannerAdController.onResume()
        }
    }


    override fun onPause() {
        if (isForCollapse) {
            collapsableBannerAdController.onPause()
        } else {
            bannerAdController.onPause()
        }
    }


    override fun onDestroy() {
        if (isForCollapse) {
            collapsableBannerAdController.onDestroy()
        } else {
            bannerAdController.onDestroy()
        }
    }


}