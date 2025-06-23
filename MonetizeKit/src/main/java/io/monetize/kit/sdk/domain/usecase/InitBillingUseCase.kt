package io.monetize.kit.sdk.domain.usecase

import io.monetize.kit.sdk.domain.repo.BillingRepository

class InitBillingUseCase(private val billingRepository: BillingRepository) {
    operator fun invoke(productId: String) = billingRepository.initBilling(productId)
}