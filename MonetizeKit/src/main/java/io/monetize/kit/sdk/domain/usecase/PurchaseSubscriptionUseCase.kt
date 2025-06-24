package io.monetize.kit.sdk.domain.usecase

import com.android.billingclient.api.ProductDetails
import io.monetize.kit.sdk.domain.repo.SubscriptionRepository


class PurchaseSubscriptionUseCase(
    private val repository: SubscriptionRepository
) {
    operator fun invoke(product: ProductDetails) = repository.purchaseProduct(product)

    fun changeSubscriptionPlan(product: ProductDetails){
        repository.changeSubscriptionPlan(product)
    }
    fun viewUrl( url: String){
        repository.viewUrl(url)
    }

}