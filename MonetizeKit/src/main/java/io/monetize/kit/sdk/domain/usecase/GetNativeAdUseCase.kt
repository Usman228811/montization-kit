package io.monetize.kit.sdk.domain.usecase

import android.app.Activity
import android.widget.LinearLayout
import io.monetize.kit.sdk.core.utils.adtype.NativeControllerConfig
import io.monetize.kit.sdk.domain.repo.GetNativeAdRepo

class GetNativeAdUseCase(private val repo: GetNativeAdRepo) {

    operator fun invoke(
        mContext: Activity,
        adFrame: LinearLayout,
        nativeControllerConfig: NativeControllerConfig,
        loadNewAd: Boolean = false
    ) {

        repo.init(
            mContext = mContext,
            nativeControllerConfig = nativeControllerConfig,
            adFrame = adFrame,
            loadNewAd = loadNewAd
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