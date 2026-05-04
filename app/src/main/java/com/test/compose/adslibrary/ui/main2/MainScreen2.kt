package com.test.compose.adslibrary.ui.main2

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.monetize.kit.sdk.core.utils.adtype.NativeControllerConfig
import io.monetize.kit.sdk.core.utils.callbacks.AdCallBack
import io.monetize.kit.sdk.presentation.ui.native_ad.AdKitNativeAdView

@Composable
fun MainScreen2() {


    var screenType by remember { mutableIntStateOf(1) }
    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth()) {

            Button(modifier = Modifier.weight(1f), onClick = {
                screenType = 1
            }) {
                Text(
                    text = "screen 1"
                )
            }
            Button(modifier = Modifier.weight(1f), onClick = {
                screenType = 2
            }) {
                Text(
                    text = "screen 2"
                )
            }
        }

        if (screenType == 1) {
            Screen1(modifier = Modifier
                .fillMaxWidth()
                .weight(1f), 1)
        } else {
            Screen1(modifier = Modifier
                .fillMaxWidth()
                .weight(1f), 2)
        }


    }
}

@Composable
fun Screen1(
    modifier: Modifier = Modifier.fillMaxSize(),
    screenType: Int
) {

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        if (screenType == 1) {
            AdKitNativeAdView(
                nativeControllerConfig = NativeControllerConfig(
                    "native_screen_1",
                    "native_screen_1",
                ), adCallBack = object : AdCallBack{
                    override fun onAdFailed(reason: String) {
                        Log.d("ioioio", "onAdFailed: $reason")
                    }

                    override fun onAdShow() {
                    }

                    override fun onAdClick() {
                    }

                }
            )
        }else{
            AdKitNativeAdView(
                nativeControllerConfig = NativeControllerConfig(
                    "native_screen_2",
                    "native_screen_2",
                ), adCallBack = object : AdCallBack{
                    override fun onAdFailed(reason: String) {
                        Log.d("ioioio", "onAdFailed: $reason")
                    }

                    override fun onAdShow() {
                    }

                    override fun onAdClick() {
                    }

                }
            )
        }
    }
}