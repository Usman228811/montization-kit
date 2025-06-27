package io.monetize.kit.sdk.core.utils.purchase

import android.app.Activity
import io.monetize.kit.sdk.domain.repo.BillingRepository
import io.monetize.kit.sdk.domain.usecase.InitBillingUseCase
import io.monetize.kit.sdk.domain.usecase.PurchaseProductUseCase

class AdKitPurchaseHelper(
    private val init: InitBillingUseCase,
    private val purchase: PurchaseProductUseCase,
    private val billingRepository: BillingRepository
) {

    val productPriceFlow = billingRepository.productPriceFlow()
    val appPurchased = billingRepository.appPurchased()

    fun initBilling(productId: String) = init(productId)

    fun purchaseProduct(activity: Activity?) = purchase(activity)
}