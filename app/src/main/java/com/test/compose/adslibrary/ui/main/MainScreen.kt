package com.test.compose.adslibrary.ui.main

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import io.monetize.kit.sdk.ads.rewarded.RewardedControllerListener
import io.monetize.kit.sdk.core.utils.adtype.BannerControllerConfig
import io.monetize.kit.sdk.core.utils.adtype.NativeControllerConfig
import io.monetize.kit.sdk.core.utils.init.AdKit
import io.monetize.kit.sdk.presentation.ui.banner.AdKitBannerAdView
import io.monetize.kit.sdk.presentation.ui.native_ad.AdKitNativeAdView

@Composable
fun MainScreen(
    gotoSubscription: () -> Unit
) {
    val context = LocalContext.current
    val activity = LocalActivity.current as Activity


    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {

        Button(onClick = {
            AdKit.rewardHelper.showRewardAd(
                adIdKey = "reward_main",
                placementKey = "inter_btn_plant",
                activity = activity,
                listener = object : RewardedControllerListener {
                    override fun onRewardDismissed(isRewarded: Boolean) {
                        Log.d("ioioioi", "onRewardDismissed: $isRewarded")
                        if (isRewarded.not()) {
                            if (AdKit.adKitPref.getInterInt("common_pref", 0)>= 2) {
                                Log.d("ioioioi", "onRewardDismissed: try again")
                            }else{
                                Log.d("ioioioi", "onRewardDismissed: continue")
                            }
                        }else{
                            Log.d("ioioioi", "onRewardDismissed: continue")
                        }
                    }

                },
                prefKey = "common_pref",
                counter = 2 //from remote conigs
            )
        }) {
            Text("showinter and to got subscripption screen")
        }
        Spacer(modifier = Modifier.weight(1f))

        AdKitNativeAdView(
            nativeControllerConfig = NativeControllerConfig(
                placementKey = "home_native",
                adIdKey = "home_native",
                ctaColor = "#FFBB86FC",
                adType = 1
            ),
            onAdClick = {
                Toast.makeText(activity, "home screen native ad click", Toast.LENGTH_SHORT).show()
            }
        )


        Spacer(modifier = Modifier.weight(1f))

        Box(modifier = Modifier.fillMaxWidth()) {

            AdKitBannerAdView(
                bannerControllerConfig = BannerControllerConfig(
                    placementKey = "home_banner",
                    adIdKey = "home_banner"
                ),
                onAdClick ={
                    Toast.makeText(activity, "home screen banner ad click", Toast.LENGTH_SHORT).show()
                })
        }
    }
}