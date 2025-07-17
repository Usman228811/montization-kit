package io.monetize.kit.sdk.presentation.ui.native_ad

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import io.monetize.kit.sdk.R
import io.monetize.kit.sdk.core.utils.adtype.NativeControllerConfig
import io.monetize.kit.sdk.presentation.viewmodels.NativeAdViewModelDialog
import io.monetize.kit.sdk.presentation.viewmodels.NativeAdViewModelDialogFactory

class AdKitNativeAdViewDialogXml @JvmOverloads constructor(
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
        nativeControllerConfig: NativeControllerConfig,
        onFail: () -> Unit = {}
    ) {
        this.nativeControllerConfig = nativeControllerConfig
        this.loadNewAd = loadNewAd

        if (context is Activity) {
            visibility = View.VISIBLE


            val viewModel = if (context is ViewModelStoreOwner) {
                ViewModelProvider(context, NativeAdViewModelDialogFactory())[NativeAdViewModelDialog::class.java]
            } else {
                null
            }

            viewModel?.initNativeSingleAdData(
                mContext = context,
                adFrame = this,
                nativeControllerConfig = nativeControllerConfig,
                onFail = onFail
            )

            if (context is LifecycleOwner) {
                viewModel?.observeLifecycle(context)
            }
        }
    }
}