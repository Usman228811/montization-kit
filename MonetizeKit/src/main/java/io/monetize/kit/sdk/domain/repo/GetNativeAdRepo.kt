package io.monetize.kit.sdk.domain.repo

import android.app.Activity
import android.widget.LinearLayout
import io.monetize.kit.sdk.core.utils.adtype.NativeControllerConfig

interface GetNativeAdRepo {

    fun init(
        mContext: Activity,
        adFrame: LinearLayout,
        nativeControllerConfig: NativeControllerConfig,
        loadNewAd: Boolean = false
    )


    fun onResume()
    fun onPause()
    fun onDestroy()
}