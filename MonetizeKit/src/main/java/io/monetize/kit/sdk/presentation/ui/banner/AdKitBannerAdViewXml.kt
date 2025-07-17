package io.monetize.kit.sdk.presentation.ui.banner

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import io.monetize.kit.sdk.R
import io.monetize.kit.sdk.core.utils.adtype.BannerControllerConfig
import io.monetize.kit.sdk.presentation.viewmodels.BannerAdViewModel
import io.monetize.kit.sdk.presentation.viewmodels.BannerAdViewModelFactory

class AdKitBannerAdViewXml @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle) {

    private var bannerControllerConfig: BannerControllerConfig? = null

    init {
        inflate(context, R.layout.ad_inflator, this)
    }

    fun loadBanner(
        context: Context,
        bannerControllerConfig: BannerControllerConfig,
        onFail: () -> Unit = {}
    ) {
        this.bannerControllerConfig = bannerControllerConfig

        val viewModel = if (context is ViewModelStoreOwner) {
            ViewModelProvider(context, BannerAdViewModelFactory())[BannerAdViewModel::class.java]
        } else {
            null
        }



        if (context is Activity) {
            visibility = View.VISIBLE
            viewModel?.initSingleBannerData(
                mContext = context,
                bannerControllerConfig = bannerControllerConfig,
                adFrame = this,
                onFail = onFail
            )

            // Optional lifecycle observe
            if (context is LifecycleOwner) {
                viewModel?.observeLifecycle(context)
            }
        }
    }
}