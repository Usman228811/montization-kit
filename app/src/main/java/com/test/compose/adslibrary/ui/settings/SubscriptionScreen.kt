package com.test.compose.adslibrary.ui.settings

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.monetize.kit.sdk.core.utils.adtype.BannerControllerConfig
import io.monetize.kit.sdk.core.utils.adtype.NativeControllerConfig
import io.monetize.kit.sdk.core.utils.callbacks.AdCallBack
import io.monetize.kit.sdk.presentation.ui.banner.AdKitBannerAdView
import io.monetize.kit.sdk.presentation.ui.native_ad.AdKitNativeAdView
import network.chaintech.sdpcomposemultiplatform.sdp
import network.chaintech.sdpcomposemultiplatform.ssp

@Composable
fun SubscriptionScreen(

) {

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

        Text(
            modifier = Modifier.fillMaxWidth().padding(all = 5.sdp),
            text = "Subscription Plans",
            fontSize = 12.ssp,
            textDecoration = TextDecoration.Underline,
            textAlign = TextAlign.Center
        )

        SubscriptionOption(
            title = "weekly",
            price = state.weeklyPrice,
            isSelected = state.selectedButtonPos == 0,
            onClick = {
                subscriptionViewModel.updateSelectedButtonPos(activity, 0)

            }
        )
        Spacer(modifier = Modifier.height(12.dp))
        SubscriptionOption(
            title = "Monthly",
            price = state.monthlyPrice,
            isSelected = state.selectedButtonPos == 1,
            onClick = {
                subscriptionViewModel.updateSelectedButtonPos(activity, 1)

            }
        )
        Spacer(modifier = Modifier.height(12.dp))
        SubscriptionOption(
            title = "Yearly",
            price = state.yearlyPrice,
            isSelected = state.selectedButtonPos == 2,
            onClick = {
                subscriptionViewModel.updateSelectedButtonPos(activity, 2)

            }
        )
        Spacer(modifier = Modifier.height(12.dp))


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

        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp), thickness = 1.dp, color = Color.Black
        )

        Text(
            modifier = Modifier.fillMaxWidth().padding(all = 5.sdp),
            text = "Life Time Plan",
            fontSize = 12.ssp,
            textDecoration = TextDecoration.Underline,
            textAlign = TextAlign.Center
        )

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 50.dp),
            textAlign = TextAlign.Center,
            text = state.oneTimePrice,
            color = Color.Black,
            fontSize = 26.sp
        )

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                subscriptionViewModel.purchaseProduct(activity)
            }
        ) {
            Text(
                text = "Purchase One Time"
            )
        }


        AdKitNativeAdView(
            nativeControllerConfig = NativeControllerConfig(
                placementKey = "subscription_native",
                adIdKey = "home_native",
            ),
            adCallBack = object : AdCallBack {
                override fun onAdFailed(reason: String) {
                    Log.d("dddddd", reason)
                }

                override fun onAdClick() {

                }

            }
        )

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )

        Box(modifier = Modifier.fillMaxWidth()) {

            AdKitBannerAdView(
                bannerControllerConfig = BannerControllerConfig(
                    placementKey = "premium_banner",
                    adIdKey = "banner_common"
                ), adCallBack = object : AdCallBack {
                    override fun onAdFailed(reason: String) {
                        Log.d("dddddd", reason)
                    }

                    override fun onAdClick() {
                    }

                }
            )
        }
    }

}


@Composable
fun SubscriptionOption(
    title: String,
    price: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .padding(horizontal = 20.dp)
            .background(
//                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.White,
                color = if (isSelected) Color.Green else Color.LightGray,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Black
            )
            Text(
                text = price,
                fontSize = 16.sp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
            )
        }
    }
}