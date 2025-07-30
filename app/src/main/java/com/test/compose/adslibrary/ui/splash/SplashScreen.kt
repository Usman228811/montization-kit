package com.test.compose.adslibrary.ui.splash

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.test.compose.adslibrary.ui.splash.content.SplashScreenContent
import com.test.compose.adslibrary.ui.splash.events.SplashOneTimeEventEvents
import com.test.compose.adslibrary.ui.splash.events.SplashScreenEvents
import io.monetize.kit.sdk.core.utils.in_app_update.AdKitInAppUpdateFlowResultLauncher

@Composable
fun SplashScreen(
    moveToNext: () -> Unit,
) {
    val activity = LocalActivity.current as Activity
    val lifecycleOwner = LocalLifecycleOwner.current

    val context = LocalContext.current
    val factory = remember { SplashScreenViewModelFactory() }
    val viewModel: SplashScreenViewModel = viewModel(factory = factory)
    val state by viewModel.state.collectAsState()

    val launcher = AdKitInAppUpdateFlowResultLauncher(onFail = {
        viewModel.onEvent(SplashScreenEvents.CheckConsent(activity))
    })

    LaunchedEffect(Unit) {
        viewModel.oneTimeEvent.collect { event ->
            when (event) {
                SplashOneTimeEventEvents.MoveToMain -> {
                    moveToNext()
                }
            }
        }
    }


    LaunchedEffect(Unit) {
        viewModel.observeLifecycle(lifecycleOwner)
        viewModel.onEvent(SplashScreenEvents.CheckUpdate(context, launcher))
    }


    LaunchedEffect(state.isAppResumed) {
        if (state.isAppResumed) {
            viewModel.onEvent(SplashScreenEvents.AppResumed(activity))
        }
    }

    LaunchedEffect(state.runSplash) {
        if (state.runSplash) {
            viewModel.onEvent(SplashScreenEvents.ShowSplashAd(activity))
        }
    }


    BackHandler {}


    SplashScreenContent(state, showAd = {
        viewModel.showSplashInter(activity)
    })
}