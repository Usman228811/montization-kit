package com.test.compose.adslibrary.ui.splash

import android.app.Activity
import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.test.compose.adslibrary.ui.splash.content.SplashScreenContent
import io.monetize.kit.sdk.core.utils.in_app_update.AdKitInAppUpdateFlowResultLauncher

@Composable
fun SplashScreen(
    moveToNext: () -> Unit,
) {

    val factory = remember { SplashScreenViewModelFactory() }
    val splashViewModel: SplashScreenViewModel = viewModel(factory = factory)
    val activity = LocalActivity.current as Activity
    val state by splashViewModel.state.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val launcher = AdKitInAppUpdateFlowResultLauncher(onFail = {
        splashViewModel.initConsent(activity)
    })

    LaunchedEffect(Unit) {
        splashViewModel.loadProducts(
            activity,
            listOf(
                "weekly_subscription2",
                "monthly1_subscription",
                "yearly_subscription"
            )
        )
    }
    LaunchedEffect(Unit) {

        splashViewModel.checkForUpdate(activity, launcher)
        splashViewModel.observeLifecycle(lifecycleOwner)
    }

    LaunchedEffect(key1 = state.runSplash) {
        if (state.runSplash) {
            splashViewModel.initSplashAd(activity)
        }
    }

    LaunchedEffect(key1 = state.moveToMain) {
        if (state.moveToMain) {
            moveToNext()
        }
    }

    LaunchedEffect(state.isAppResumed) {
        if (state.isAppResumed) {
            splashViewModel.resumeSplashAd(activity)
        }
    }

    SplashScreenContent(state = state, showAd = {
        splashViewModel.showSplashOnClick(activity)
    })
}