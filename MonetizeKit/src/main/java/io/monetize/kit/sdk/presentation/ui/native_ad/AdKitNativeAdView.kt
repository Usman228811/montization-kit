package io.monetize.kit.sdk.presentation.ui.native_ad

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import io.monetize.kit.sdk.R
import io.monetize.kit.sdk.core.utils.adtype.NativeControllerConfig
import io.monetize.kit.sdk.core.utils.callbacks.AdCallBack
import io.monetize.kit.sdk.presentation.ui.rememberAdCallBack
import io.monetize.kit.sdk.presentation.viewmodels.NativeAdViewModel
import io.monetize.kit.sdk.presentation.viewmodels.NativeAdViewModelFactory
import kotlin.random.Random


@Composable
fun AdKitNativeAdView(
    nativeControllerConfig: NativeControllerConfig,
    adCallBack: AdCallBack ?= null,
    callCustomDestroy: ((() -> Unit) -> Unit)? = null

) {

    val nativeAdViewModel: NativeAdViewModel = viewModel(
        key = "${nativeControllerConfig.placementKey}}",
        factory = NativeAdViewModelFactory()
    )

    val tet = LocalActivity.current as Activity
    val lifecycleOwner = LocalLifecycleOwner.current
    val stableAdCallBack = rememberAdCallBack(adCallBack)

    DisposableEffect( nativeAdViewModel, lifecycleOwner) {
        val observer = nativeAdViewModel.observeLifecycle(lifecycleOwner)
        onDispose {
            // Remove the observer, otherwise every re-entry adds another one and
            // onResume (re-inflate + onAdShow) runs once per stale observer.
            // The ad itself is released in ViewModel.onCleared / callCustomDestroy.
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }


    LaunchedEffect(nativeAdViewModel) {
        callCustomDestroy?.invoke {
            nativeAdViewModel.onDestroy()
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {

        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { ctx ->
                val inflater = LayoutInflater.from(ctx)
                val nativeAdLayout = inflater.inflate(R.layout.ad_inflator, null) as LinearLayout
                nativeAdLayout.visibility = View.VISIBLE

                // Set up the ad once per view; update{} would run again on every parent recomposition
                nativeAdViewModel.initNativeSingleAdData(
                    mContext = tet,
                    adFrame = nativeAdLayout,
                    nativeControllerConfig = nativeControllerConfig,
                    adCallBack = stableAdCallBack,
                    )
                nativeAdLayout
            }
        )
    }

}
