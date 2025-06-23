package io.monetize.kit.sdk.domain.usecase

import android.app.Activity
import io.monetize.kit.sdk.domain.repo.BillingRepository

class PurchaseProductUseCase(private val billingRepository: BillingRepository) {
    operator fun invoke(activity: Activity?) = billingRepository.purchaseProduct(activity)
}