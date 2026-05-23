package io.monetize.kit.sdk.domain.usecase

import android.app.Activity
import android.content.Context
import io.monetize.kit.sdk.data.impl.PlayLifeTimeRepositoryImpl
import io.monetize.kit.sdk.data.impl.RCLifeTimeRepositoryImpl
import io.monetize.kit.sdk.domain.repo.BillingRepository

class PurchaseProductUseCase private constructor(
    private val billingRepositoryPlay: BillingRepository,
    private val billingRepositoryRC: BillingRepository,
) {
    fun purchasePlayProduct(
        activity: Activity,
        productId: String, onUserDismissedPaywall: (() -> Unit)? = null
    ) {
        billingRepositoryPlay.purchaseProduct(activity, productId, onUserDismissedPaywall)
    }
    fun purchaseRcProduct(
        activity: Activity,
        productId: String, onUserDismissedPaywall: (() -> Unit)? = null
    ) {
        billingRepositoryRC.purchaseProduct(activity, productId, onUserDismissedPaywall)
    }

    companion object {
        @Volatile
        private var instance: PurchaseProductUseCase? = null

        fun getInstance(playRepo: BillingRepository, rcRepository: BillingRepository): PurchaseProductUseCase {

            return instance ?: synchronized(this) {

                instance ?: PurchaseProductUseCase(
                    playRepo,
                    rcRepository
                ).also { instance = it }
            }
        }
    }
}


//class PurchaseProductUseCase(private val billingRepository: BillingRepository) {
//    operator fun invoke(activity: Activity?) = billingRepository.purchaseProduct(activity)
//}