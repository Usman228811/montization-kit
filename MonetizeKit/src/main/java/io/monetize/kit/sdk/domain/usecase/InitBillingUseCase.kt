package io.monetize.kit.sdk.domain.usecase

import io.monetize.kit.sdk.domain.repo.BillingRepository


class InitBillingUseCase private constructor(
    private val billingRepository: BillingRepository
) {
    operator fun invoke(productId: String) = billingRepository.initBilling(productId)

    companion object {
        @Volatile
        private var instance: InitBillingUseCase? = null


        fun getInstance(billingRepository: BillingRepository): InitBillingUseCase {

            return instance ?: synchronized(this) {
                instance ?: InitBillingUseCase(billingRepository).also { instance = it }
            }
        }

    }
}

//class InitBillingUseCase(private val billingRepository: BillingRepository) {
//    operator fun invoke(productId: String) = billingRepository.initBilling(productId)
//}