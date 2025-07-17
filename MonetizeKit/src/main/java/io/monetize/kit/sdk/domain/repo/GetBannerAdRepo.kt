package io.monetize.kit.sdk.domain.repo

import android.app.Activity
import android.widget.LinearLayout
import io.monetize.kit.sdk.core.utils.adtype.BannerControllerConfig

interface GetBannerAdRepo {

    fun init(
        mContext: Activity,
        adFrame: LinearLayout,
        bannerControllerConfig: BannerControllerConfig,
        onFail: () -> Unit
    )



    fun onResume()
    fun onPause()
    fun onDestroy()
}