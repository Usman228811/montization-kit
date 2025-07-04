package com.test.compose.adslibrary.di

import com.test.compose.adslibrary.ui.settings.SubscriptionViewModel
import com.test.compose.adslibrary.ui.splash.SplashScreenViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val MainModule = module {
    viewModelOf(::SplashScreenViewModel)
    viewModelOf(::SubscriptionViewModel)
}