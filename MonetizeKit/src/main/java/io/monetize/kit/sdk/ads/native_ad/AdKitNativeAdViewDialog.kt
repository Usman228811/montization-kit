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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import io.monetize.kit.sdk.R
import io.monetize.kit.sdk.core.utils.adtype.NativeControllerConfig
import io.monetize.kit.sdk.presentation.viewmodels.NativeAdViewModelDialog
import org.koin.androidx.compose.koinViewModel


@Composable
fun AdKitNativeAdViewDialog(
    nativeAdViewModel: NativeAdViewModelDialog = koinViewModel(),
    loadNewAd: Boolean = false,
    nativeControllerConfig: NativeControllerConfig

) {

    val tet = LocalActivity.current as Activity
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
                    mContext = tet,
                    adFrame = adFrame,
                    nativeControllerConfig = nativeControllerConfig,
                    loadNewAd = loadNewAd,
                )
            }
        )
    }

}