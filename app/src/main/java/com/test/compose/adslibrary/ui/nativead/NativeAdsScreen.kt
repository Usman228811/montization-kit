package com.test.compose.adslibrary.ui.nativead

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.monetize.kit.sdk.core.utils.adtype.NativeControllerConfig
import io.monetize.kit.sdk.core.utils.callbacks.AdCallBack
import io.monetize.kit.sdk.presentation.ui.native_ad.AdKitNativeAdView

@Composable
fun NativeAdsScreen(
    gotoFullScreenNative: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF4F6FF),
                        Color(0xFFE9ECFF)
                    )
                )
            )
    ) {

        // HEADER
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF5B5FEF),
                            Color(0xFF2E2F6E)
                        )
                    )
                )
                .padding(vertical = 18.dp)
        ) {

            Text(
                text = "Native Ads Showcase",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // CONTENT
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {

            Spacer(modifier = Modifier.height(14.dp))

            NativeSection(title = "Large Native") {

                AdKitNativeAdView(
                    nativeControllerConfig = NativeControllerConfig(
                        placementKey = "large_native",
                        adIdKey = "large_native"
                    ),
                    adCallBack = emptyAdCallback()
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            NativeSection(title = "Small Native With Media") {

                AdKitNativeAdView(
                    nativeControllerConfig = NativeControllerConfig(
                        placementKey = "small_native_media_view",
                        adIdKey = "small_native_media_view"
                    ),
                    adCallBack = emptyAdCallback()
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            NativeSection(title = "Small Native") {

                AdKitNativeAdView(
                    nativeControllerConfig = NativeControllerConfig(
                        placementKey = "small_native",
                        adIdKey = "small_native"
                    ),
                    adCallBack = emptyAdCallback()
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            NativeSection(title = "Small Native Mini") {

                AdKitNativeAdView(
                    nativeControllerConfig = NativeControllerConfig(
                        placementKey = "small_native_mini",
                        adIdKey = "small_native_mini"
                    ),
                    adCallBack = emptyAdCallback()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // FOOTER BUTTON
        Button(
            onClick = { gotoFullScreenNative() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF5B5FEF)
            )
        ) {

            Text(
                text = "Full Screen Native",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White
            )
        }
    }
}

@Composable
fun NativeSection(
    title: String,
    content: @Composable () -> Unit
) {

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = title,
            modifier = Modifier.padding(vertical = 10.dp),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2E2F6E)
        )

        content()
    }
}

fun emptyAdCallback() = object : AdCallBack {
    override fun onAdFailed(reason: String) {}
    override fun onAdShow() {}
    override fun onAdClick() {}
}