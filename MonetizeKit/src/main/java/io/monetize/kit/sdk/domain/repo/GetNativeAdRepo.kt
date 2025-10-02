package io.monetize.kit.sdk.domain.repo

import android.app.Activity
import android.widget.LinearLayout
import io.monetize.kit.sdk.core.utils.adtype.NativeControllerConfig
import io.monetize.kit.sdk.core.utils.callbacks.AdCallBack

interface GetNativeAdRepo {

    fun init(
        mContext: Activity,
        adFrame: LinearLayout,
        nativeControllerConfig: NativeControllerConfig,
        adCallBack: AdCallBack,
        )


    fun onResume()
    fun onPause()
    fun onDestroy()
}