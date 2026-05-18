package com.test.compose.adslibrary.ui.splash.state


data class SplashScreenState(
    val isConsentManager: Boolean = false,
    val initializeSplash: Boolean = false,
    val fireBaseFetch: Boolean = false,
    val showRestartDialog: Boolean = false,
    val isAppResumed: Boolean = false,
    val moveToMain: Boolean = false,
    val isPurchased: Boolean = false,
    val runSplash: Boolean = false,
    val onAdLoaded: Boolean = false,
    val progress: Int = 0,
    val loadAndShow : Boolean = true
)
