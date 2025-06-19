package com.test.compose.adslibrary.di

import com.test.compose.adslibrary.ui.splash.SplashScreenViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val MainModule = module {
    viewModel { SplashScreenViewModel(get()) }
}