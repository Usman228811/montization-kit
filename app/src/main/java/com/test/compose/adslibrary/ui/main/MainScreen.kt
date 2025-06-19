package com.test.compose.adslibrary.ui.main

import android.app.Activity
import android.widget.Toast
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
import io.monetize.kit.sdk.ads.banner.AdKitBannerAdView
import io.monetize.kit.sdk.ads.interstitial.AdSdkInterHelper
import io.monetize.kit.sdk.ads.interstitial.InterstitialControllerListener
import io.monetize.kit.sdk.ads.native_ad.AdKitNativeAdView
import io.monetize.kit.sdk.core.utils.adtype.BannerControllerConfig
import io.monetize.kit.sdk.core.utils.adtype.CollapsableConfig
import io.monetize.kit.sdk.core.utils.adtype.NativeControllerConfig
import org.koin.compose.koinInject

@Composable
fun MainScreen(
    adSdkInterHelper :AdSdkInterHelper = koinInject()
) {

    val activity = LocalActivity.current as Activity
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {



        Button(onClick = {
            adSdkInterHelper.showInterAd(
                activity = activity,
                enable = true,
                interInstant = true,
                listener = object : InterstitialControllerListener {
                    override fun onAdClosed() {
                        Toast.makeText(
                            activity,
                            "ad closed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }
            )
        }) {
            Text("showinter")
        }
        Spacer(modifier = Modifier.weight(1f))

        AdKitNativeAdView(
            nativeControllerConfig = NativeControllerConfig(
                key = "home_native",
                adId = "ca-app-pub-3940256099942544/2247696110",
                isAdEnable = true,
                adType = 0
            )
        )


        Spacer(modifier = Modifier.weight(1f))

        Box(modifier = Modifier.fillMaxWidth()) {

            AdKitBannerAdView(
                bannerControllerConfig = BannerControllerConfig(
                    key = "home_banner",
                    adId = "ca-app-pub-3940256099942544/2014213617",
                    isAdEnable = true,
                    collapsableConfig = CollapsableConfig(
                        isBottom = true
                    )
                )
            )
        }
    }
}