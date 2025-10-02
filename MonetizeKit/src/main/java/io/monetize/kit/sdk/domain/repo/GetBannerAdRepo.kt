package io.monetize.kit.sdk.domain.repo

import android.app.Activity
import android.widget.LinearLayout
import io.monetize.kit.sdk.core.utils.adtype.BannerControllerConfig
import io.monetize.kit.sdk.core.utils.callbacks.AdCallBack

interface GetBannerAdRepo {

    fun init(
        mContext: Activity,
        adFrame: LinearLayout,
        bannerControllerConfig: BannerControllerConfig,
        adCallBack: AdCallBack

    )



    fun onResume()
    fun onPause()
    fun onDestroy()
}