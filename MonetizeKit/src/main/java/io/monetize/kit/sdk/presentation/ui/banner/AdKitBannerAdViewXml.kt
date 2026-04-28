package io.monetize.kit.sdk.presentation.ui.banner

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import io.monetize.kit.sdk.R
import io.monetize.kit.sdk.core.utils.adtype.BannerControllerConfig
import io.monetize.kit.sdk.core.utils.callbacks.AdCallBack
import io.monetize.kit.sdk.presentation.viewmodels.BannerAdViewModel
import io.monetize.kit.sdk.presentation.viewmodels.BannerAdViewModelFactory

class AdKitBannerAdViewXml @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle) {

    private var bannerControllerConfig: BannerControllerConfig? = null

    private var viewModel: BannerAdViewModel? = null

    init {
        inflate(context, R.layout.ad_inflator, this)
    }

    fun destroyBannerAd() {
        viewModel?.onDestroy()
    }

    fun loadBanner(
        context: Context,
        owner: ViewModelStoreOwner,
        bannerControllerConfig: BannerControllerConfig,
        adCallBack: AdCallBack? = null

    ) {
        this.bannerControllerConfig = bannerControllerConfig

        viewModel = ViewModelProvider(
            owner,
            BannerAdViewModelFactory()
        )[bannerControllerConfig.placementKey,
            BannerAdViewModel::class.java]



        if (context is Activity) {
            visibility = VISIBLE
            viewModel?.initSingleBannerData(
                mContext = context,
                bannerControllerConfig = bannerControllerConfig,
                adFrame = this,
                adCallBack = adCallBack,
            )

            // Optional lifecycle observe
            if (context is LifecycleOwner) {
                viewModel?.observeLifecycle(context)
            }
        }
    }
}