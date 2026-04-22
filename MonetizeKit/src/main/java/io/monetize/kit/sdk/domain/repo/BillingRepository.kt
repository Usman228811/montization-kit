package io.monetize.kit.sdk.domain.repo

import android.app.Activity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

data class PurchasePriceModel(val price: String = "")

// domain/repository/BillingRepository.kt
interface BillingRepository {
    fun productPriceFlow(): StateFlow<PurchasePriceModel>

    fun initBilling(productId: String, subscriptionListener: SubscriptionListener)
    fun checkProductPurchaseHistory()
    fun purchaseProduct(activity: Activity?,onUserDismissedPaywall :(()->Unit) ?= null)
}