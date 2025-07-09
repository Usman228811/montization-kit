package io.monetize.kit.sdk.data.impl

import android.app.Activity
import android.content.Context
import android.widget.LinearLayout
import androidx.lifecycle.ViewModelProvider.NewInstanceFactory.Companion.instance
import io.monetize.kit.sdk.ads.banner.BaseSingleBannerActivity
import io.monetize.kit.sdk.ads.collapsable.BaseCollapsableBannerActivity
import io.monetize.kit.sdk.core.utils.adtype.BannerControllerConfig
import io.monetize.kit.sdk.domain.repo.GetBannerAdRepo

class GetBannerAdRepoImpl private constructor(
    private val baseSingleBannerActivity: BaseSingleBannerActivity,
    private val baseCollapsableBannerActivity: BaseCollapsableBannerActivity
) : GetBannerAdRepo {


    companion object {

        fun getInstance(
        ): GetBannerAdRepoImpl {
            return GetBannerAdRepoImpl(
                baseSingleBannerActivity = BaseSingleBannerActivity.getInstance(),
                baseCollapsableBannerActivity = BaseCollapsableBannerActivity.getInstance(),
            )
        }
    }

    private var isForCollapse = false


    override fun init(
        mContext: Activity,
        adFrame: LinearLayout,
        bannerControllerConfig: BannerControllerConfig,
        onFail: () -> Unit
    ) {
        this.isForCollapse = bannerControllerConfig.collapsableConfig != null
        if (isForCollapse) {
            baseCollapsableBannerActivity.initCollapsableBannerAd(
                mContext = mContext,
                bannerControllerConfig = bannerControllerConfig,
                adFrame = adFrame,
                onFail = onFail,
            )

        } else {
            baseSingleBannerActivity.initSingleBannerData(
                mContext = mContext,
                bannerControllerConfig = bannerControllerConfig,
                adFrame = adFrame,
                onFail = onFail,
            )
        }
    }


    override fun onResume() {
        if (isForCollapse) {
            baseCollapsableBannerActivity.onResume()
        } else {
            baseSingleBannerActivity.onResume()
        }
    }


    override fun onPause() {
        if (isForCollapse) {
            baseCollapsableBannerActivity.onPause()
        } else {
            baseSingleBannerActivity.onPause()
        }
    }


    override fun onDestroy() {
        if (isForCollapse) {
            baseCollapsableBannerActivity.onDestroy()
        } else {
            baseSingleBannerActivity.onDestroy()
        }
    }


}