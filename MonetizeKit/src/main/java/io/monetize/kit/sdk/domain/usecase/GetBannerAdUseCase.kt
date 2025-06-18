package io.monetize.kit.sdk.domain.usecase

import android.app.Activity
import android.widget.LinearLayout
import io.monetize.kit.sdk.core.utils.adtype.BannerControllerConfig
import io.monetize.kit.sdk.domain.repo.GetBannerAdRepo

class GetBannerAdUseCase(private val repo: GetBannerAdRepo) {

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

    fun onResume(){
        repo.onResume()
    }
    fun onPause(){
        repo.onPause()
    }
    fun onDestroy(){
        repo.onDestroy()
    }
}