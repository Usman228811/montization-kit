package com.test.compose.adslibrary.ui.settings

import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import io.monetize.kit.sdk.core.utils.adtype.BannerControllerConfig
import io.monetize.kit.sdk.core.utils.adtype.NativeControllerConfig
import io.monetize.kit.sdk.presentation.ui.banner.AdKitBannerAdView
import io.monetize.kit.sdk.presentation.ui.native_ad.AdKitNativeAdView

@Composable
fun SubscriptionScreen(

) {

    val context = LocalContext.current
    val factory = remember { SubscriptionViewModelFactory() }
    val subscriptionViewModel: SubscriptionViewModel = viewModel(factory = factory)

    val activity = LocalActivity.current as Activity

    val state by subscriptionViewModel.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {

        LaunchedEffect(Unit) {
            subscriptionViewModel.loadProducts(
                activity,
                listOf(
                    "weekly_subscription2",
                    "monthly1_subscription",
                    "yearly_subscription"
                )
            )
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                subscriptionViewModel.updateSelectedButtonPos(0)

            }
        ) {
            Text(
                text = "weekly ${state.weeklyPrice}"
            )
        }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {

                subscriptionViewModel.updateSelectedButtonPos(1)
            }
        ) {
            Text(
                text = "monthly ${state.monthlyPrice}"
            )
        }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {

                subscriptionViewModel.updateSelectedButtonPos(2)
            }
        ) {
            Text(
                text = "yearly ${state.yearlyPrice}"
            )
        }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                subscriptionViewModel.purchase(activity)
            }
        ) {
            Text(
                text = state.buttonText
            )
        }


        AdKitNativeAdView(
            nativeControllerConfig = NativeControllerConfig(
                placementKey = "subscription_native",
                adIdKey = "subscription_native",
            )
        )

        Box(modifier = Modifier.fillMaxWidth()) {

            AdKitBannerAdView(
                bannerControllerConfig = BannerControllerConfig(
                    placementKey = "premium_banner",
                    adIdKey = "premium_banner"
                )
            )
        }
    }

}