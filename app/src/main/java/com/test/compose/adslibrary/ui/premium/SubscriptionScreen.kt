package com.test.compose.adslibrary.ui.premium

import android.app.Activity
import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.test.compose.adslibrary.ui.nativead.emptyAdCallback
import io.monetize.kit.sdk.core.utils.adtype.BannerControllerConfig
import io.monetize.kit.sdk.core.utils.adtype.NativeControllerConfig
import io.monetize.kit.sdk.presentation.ui.banner.AdKitBannerAdView
import io.monetize.kit.sdk.presentation.ui.native_ad.AdKitNativeAdView
@Composable
fun SubscriptionScreen() {

    val factory = remember { SubscriptionViewModelFactory() }
    val subscriptionViewModel: SubscriptionViewModel = viewModel(factory = factory)

    val activity = LocalActivity.current as Activity
    val state by subscriptionViewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        subscriptionViewModel.loadProducts(activity)
    }

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
                text = "Premium Plans",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {

            SectionTitle("Subscription Plans")

            SubscriptionOption(
                title = "Remove Ads",
                price = state.removeAdsPrice,
                isSelected = state.selectedButtonPos == 0,
                onClick = { subscriptionViewModel.updateSelectedButtonPos(0) }
            )

            Spacer(Modifier.height(12.dp))

            SubscriptionOption(
                title = "Feature 1",
                price = state.feature1Price,
                isSelected = state.selectedButtonPos == 1,
                onClick = { subscriptionViewModel.updateSelectedButtonPos(1) }
            )

            Spacer(Modifier.height(12.dp))

            SubscriptionOption(
                title = "Feature 2",
                price = state.feature2Price,
                isSelected = state.selectedButtonPos == 2,
                onClick = { subscriptionViewModel.updateSelectedButtonPos(2) }
            )

            Spacer(Modifier.height(12.dp))

            SubscriptionOption(
                title = "Feature 3",
                price = state.feature3Price,
                isSelected = state.selectedButtonPos == 3,
                onClick = { subscriptionViewModel.updateSelectedButtonPos(3) }
            )

            Spacer(Modifier.height(16.dp))

            PrimaryButton(
                text = state.buttonText,
                onClick = { subscriptionViewModel.purchase(activity) }
            )

            Spacer(Modifier.height(28.dp))

            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))

            Spacer(Modifier.height(20.dp))

            SectionTitle("Lifetime Plan")

            Text(
                text = state.oneTimePrice,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 30.dp),
                textAlign = TextAlign.Center,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E2F6E)
            )

            PrimaryButton(
                text = state.buttonTextLifeTime,
                onClick = { subscriptionViewModel.purchaseProduct(activity) }
            )

            Spacer(Modifier.height(20.dp))

            AdKitNativeAdView(
                nativeControllerConfig = NativeControllerConfig(
                    placementKey = "subscription_native",
                    adIdKey = "native_common"
                ),
                adCallBack = emptyAdCallback()
            )

            Spacer(Modifier.height(20.dp))
        }

        // BANNER
        AdKitBannerAdView(
            bannerControllerConfig = BannerControllerConfig(
                placementKey = "premium_banner",
                adIdKey = "banner_common"
            ),
            adCallBack = emptyAdCallback()
        )
    }
}

@Composable
fun SubscriptionOption(
    title: String,
    price: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                Color(0xFFEDEBFF)
            else
                Color.White
        ),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) Color(0xFF5B5FEF) else Color.Black
            )

            Text(
                text = price,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E2F6E)
            )
        }
    }
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit
) {

    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF5B5FEF)
        )
    ) {

        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
fun SectionTitle(title: String) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(width = 4.dp, height = 18.dp)
                .background(Color(0xFF5B5FEF), RoundedCornerShape(50))
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2E2F6E)
        )
    }
}