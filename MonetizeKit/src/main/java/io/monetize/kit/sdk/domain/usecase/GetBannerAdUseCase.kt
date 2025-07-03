package io.monetize.kit.sdk.domain.usecase

import android.app.Activity
import android.content.Context
import android.widget.LinearLayout
import io.monetize.kit.sdk.core.utils.adtype.BannerControllerConfig
import io.monetize.kit.sdk.data.impl.GetBannerAdRepoImpl
import io.monetize.kit.sdk.domain.repo.GetBannerAdRepo

class GetBannerAdUseCase private constructor(private val repo: GetBannerAdRepo) {

    companion object {

        fun getInstance(
            context: Context
        ): GetBannerAdUseCase {

            val repo = GetBannerAdRepoImpl.getInstance(context)


            return GetBannerAdUseCase(
                repo
            )
        }
    }

    operator fun invoke(
        mContext: Activity,
        adFrame: LinearLayout,
        bannerControllerConfig: BannerControllerConfig

    ) {

        repo.init(
            mContext = mContext,
            bannerControllerConfig = bannerControllerConfig,
            adFrame = adFrame,
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