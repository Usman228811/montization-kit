package io.monetize.kit.sdk.core.di

import com.google.firebase.messaging.threads.PoolableExecutors.factory
import io.monetize.kit.sdk.domain.usecase.GetNativeAdUseCase
import io.monetize.kit.sdk.domain.usecase.InitBillingUseCase
import io.monetize.kit.sdk.domain.usecase.PurchaseProductUseCase
import io.monetize.kit.sdk.domain.usecase.PurchaseSubscriptionUseCase
import io.monetize.kit.sdk.domain.usecase.QuerySubscriptionProductsUseCase

//val domainModule = module {
//    factory { GetBannerAdUseCase(get()) }
//    factory { GetNativeAdUseCase(get()) }


//    single { InitBillingUseCase(get()) }
//    single { PurchaseProductUseCase(get()) }


//    single { QuerySubscriptionProductsUseCase(get(), get()) }
//    single { PurchaseSubscriptionUseCase(get()) }
//}