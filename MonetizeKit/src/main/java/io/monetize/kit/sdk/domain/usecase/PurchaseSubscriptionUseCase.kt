package io.monetize.kit.sdk.domain.usecase

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.ProductDetails
import io.monetize.kit.sdk.data.impl.SubscriptionRepositoryImpl
import io.monetize.kit.sdk.domain.repo.SubscriptionRepository


class PurchaseSubscriptionUseCase private constructor(
    private val repository: SubscriptionRepository
) {

    companion object {
        @Volatile
        private var instance: PurchaseSubscriptionUseCase? = null


        fun getInstance(
            context: Context
        ): PurchaseSubscriptionUseCase {
            val repo = SubscriptionRepositoryImpl.getInstance(context)
            return instance ?: synchronized(this) {
                instance ?: PurchaseSubscriptionUseCase(
                    repo
                ).also { instance = it }
            }
        }
    }


    operator fun invoke(product: ProductDetails) = repository.purchaseProduct(product)

    fun changeSubscriptionPlan(product: ProductDetails) {
        repository.changeSubscriptionPlan(product)
    }

    fun viewUrl(activity: Activity, url: String) {
        repository.viewUrl(activity, url)
    }

}