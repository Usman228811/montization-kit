package com.test.compose.adslibrary.ui.inter

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.monetize.kit.sdk.ads.interstitial.InterstitialControllerListener
import io.monetize.kit.sdk.ads.rewarded.RewardedControllerListener
import io.monetize.kit.sdk.core.utils.init.AdKit
@Composable
fun InterAdsScreen() {

    val activity = LocalActivity.current as Activity

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
                text = "Interstitial & Reward Ads",
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
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {

            AdButton(
                text = "Inter Preload (No Counter)"
            ) {
                AdKit.interHelper.showInterAd(
                    activity = activity,
                    placementKey = "inter_preload",
                    adIdKey = "inter_common",
                    listener = emptyInterListener()
                )
            }

            AdButton(
                text = "Inter Preload (Counter = 1)"
            ) {
                AdKit.interHelper.showInterAd(
                    activity = activity,
                    placementKey = "inter_preload_with_counter",
                    adIdKey = "inter_common",
                    listener = emptyInterListener(),
                    "inter_pref_key",
                    1
                )
            }

            AdButton(
                text = "Inter Instant (No Counter)"
            ) {
                AdKit.interHelper.showInterAd(
                    activity = activity,
                    placementKey = "inter_instant",
                    adIdKey = "inter_common",
                    listener = emptyInterListener()
                )
            }

            AdButton(
                text = "Inter Instant (Counter = 1)"
            ) {
                AdKit.interHelper.showInterAd(
                    activity = activity,
                    placementKey = "inter_instant_with_counter",
                    adIdKey = "inter_common",
                    listener = emptyInterListener(),
                    "inter_pref_key",
                    1
                )
            }

            AdButton(
                text = "Reward Ad"
            ) {
                AdKit.rewardHelper.showRewardAd(
                    activity = activity,
                    placementKey = "reward_ad",
                    adIdKey = "reward_common",
                    listener = object : RewardedControllerListener {
                        override fun onRewardDismissed(
                            isRewarded: Boolean,
                            reason: String
                        ) {
                            Toast.makeText(
                                activity,
                                if (isRewarded) "Reward Granted" else "Reward Not Granted",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun AdButton(
    text: String,
    onClick: () -> Unit
) {

    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF5B5FEF)
        )
    ) {

        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color.White
        )
    }
}

fun emptyInterListener() = object : InterstitialControllerListener {
    override fun onAdClosed(isInterShowed: Boolean, reason: String) {}
}