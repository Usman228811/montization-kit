package io.monetize.kit.sdk.core.di

import io.monetize.kit.sdk.domain.usecase.GetBannerAdUseCase
import io.monetize.kit.sdk.domain.usecase.GetNativeAdUseCase
import io.monetize.kit.sdk.domain.usecase.InitBillingUseCase
import io.monetize.kit.sdk.domain.usecase.PurchaseProductUseCase
import io.monetize.kit.sdk.domain.usecase.PurchaseSubscriptionUseCase
import io.monetize.kit.sdk.domain.usecase.QuerySubscriptionProductsUseCase
import io.monetize.kit.sdk.presentation.viewmodels.BannerAdViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val domainModule = module {
    factory { GetBannerAdUseCase(get()) }
    factory { GetNativeAdUseCase(get()) }


    single { InitBillingUseCase(get()) }
    single { PurchaseProductUseCase(get()) }


    single { QuerySubscriptionProductsUseCase(get(), get()) }
    single { PurchaseSubscriptionUseCase(get()) }
}