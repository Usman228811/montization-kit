package io.monetize.kit.sdk.presentation.ui.banner

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
import io.monetize.kit.sdk.core.utils.adtype.BannerControllerConfig
import io.monetize.kit.sdk.core.utils.callbacks.AdCallBack
import io.monetize.kit.sdk.presentation.viewmodels.BannerAdViewModel
import io.monetize.kit.sdk.presentation.viewmodels.BannerAdViewModelFactory


@Composable
fun AdKitBannerAdView(
    bannerControllerConfig: BannerControllerConfig,
    adCallBack: AdCallBack ?= null,
    callCustomDestroy: ((() -> Unit) -> Unit)? = null


) {

    val activity = LocalActivity.current as Activity
    val bannerAdViewModel: BannerAdViewModel = viewModel(
        key = bannerControllerConfig.placementKey,
        factory = BannerAdViewModelFactory()
    )

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(bannerAdViewModel, lifecycleOwner) {
        bannerAdViewModel.observeLifecycle(lifecycleOwner)
        onDispose {
//            bannerAdViewModel.onDestroy()
        }
    }


    callCustomDestroy?.invoke {
        bannerAdViewModel.onDestroy()
    }

    Column(modifier = Modifier.fillMaxWidth()) {

        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { ctx ->
                val inflater = LayoutInflater.from(ctx)
                inflater.inflate(R.layout.ad_inflator, null) as LinearLayout
            },
            update = { adFrame ->
                adFrame.visibility = View.VISIBLE

                bannerAdViewModel.initSingleBannerData(
                    mContext = activity,
                    bannerControllerConfig = bannerControllerConfig,
                    adFrame = adFrame,
                    adCallBack = adCallBack
                )
            }
        )
    }

}