package com.test.compose.adslibrary.ui.splash.events

import android.app.Activity
import android.content.Context
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest

sealed class SplashScreenEvents {
    data class CheckUpdate(
        val context: Context,
        val launcher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>
    ) : SplashScreenEvents()

    data class ShowSplashAd(val mContext: Activity) : SplashScreenEvents()
    data class AppResumed(val mContext: Activity) : SplashScreenEvents()
    data class CheckConsent(val mContext: Activity): SplashScreenEvents()
    data object FireBaseFetch: SplashScreenEvents()
    data object RunSplash: SplashScreenEvents()
}