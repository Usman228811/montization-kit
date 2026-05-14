package com.test.compose.adslibrary.ui.main2

import android.util.Log
import androidx.compose.foundation.background
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.monetize.kit.sdk.core.utils.adtype.NativeControllerConfig
import io.monetize.kit.sdk.core.utils.callbacks.AdCallBack
import io.monetize.kit.sdk.presentation.ui.native_ad.AdKitNativeAdView

@Composable
fun MainScreen2() {

    var screenType by remember { mutableIntStateOf(1) }

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

        // TOP SWITCH
        Row(modifier = Modifier.fillMaxWidth()) {

            ToggleButton(
                text = "Screen 1",
                selected = screenType == 1,
                modifier = Modifier.weight(1f)
            ) {
                screenType = 1
            }

            ToggleButton(
                text = "Screen 2",
                selected = screenType == 2,
                modifier = Modifier.weight(1f)
            ) {
                screenType = 2
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // CONTENT
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {

            Screen1(screenType = screenType)
        }
    }
}

@Composable
fun ToggleButton(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {

    Button(
        onClick = onClick,
        modifier = modifier
            .padding(6.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected)
                Color(0xFF5B5FEF)
            else
                Color(0xFFE0E3FF)
        )
    ) {

        Text(
            text = text,
            color = if (selected) Color.White else Color(0xFF2E2F6E),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun Screen1(
    screenType: Int
) {

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        val config = if (screenType == 1) {
            NativeControllerConfig("large_native", "large_native")
        } else {
            NativeControllerConfig("small_native", "small_native")
        }

        AdKitNativeAdView(
            nativeControllerConfig = config,
            adCallBack = object : AdCallBack {

                override fun onAdFailed(reason: String) {
                    Log.d("Screen1", "failed: $reason")
                }

                override fun onAdShow() {}

                override fun onAdClick() {}
            }
        )
    }
}