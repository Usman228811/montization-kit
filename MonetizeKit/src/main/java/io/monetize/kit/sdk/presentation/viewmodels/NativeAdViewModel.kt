package io.monetize.kit.sdk.presentation.viewmodels

import android.app.Activity
import android.widget.LinearLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.monetize.kit.sdk.core.utils.adtype.NativeControllerConfig
import io.monetize.kit.sdk.core.utils.callbacks.AdCallBack
import io.monetize.kit.sdk.domain.usecase.GetNativeAdUseCase

class NativeAdViewModelFactory(
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return NativeAdViewModel(
            GetNativeAdUseCase.getInstance()
        ) as T
    }
}


class NativeAdViewModel(private var getNativeAdUseCase: GetNativeAdUseCase) : ViewModel() {

    fun initNativeSingleAdData(
        mContext: Activity,
        adFrame: LinearLayout,
        nativeControllerConfig: NativeControllerConfig,
        adCallBack: AdCallBack?,
    ) {
        getNativeAdUseCase.invoke(
            mContext = mContext,
            nativeControllerConfig = nativeControllerConfig,
            adFrame = adFrame,
            adCallBack= adCallBack,

            )
    }


    fun onResume() {
        getNativeAdUseCase.onResume()
    }

    fun onPause() {
        getNativeAdUseCase.onPause()
    }

    fun onDestroy() {
        getNativeAdUseCase.onDestroy()
    }

    fun observeLifecycle(lifecycleOwner: LifecycleOwner) {
        val lifecycleObserver = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    onResume()
                }

                Lifecycle.Event.ON_PAUSE -> {
                    onPause()
                }

                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)

    }

    override fun onCleared() {
        // Activity ON_DESTROY also occurs during language/configuration changes.
        // ViewModel cleanup runs only when this ad owner is permanently removed.
        onDestroy()
        super.onCleared()
    }
}
