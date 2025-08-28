package com.test.compose.adslibrary.ui.main

import android.app.Activity
import android.util.Log
import android.widget.Toast
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.monetize.kit.sdk.ads.interstitial.InterstitialControllerListener
import io.monetize.kit.sdk.core.utils.adtype.BannerControllerConfig
import io.monetize.kit.sdk.core.utils.adtype.NativeControllerConfig
import io.monetize.kit.sdk.core.utils.init.AdKit
import io.monetize.kit.sdk.presentation.ui.banner.AdKitBannerAdView
import io.monetize.kit.sdk.presentation.ui.native_ad.AdKitNativeAdView
import io.monetize.kit.sdk.presentation.ui.native_ad.AdKitNativeAdViewDialog

@Composable
fun MainScreen(
    gotoSubscription: () -> Unit
) {
    LocalContext.current
    val activity = LocalActivity.current as Activity
    var destroy: (() -> Unit)? = null

    var showExit by remember { mutableStateOf(false) }

    BackHandler { showExit = true }

    if (showExit) {
        ExitDialog(onDismissRequest = {
            showExit = false
        })
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {

        Button(onClick = {

            AdKit.interHelper.showInterAd(
                activity = activity,
                placementKey = "home_inter",
                adIdKey = "home_inter",
                listener = object : InterstitialControllerListener {
                    override fun onAdClosed(isInterShowed: Boolean) {
                        Log.d("iooioi", "onAdClosed: $isInterShowed")
                    }
                }

            )

//            AdKit.rewardHelper.showRewardAd(
//                adIdKey = "reward_main",
//                placementKey = "inter_btn_plant",
//                activity = activity,
//                listener = object : RewardedControllerListener {
//                    override fun onRewardDismissed(isRewarded: Boolean) {
//                        Log.d("ioioioi", "onRewardDismissed: $isRewarded")
//                        if (isRewarded.not()) {
//                            if (AdKit.adKitPref.getInterInt("common_pref", 0) >= 2) {
//                                Log.d("ioioioi", "onRewardDismissed: try again")
//                            } else {
//                                Log.d("ioioioi", "onRewardDismissed: continue")
//                            }
//                        } else {
//
//                            gotoSubscription()
//                            Log.d("ioioioi", "onRewardDismissed: continue")
//                        }
//                    }
//
//                },
//            )
        }) {
            Text("showinter and to got subscripption screen")
        }
        Spacer(modifier = Modifier.weight(1f))

        AdKitNativeAdView(
            nativeControllerConfig = NativeControllerConfig(
                placementKey = "home_native",
                adIdKey = "home_native",
                ctaColor = "#FFBB86FC",
                adType = 3
            ), onFail = {

            },
            onAdClick = {
                Toast.makeText(activity, "home screen native ad click", Toast.LENGTH_SHORT).show()
            }, callCustomDestroy = { callCustomDestroy ->
                destroy = callCustomDestroy
            }
        )

        Button(onClick = {
            destroy?.invoke()
        }) {
            Text(text = "destroy native ad")
        }


        Spacer(modifier = Modifier.weight(1f))

        Box(modifier = Modifier.fillMaxWidth()) {

            AdKitBannerAdView(
                bannerControllerConfig = BannerControllerConfig(
                    placementKey = "home_banner",
                    adIdKey = "home_banner"
                ),
                onAdClick = {
                    Toast.makeText(activity, "home screen banner ad click", Toast.LENGTH_SHORT)
                        .show()
                })
        }
    }
}

@Composable
fun ExitDialog(onDismissRequest: () -> Unit) {

    var destroy: (() -> Unit)? = null

    Dialog(
        properties = DialogProperties(
            usePlatformDefaultWidth = false // Needed for full width
        ), onDismissRequest = {
            destroy?.invoke()
            onDismissRequest()
        }) {

        Column(modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)) {

            AdKitNativeAdViewDialog(
                nativeControllerConfig = NativeControllerConfig(
                    "exit_native",
                    "exit_native",
                    2,
                ), callCustomDestroy = {
                    destroy = it
                }
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text(
                    text = "This is a minimal dialog",
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.Center),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}