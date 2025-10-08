package com.test.compose.adslibrary.ui.main

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.test.compose.adslibrary.MainActivity
import com.test.compose.adslibrary.utils.Color579B68
import io.monetize.kit.sdk.ads.interstitial.InterstitialControllerListener
import io.monetize.kit.sdk.ads.rewarded.RewardedControllerListener
import io.monetize.kit.sdk.core.utils.adtype.BannerControllerConfig
import io.monetize.kit.sdk.core.utils.adtype.NativeControllerConfig
import io.monetize.kit.sdk.core.utils.callbacks.AdCallBack
import io.monetize.kit.sdk.core.utils.init.AdKit
import io.monetize.kit.sdk.presentation.ui.banner.AdKitBannerAdView
import io.monetize.kit.sdk.presentation.ui.native_ad.AdKitNativeAdView
import io.monetize.kit.sdk.presentation.ui.native_ad.AdKitNativeAdViewDialog
import network.chaintech.sdpcomposemultiplatform.sdp

@Composable
fun MainScreen(
    gotoSubscription: () -> Unit
) {
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
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {


        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {

            AdKitBannerAdView(
                bannerControllerConfig = BannerControllerConfig(
                    placementKey = "home_banner_top",
                    adIdKey = "home_banner_top"
                ),
                /*adCallBack = object : AdCallBack{
                    override fun onAdFailed(reason: String) {
                        Log.d("dddddd", reason)
                    }

                    override fun onAdClick() {
                        Toast.makeText(activity, "home screen banner top ad click", Toast.LENGTH_SHORT)
                            .show()
                    }
                }*/
            )
        }


        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = Color579B68
                )
                .padding(10.sdp), horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.sdp),
                textAlign = TextAlign.Center,
                text = stringResource(com.test.compose.adslibrary.R.string.hello)
            )



            Button(onClick = {
                AdKit.adKitPref.appLanguageCode =
                    if (AdKit.adKitPref.appLanguageCode == "en") "ur" else "en"
                activity.startActivity(
                    Intent(activity, MainActivity::class.java)
                        .putExtra("languageChange", true)
                )
                activity.finish()
            }) {
                Text("change language")
            }
        }


        Button(onClick = {
            gotoSubscription()
        }) {
            Text("goto subscription screen")
        }
        Button(onClick = {


            AdKit.interHelper.showInterAd(
                activity = activity,
                placementKey = "home_inter",
                adIdKey = "home_inter",
                listener = object : InterstitialControllerListener {
                    override fun onAdClosed(isInterShowed: Boolean, reason: String) {
                        Log.d("dddddd", reason)
                        gotoSubscription()
                    }
                }, "testt", 1
            )

        }) {
            Text("show inter and goto subscription screen")
        }
        Button(onClick = {

            AdKit.rewardHelper.showRewardAd(
                adIdKey = "reward_main",
                placementKey = "inter_btn_plant",
                activity = activity,
                listener = object : RewardedControllerListener {
                    override fun onRewardDismissed(isRewarded: Boolean, reason: String) {

                        Log.d("dddddd", reason)

                        gotoSubscription()

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
                    }

                },
                prefKey = "dddd", counter = 1,
            )
        }) {
            Text("show reward and goto subscription screen")
        }

        Box(modifier = Modifier.fillMaxWidth().padding(top = 20.sdp)) {

            AdKitNativeAdView(
                nativeControllerConfig = NativeControllerConfig(
                    placementKey = "home_native",
                    adIdKey = "home_native",
                ),
                adCallBack = object : AdCallBack {
                    override fun onAdFailed(reason: String) {
                        Log.d("dddddd", reason)
                    }

                    override fun onAdClick() {
                        Toast.makeText(activity, "home screen native ad click", Toast.LENGTH_SHORT)
                            .show()
                    }
                }, callCustomDestroy = { callCustomDestroy ->
                    destroy = callCustomDestroy
                }
            )
        }

        Button(onClick = {
            destroy?.invoke()
        }) {
            Text(text = "destroy native ad")
        }


        Spacer(modifier = Modifier.weight(1f))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {

            AdKitBannerAdView(
                bannerControllerConfig = BannerControllerConfig(
                    placementKey = "home_banner",
                    adIdKey = "home_banner"
                ),
                adCallBack = object : AdCallBack {
                    override fun onAdFailed(reason: String) {
                        Log.d("dddddd", reason)
                    }

                    override fun onAdClick() {
                        Toast.makeText(activity, "home screen banner ad click", Toast.LENGTH_SHORT)
                            .show()
                    }

                }
            )
        }
    }
}

@Composable
fun ExitDialog(onDismissRequest: () -> Unit) {

    var destroy: (() -> Unit)? = null
    val activity = LocalActivity.current as Activity

    Dialog(
        properties = DialogProperties(
            usePlatformDefaultWidth = false // Needed for full width
        ), onDismissRequest = {
            destroy?.invoke()
            onDismissRequest()
        }) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
        ) {

            AdKitNativeAdViewDialog(
                nativeControllerConfig = NativeControllerConfig(
                    "exit_native",
                    "exit_native",
                ),
                adCallBack = object : AdCallBack {
                    override fun onAdFailed(reason: String) {

                    }

                    override fun onAdClick() {
                    }

                }, callCustomDestroy = {
                    destroy = it
                }
            )

            Button(modifier = Modifier
                .fillMaxWidth()
                .padding(all = 15.sdp), onClick = {
                activity.finish()
            }) {
                Text(
                    text = "Exit"
                )
            }
        }
    }
}