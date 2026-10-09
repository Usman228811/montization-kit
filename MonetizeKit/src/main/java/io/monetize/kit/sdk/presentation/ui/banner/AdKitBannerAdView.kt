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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import io.monetize.kit.sdk.R
import io.monetize.kit.sdk.core.utils.adtype.BannerControllerConfig
import io.monetize.kit.sdk.core.utils.callbacks.AdCallBack
import io.monetize.kit.sdk.presentation.ui.rememberAdCallBack
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
    val stableAdCallBack = rememberAdCallBack(adCallBack)

    DisposableEffect(bannerAdViewModel, lifecycleOwner) {
        val observer = bannerAdViewModel.observeLifecycle(lifecycleOwner)
        onDispose {
            // Remove the observer, otherwise every re-entry adds another one and
            // onResume runs once per stale observer.
            // The ad itself is released in ViewModel.onCleared / callCustomDestroy.
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }


    LaunchedEffect(bannerAdViewModel) {
        callCustomDestroy?.invoke {
            bannerAdViewModel.onDestroy()
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {

        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { ctx ->
                val inflater = LayoutInflater.from(ctx)
                val adFrame = inflater.inflate(R.layout.ad_inflator, null) as LinearLayout
                adFrame.visibility = View.VISIBLE

                // Set up the ad once per view; update{} would run again on every parent recomposition
                bannerAdViewModel.initSingleBannerData(
                    mContext = activity,
                    bannerControllerConfig = bannerControllerConfig,
                    adFrame = adFrame,
                    adCallBack = stableAdCallBack
                )
                adFrame
            }
        )
    }

}