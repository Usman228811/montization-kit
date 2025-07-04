package io.monetize.kit.sdk.ads.native_ad

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import io.monetize.kit.sdk.R
import io.monetize.kit.sdk.core.utils.adtype.NativeControllerConfig
import io.monetize.kit.sdk.presentation.viewmodels.NativeAdViewModel
import io.monetize.kit.sdk.presentation.viewmodels.NativeAdViewModelDialog
import io.monetize.kit.sdk.presentation.viewmodels.NativeAdViewModelDialogFactory
import io.monetize.kit.sdk.presentation.viewmodels.NativeAdViewModelFactory


@Composable
fun AdKitNativeAdViewDialog(
    loadNewAd: Boolean = false,
    nativeControllerConfig: NativeControllerConfig

) {

    val context = LocalContext.current
    val nativeAdViewModel: NativeAdViewModelDialog = viewModel(
        factory = NativeAdViewModelDialogFactory(context)
    )


    val activity = LocalActivity.current as Activity
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        nativeAdViewModel.observeLifecycle(lifecycleOwner)
        onDispose {
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {

        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { ctx ->
                val inflater = LayoutInflater.from(ctx)
                val nativeAdLayout = inflater.inflate(R.layout.ad_inflator, null) as LinearLayout
                nativeAdLayout
            },
            update = { adFrame ->
                adFrame.visibility = View.VISIBLE

                nativeAdViewModel.initNativeSingleAdData(
                    mContext = activity,
                    adFrame = adFrame,
                    nativeControllerConfig = nativeControllerConfig,
                    loadNewAd = loadNewAd,
                )
            }
        )
    }

}