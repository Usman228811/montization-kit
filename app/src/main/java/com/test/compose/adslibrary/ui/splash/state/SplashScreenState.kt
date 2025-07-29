package com.test.compose.adslibrary.ui.splash.state

import io.monetize.kit.sdk.core.utils.in_app_update.UpdateState

data class SplashScreenState(
    val runSplash: Boolean = false,
    val isAppResumed: Boolean = false,
    val isConsentManager: Boolean = false,
    val progress: Int = 0,
    val updateState: UpdateState = UpdateState.Idle,
)
