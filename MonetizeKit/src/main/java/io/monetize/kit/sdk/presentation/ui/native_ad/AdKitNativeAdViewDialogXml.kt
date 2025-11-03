package io.monetize.kit.sdk.presentation.ui.native_ad

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import io.monetize.kit.sdk.R
import io.monetize.kit.sdk.core.utils.adtype.NativeControllerConfig
import io.monetize.kit.sdk.core.utils.callbacks.AdCallBack
import io.monetize.kit.sdk.presentation.viewmodels.NativeAdViewModelDialog
import io.monetize.kit.sdk.presentation.viewmodels.NativeAdViewModelDialogFactory

class AdKitNativeAdViewDialogXml @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle) {

    private var nativeControllerConfig: NativeControllerConfig? = null

    private var mViewModel: NativeAdViewModelDialog? = null


    init {
        inflate(context, R.layout.ad_inflator, this)
    }

    fun loadNative(
        context: Context,
        owner: ViewModelStoreOwner,
        nativeControllerConfig: NativeControllerConfig,
        adCallBack: AdCallBack? = null,
    ) {
        this.nativeControllerConfig = nativeControllerConfig

        if (context is Activity) {
            visibility = VISIBLE


            mViewModel = ViewModelProvider(
                owner,
                NativeAdViewModelDialogFactory()
            )[NativeAdViewModelDialog::class.java]

            mViewModel?.initNativeSingleAdData(
                mContext = context,
                adFrame = this,
                nativeControllerConfig = nativeControllerConfig,
                adCallBack = adCallBack,

                )

            if (context is LifecycleOwner) {
                mViewModel?.observeLifecycle(context)
            }
        }
    }

    fun destroyNativeAd() {
        mViewModel?.onDestroy()
    }
}