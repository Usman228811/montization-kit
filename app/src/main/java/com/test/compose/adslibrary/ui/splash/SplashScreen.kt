package com.test.compose.adslibrary.ui.splash

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SplashScreen(
    moveToNext: () -> Unit,
) {
    val activity = LocalActivity.current as Activity
    val lifecycleOwner = LocalLifecycleOwner.current

    val context = LocalContext.current
    val factory = remember { SplashScreenViewModelFactory(context) }
    val viewModel: SplashScreenViewModel = viewModel(factory = factory)
    val state by viewModel.state.collectAsState()

//    val launcher = AdSdkInAppUpdateFlowResultLauncher (onFail = {
//        viewModel.initConsent(activity)
//    })


    LaunchedEffect(Unit) {
        viewModel.observeLifecycle(lifecycleOwner)
        viewModel.initConsent(activity)


//        viewModel.checkUpdate(launcher)
    }

    LaunchedEffect(state.moveToMain) {
        if (state.moveToMain) {
            moveToNext()
        }
    }

    LaunchedEffect(state.isAppResumed) {
        if (state.isAppResumed) {
            viewModel.resumeSplashAd(activity)
        }
    }

    LaunchedEffect(state.runSplash) {
        if (state.runSplash) {
            viewModel.showSplashAd(activity)
        }
    }

    BackHandler {}


    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Splash Screen"
        )
    }
}