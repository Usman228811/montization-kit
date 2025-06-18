package io.monetize.kit.sdk.core.di

import io.monetize.kit.sdk.ads.banner.BaseSingleBannerActivity
import io.monetize.kit.sdk.ads.collapsable.BaseCollapsableBannerActivity
import io.monetize.kit.sdk.data.impl.GetBannerAdRepoImpl
import io.monetize.kit.sdk.data.impl.GetNativeAdRepoImpl
import io.monetize.kit.sdk.domain.repo.GetBannerAdRepo
import io.monetize.kit.sdk.domain.repo.GetNativeAdRepo
import org.koin.dsl.module

val dataModule = module {
    factory { BaseCollapsableBannerActivity(get(), get(), get()) }
    factory { BaseSingleBannerActivity(get(), get(), get()) }
    factory<GetBannerAdRepo> { GetBannerAdRepoImpl(get(), get()) }
    factory<GetNativeAdRepo> { GetNativeAdRepoImpl(get(), get(), get()) }
}