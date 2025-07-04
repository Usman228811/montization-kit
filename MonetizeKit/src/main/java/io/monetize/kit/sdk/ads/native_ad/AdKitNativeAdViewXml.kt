package io.monetize.kit.sdk.ads.native_ad

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.lifecycle.LifecycleOwner
import io.monetize.kit.sdk.R
import io.monetize.kit.sdk.core.utils.adtype.NativeControllerConfig
import io.monetize.kit.sdk.presentation.viewmodels.NativeAdViewModel

class AdKitNativeAdViewXml @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle) {

    private var nativeControllerConfig: NativeControllerConfig? = null
    private var loadNewAd: Boolean = false

    init {
        inflate(context, R.layout.ad_inflator, this)
    }

    fun loadNative(
        context: Context,
        viewModel: NativeAdViewModel?,
        nativeControllerConfig: NativeControllerConfig,
        loadNewAd: Boolean = false
    ) {
        this.nativeControllerConfig = nativeControllerConfig
        this.loadNewAd = loadNewAd

        if (context is Activity) {
            visibility = View.VISIBLE
            viewModel?.initNativeSingleAdData(
                mContext = context,
                adFrame = this,
                nativeControllerConfig = nativeControllerConfig,
                loadNewAd = loadNewAd
            )

            if (context is LifecycleOwner) {
                viewModel?.observeLifecycle(context)
            }
        }
    }
}