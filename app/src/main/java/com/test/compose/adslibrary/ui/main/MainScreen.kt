package com.test.compose.adslibrary.ui.main

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.test.compose.adslibrary.MainActivity
import com.test.compose.adslibrary.ui.nativead.emptyAdCallback
import com.test.compose.adslibrary.xml.MainXmlActivity
import io.monetize.kit.sdk.core.utils.adtype.BannerControllerConfig
import io.monetize.kit.sdk.core.utils.adtype.NativeControllerConfig
import io.monetize.kit.sdk.core.utils.init.AdKit
import io.monetize.kit.sdk.presentation.ui.banner.AdKitBannerAdView
import io.monetize.kit.sdk.presentation.ui.native_ad.AdKitNativeAdView


var LIFE_TIME_ID = "android.test.purchased"
var REMOVE_ADS_ID = "remove_ads"
var FEATURE_1 = "unlockphotos"
var FEATURE_2 = "duplicate_scan"
var FEATURE_3 = "unlockall"

@Composable
fun MainScreen(
    gotoBannerScreen: () -> Unit,
    gotoNativeAdsScreen: () -> Unit,
    gotoSubscription: () -> Unit,
    gotoMainScreen2: () -> Unit,
    gotoInterAds: () -> Unit,
) {

    val activity = LocalActivity.current as Activity
    var showExit by remember { mutableStateOf(false) }

    BackHandler { showExit = true }

    if (showExit) {
        ExitDialog(
            onDismissRequest = { showExit = false }
        )
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

        // HEADER CARD (Premium)
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
                .padding(vertical = 26.dp),
            contentAlignment = Alignment.Center
        ) {

            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                Text(
                    text = "🚀 AdKit Demo",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = stringResource(com.test.compose.adslibrary.R.string.hello),
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        AdKit.adKitPref.appLanguageCode =
                            if (AdKit.adKitPref.appLanguageCode == "en") "ur" else "en"

                        activity.startActivity(
                            Intent(activity, MainActivity::class.java).putExtra(
                                "languageChange",
                                true
                            ).apply {
                                flags =
                                    Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }.also {
                                activity.startActivity(it)
                            }
                        )
                        activity.finish()
                    },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White
                    )
                ) {
                    Text(
                        text = "Change Language",
                        color = Color(0xFF2E2F6E),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {

            MenuButton("Banner Ads", gotoBannerScreen)
            MenuButton("Native Ads", gotoNativeAdsScreen)
            MenuButton("Interstitial Ads", gotoInterAds)
            MenuButton("Subscription", gotoSubscription)
            MenuButton("Main Screen 2", gotoMainScreen2)

            MenuButton("Main XML Activity") {
                activity.startActivity(Intent(activity, MainXmlActivity::class.java))
            }

            Spacer(modifier = Modifier.height(20.dp))
            AdKitNativeAdView(
                nativeControllerConfig = NativeControllerConfig(
                    placementKey = "large_native",
                    adIdKey = "large_native"
                ),
                adCallBack = emptyAdCallback()
            )
        }
    }
}

@Composable
fun MenuButton(
    text: String,
    onClick: () -> Unit
) {

    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
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
fun ExitDialog(onDismissRequest: () -> Unit) {

    val activity = LocalActivity.current as Activity

    var destroyNative by remember { mutableStateOf<(() -> Unit)?>(null) }
    var destroyBanner by remember { mutableStateOf<(() -> Unit)?>(null) }

    Dialog(
        properties = DialogProperties(usePlatformDefaultWidth = false),
        onDismissRequest = {
            destroyNative?.invoke()
            destroyBanner?.invoke()
            onDismissRequest()
        }
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
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
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = "Exit App?",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            // ADS
            AdKitBannerAdView(
                bannerControllerConfig = BannerControllerConfig(
                    placementKey = "exit_banner",
                    adIdKey = "banner_common"
                ),
                adCallBack = emptyAdCallback(),
                callCustomDestroy = { destroyBanner = it }
            )

            AdKitNativeAdView(
                nativeControllerConfig = NativeControllerConfig(
                    "exit_native",
                    "native_common",
                    loadNextAd = false
                ),
                adCallBack = emptyAdCallback(),
                callCustomDestroy = { destroyNative = it }
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF5B5FEF)
                ),
                onClick = { activity.finish() }
            ) {
                Text(
                    text = "Exit",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}