package com.test.compose.adslibrary.ui.splash.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.test.compose.adslibrary.ui.splash.state.SplashScreenState

@Composable
fun SplashScreenContent(state: SplashScreenState, showAd :() ->Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        if (state.onAdLoaded) {
            Button(onClick = {
                showAd()
            }) {
                Text(text = "Goto Main")
            }
        }
    }

}