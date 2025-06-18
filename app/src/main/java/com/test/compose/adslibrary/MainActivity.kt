package com.test.compose.adslibrary

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.monetize.kit.sdk.ads.banner.AdKitBannerAdView
import io.monetize.kit.sdk.ads.interstitial.AdKitInterHelper
import io.monetize.kit.sdk.ads.interstitial.InterstitialControllerListener
import io.monetize.kit.sdk.ads.native_ad.AdKitNativeAdView
import io.monetize.kit.sdk.core.utils.adtype.BannerControllerConfig
import io.monetize.kit.sdk.core.utils.adtype.NativeControllerConfig
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    val adKitInterHelper: AdKitInterHelper by inject()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            Scaffold { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(Color.Red)
                ) {

                    Button(onClick = {
                        adKitInterHelper.showInterAd(
                            activity = this@MainActivity,
                            enable = true,
                            interInstant = true,
                            listener = object : InterstitialControllerListener {
                                override fun onAdClosed() {
                                    Toast.makeText(
                                        this@MainActivity,
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
                                adId = "ca-app-pub-3940256099942544/9214589741",
                                isAdEnable = true
                            )
                        )
                    }
                }
            }
        }
    }
}

