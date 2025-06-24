package io.monetize.kit.sdk.core.di

import io.monetize.kit.sdk.ads.banner.BaseSingleBannerActivity
import io.monetize.kit.sdk.ads.collapsable.BaseCollapsableBannerActivity
import io.monetize.kit.sdk.data.impl.BillingRepositoryImpl
import io.monetize.kit.sdk.data.impl.GetBannerAdRepoImpl
import io.monetize.kit.sdk.data.impl.GetNativeAdRepoImpl
import io.monetize.kit.sdk.data.impl.SubscriptionRepositoryImpl
import io.monetize.kit.sdk.domain.repo.BillingRepository
import io.monetize.kit.sdk.domain.repo.GetBannerAdRepo
import io.monetize.kit.sdk.domain.repo.GetNativeAdRepo
import io.monetize.kit.sdk.domain.repo.SubscriptionRepository
import io.monetize.kit.sdk.domain.usecase.PurchaseSubscriptionUseCase
import io.monetize.kit.sdk.domain.usecase.QuerySubscriptionProductsUseCase
import org.koin.dsl.module

val dataModule = module {
    factory { BaseCollapsableBannerActivity(get(), get(), get()) }
    factory { BaseSingleBannerActivity(get(), get(), get()) }
    factory<GetBannerAdRepo> { GetBannerAdRepoImpl(get(), get()) }
    factory<GetNativeAdRepo> { GetNativeAdRepoImpl(get(), get(), get()) }

    single<BillingRepository> { BillingRepositoryImpl(get(), get(), get()) }

    single<SubscriptionRepository> { SubscriptionRepositoryImpl(get()) }

}