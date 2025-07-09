package io.monetize.kit.sdk.domain.usecase

import android.app.Activity
import android.content.Context
import android.widget.LinearLayout
import io.monetize.kit.sdk.core.utils.adtype.NativeControllerConfig
import io.monetize.kit.sdk.data.impl.GetNativeAdRepoImpl
import io.monetize.kit.sdk.domain.repo.GetNativeAdRepo

class GetNativeAdUseCase private constructor(private val repo: GetNativeAdRepo) {

    companion object {

        fun getInstance(
        ): GetNativeAdUseCase {
            val repo = GetNativeAdRepoImpl.getInstance()
            return GetNativeAdUseCase(
                repo
            )
        }
    }

    operator fun invoke(
        mContext: Activity,
        adFrame: LinearLayout,
        nativeControllerConfig: NativeControllerConfig,
        onFail: () -> Unit
    ) {

        repo.init(
            mContext = mContext,
            nativeControllerConfig = nativeControllerConfig,
            adFrame = adFrame,
            onFail = onFail,
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