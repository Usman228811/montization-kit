package com.test.compose.adslibrary.ui.main

import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import io.monetize.kit.sdk.presentation.ui.banner.AdKitBannerAdView
import io.monetize.kit.sdk.ads.interstitial.InterstitialControllerListener
import io.monetize.kit.sdk.presentation.ui.native_ad.AdKitNativeAdView
import io.monetize.kit.sdk.core.utils.adtype.BannerControllerConfig
import io.monetize.kit.sdk.core.utils.adtype.NativeControllerConfig
import io.monetize.kit.sdk.core.utils.init.AdKit

@Composable
fun MainScreen(
    gotoSubscription: () -> Unit
) {
    val context = LocalContext.current

    val activity = LocalActivity.current as Activity


    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {

        Button(onClick = {
            AdKit.interHelper.showInterAd(
                adIdKey = "inter_common",
                placementKey = "inter_btn_plant",
                activity = activity,
                listener = object : InterstitialControllerListener {
                    override fun onAdClosed() {
                        gotoSubscription()
                    }

                },
            )
        }) {
            Text("showinter and to got subscripption screen")
        }
        Spacer(modifier = Modifier.weight(1f))

        AdKitNativeAdView(
            nativeControllerConfig = NativeControllerConfig(
                key = "home_native"
            )
        )


        Spacer(modifier = Modifier.weight(1f))

        Box(modifier = Modifier.fillMaxWidth()) {

            AdKitBannerAdView(
                bannerControllerConfig = BannerControllerConfig(
                    placementKey = "home_banner",
                ),
            )
        }
    }
}