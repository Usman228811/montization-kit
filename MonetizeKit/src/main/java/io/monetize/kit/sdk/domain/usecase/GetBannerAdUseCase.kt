package io.monetize.kit.sdk.domain.usecase

import android.app.Activity
import android.widget.LinearLayout
import io.monetize.kit.sdk.core.utils.adtype.BannerControllerConfig
import io.monetize.kit.sdk.core.utils.callbacks.AdCallBack
import io.monetize.kit.sdk.data.impl.GetBannerAdRepoImpl
import io.monetize.kit.sdk.domain.repo.GetBannerAdRepo

class GetBannerAdUseCase private constructor(private val repo: GetBannerAdRepo) {

    companion object {

        fun getInstance(
        ): GetBannerAdUseCase {

            val repo = GetBannerAdRepoImpl.getInstance()


            return GetBannerAdUseCase(
                repo
            )
        }
    }

    operator fun invoke(
        mContext: Activity,
        adFrame: LinearLayout,
        bannerControllerConfig: BannerControllerConfig,
        adCallBack: AdCallBack


    ) {

        repo.init(
            mContext = mContext,
            bannerControllerConfig = bannerControllerConfig,
            adFrame = adFrame,
            adCallBack = adCallBack,
        )

    }

    fun onResume() {
        repo.onResume()
    }

    fun onPause() {
        repo.onPause()
    }

    fun onDestroy() {
        repo.onDestroy()
    }
}