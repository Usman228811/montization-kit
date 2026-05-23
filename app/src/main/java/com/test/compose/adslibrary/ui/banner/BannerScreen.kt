package com.test.compose.adslibrary.ui.banner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.test.compose.adslibrary.ui.nativead.emptyAdCallback
import io.monetize.kit.sdk.core.utils.adtype.BannerControllerConfig
import io.monetize.kit.sdk.core.utils.callbacks.AdCallBack
import io.monetize.kit.sdk.presentation.ui.banner.AdKitBannerAdView
@Composable
fun BannerScreen() {

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
                text = "Banner Ads Showcase",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Top Banner
        BannerSection(title = "Top Collapsible Banner") {

            AdKitBannerAdView(
                bannerControllerConfig = BannerControllerConfig(
                    placementKey = "top_banner_collapsable",
                    adIdKey = "top_banner_collapsable"
                ),
                adCallBack = emptyAdCallback()
            )
        }

        // Scroll Content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {

            Spacer(modifier = Modifier.height(14.dp))

            BannerSection(title = "Adaptive Banner") {

                AdKitBannerAdView(
                    bannerControllerConfig = BannerControllerConfig(
                        placementKey = "adaptive_banner",
                        adIdKey = "adaptive_banner"
                    ),
                    adCallBack = emptyAdCallback()
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            BannerSection(title = "Large Banner") {

                AdKitBannerAdView(
                    bannerControllerConfig = BannerControllerConfig(
                        placementKey = "large_banner",
                        adIdKey = "large_banner"
                    ),
                    adCallBack = emptyAdCallback()
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            BannerSection(title = "Medium Rectangle Banner") {

                AdKitBannerAdView(
                    bannerControllerConfig = BannerControllerConfig(
                        placementKey = "med_rec_banner",
                        adIdKey = "med_rec_banner"
                    ),
                    adCallBack = emptyAdCallback()
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            BannerSection(title = "Large Anchored Banner") {

                AdKitBannerAdView(
                    bannerControllerConfig = BannerControllerConfig(
                        placementKey = "large_anchored_banner",
                        adIdKey = "large_anchored_banner"
                    ),
                    adCallBack = emptyAdCallback()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Bottom Banner
        BannerSection(title = "Bottom Collapsible Banner") {

            AdKitBannerAdView(
                bannerControllerConfig = BannerControllerConfig(
                    placementKey = "bottom_banner_collapsable",
                    adIdKey = "bottom_banner_collapsable"
                ),
                adCallBack = emptyAdCallback()
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun BannerSection(
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