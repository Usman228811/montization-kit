package io.monetize.kit.sdk.presentation.viewmodels

import android.app.Activity
import android.widget.LinearLayout
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import io.monetize.kit.sdk.core.utils.adtype.BannerControllerConfig
import io.monetize.kit.sdk.domain.usecase.GetBannerAdUseCase

class BannerAdViewModel(private val getBannerAdUseCase: GetBannerAdUseCase) : ViewModel() {


    fun initSingleBannerData(
        mContext: Activity,
        adFrame: LinearLayout,
        bannerControllerConfig: BannerControllerConfig
    ) {
        getBannerAdUseCase(
            mContext,
            adFrame,
            bannerControllerConfig
        )
    }


    fun onResume() {
        getBannerAdUseCase.onResume()
    }

    fun onPause() {

        getBannerAdUseCase.onPause()
    }

    fun onDestroy() {

        getBannerAdUseCase.onDestroy()
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