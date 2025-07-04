package io.monetize.kit.sdk.ads.banner

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
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
import io.monetize.kit.sdk.core.utils.adtype.BannerControllerConfig
import io.monetize.kit.sdk.presentation.viewmodels.BannerAdViewModel
import io.monetize.kit.sdk.presentation.viewmodels.BannerAdViewModelFactory


@Composable
fun AdKitBannerAdView(

    bannerControllerConfig: BannerControllerConfig
) {

    val context = LocalContext.current
    val bannerAdViewModel: BannerAdViewModel = viewModel(
        factory = BannerAdViewModelFactory(context)
    )

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        bannerAdViewModel.observeLifecycle(lifecycleOwner)
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

                bannerAdViewModel.initSingleBannerData(
                    mContext = context as Activity,
                    bannerControllerConfig = bannerControllerConfig,
                    adFrame = adFrame
                )
            }
        )
    }

}