package io.monetize.kit.sdk.domain.usecase

import android.app.Activity
import android.content.Context
import io.monetize.kit.sdk.core.utils.AdKitInternetController
import io.monetize.kit.sdk.core.utils.AdKitPref
import io.monetize.kit.sdk.data.impl.BillingRepositoryImpl
import io.monetize.kit.sdk.domain.repo.BillingRepository

class PurchaseProductUseCase private constructor(
    private val billingRepository: BillingRepository
) {
    operator fun invoke(activity: Activity?) = billingRepository.purchaseProduct(activity)

    companion object {
        @Volatile
        private var instance: PurchaseProductUseCase? = null

        fun getInstance(billingRepository: BillingRepository): PurchaseProductUseCase {

            return instance ?: synchronized(this) {
                instance ?: PurchaseProductUseCase(billingRepository).also { instance = it }
            }
        }
    }
}


//class PurchaseProductUseCase(private val billingRepository: BillingRepository) {
//    operator fun invoke(activity: Activity?) = billingRepository.purchaseProduct(activity)
//}