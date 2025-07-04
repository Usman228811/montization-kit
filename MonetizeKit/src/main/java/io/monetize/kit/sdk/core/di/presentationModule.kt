package io.monetize.kit.sdk.core.di

import io.monetize.kit.sdk.presentation.viewmodels.BannerAdViewModel
import io.monetize.kit.sdk.presentation.viewmodels.NativeAdViewModel
import io.monetize.kit.sdk.presentation.viewmodels.NativeAdViewModelDialog
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val presentationModule = module {
    viewModelOf(::NativeAdViewModel)
    viewModelOf(::BannerAdViewModel)
    viewModelOf(::NativeAdViewModelDialog)
}