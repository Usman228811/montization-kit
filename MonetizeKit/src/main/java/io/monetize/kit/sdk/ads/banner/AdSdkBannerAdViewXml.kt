package io.monetize.kit.sdk.ads.banner

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.lifecycle.LifecycleOwner
import io.monetize.kit.sdk.R
import io.monetize.kit.sdk.core.utils.adtype.BannerControllerConfig
import io.monetize.kit.sdk.presentation.viewmodels.BannerAdViewModel

class AdSdkBannerAdViewXml @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle) {

    private lateinit var bannerAdViewModel: BannerAdViewModel
    private var bannerControllerConfig: BannerControllerConfig? = null

    init {
        inflate(context, R.layout.ad_inflator, this)
    }

    fun loadBanner(
        context: Context,
        viewModel: BannerAdViewModel,
        bannerControllerConfig: BannerControllerConfig
    ) {
        bannerAdViewModel = viewModel
        this.bannerControllerConfig = bannerControllerConfig

        if (context is Activity) {
            visibility = View.VISIBLE
            bannerAdViewModel.initSingleBannerData(
                mContext = context,
                bannerControllerConfig = bannerControllerConfig,
                adFrame = this
            )

            // Optional lifecycle observe
            if (context is LifecycleOwner) {
                bannerAdViewModel.observeLifecycle(context)
            }
        }
    }
}