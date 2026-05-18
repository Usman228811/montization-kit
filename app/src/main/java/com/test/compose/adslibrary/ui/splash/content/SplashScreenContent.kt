package com.test.compose.adslibrary.ui.splash.content

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.test.compose.adslibrary.ui.splash.state.SplashScreenState

@Composable
fun SplashScreenContent(
    state: SplashScreenState,
    showAd: () -> Unit
) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF4F6FF),
                        Color(0xFFE9ECFF)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {

        when {

            // 1️⃣ AD LOADING STATE
            !state.onAdLoaded -> {

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    CircularProgressIndicator(
                        color = Color(0xFF5B5FEF)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Preparing Experience...",
                        color = Color(0xFF2E2F6E),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // 2️⃣ AUTO SHOW MODE (no UI needed)
            state.onAdLoaded && state.loadAndShow -> {


                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    CircularProgressIndicator(color = Color(0xFF5B5FEF))

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Launching Ad...",
                        color = Color(0xFF2E2F6E)
                    )
                }
            }

            // 3️⃣ MANUAL SHOW MODE (button)
            state.onAdLoaded && !state.loadAndShow -> {

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(
                                Color(0xFF5B5FEF).copy(alpha = 0.1f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🎬",
                            fontSize = 30.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Ad Ready",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E2F6E)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Tap below to continue",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = showAd,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF5B5FEF)
                        )
                    ) {
                        Text(
                            text = "Show Ad & Continue",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}