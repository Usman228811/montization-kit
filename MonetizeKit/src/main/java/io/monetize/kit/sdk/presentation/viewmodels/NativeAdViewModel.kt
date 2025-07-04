package io.monetize.kit.sdk.presentation.viewmodels

import android.app.Activity
import android.content.Context
import android.widget.LinearLayout
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.monetize.kit.sdk.core.utils.adtype.NativeControllerConfig
import io.monetize.kit.sdk.domain.usecase.GetNativeAdUseCase

class NativeAdViewModelFactory(
    val context: Context
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return NativeAdViewModel(
            GetNativeAdUseCase.getInstance(context = context)
        ) as T
    }
}


class NativeAdViewModel(private var getNativeAdUseCase: GetNativeAdUseCase) : ViewModel() {

    fun initNativeSingleAdData(
        mContext: Activity,
        adFrame: LinearLayout,
        nativeControllerConfig: NativeControllerConfig,
        loadNewAd: Boolean = false
    ) {
        getNativeAdUseCase.invoke(
            mContext = mContext,
            nativeControllerConfig = nativeControllerConfig,
            adFrame = adFrame,
            loadNewAd = loadNewAd,
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

        // Ensure observer is removed when lifecycle is destroyed
        lifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                onDestroy()
                lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
            }
        })
    }
}