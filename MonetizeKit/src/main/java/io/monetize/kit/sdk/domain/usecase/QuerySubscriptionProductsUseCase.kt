package io.monetize.kit.sdk.domain.usecase

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import io.monetize.kit.sdk.core.utils.init.AdKit.adKitPref
import io.monetize.kit.sdk.data.impl.SubscriptionRepositoryImpl
import io.monetize.kit.sdk.domain.repo.SubscriptionListener
import io.monetize.kit.sdk.domain.repo.SubscriptionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class Products(
    val products: Map<String, ProductDetails>? = null
)


class QuerySubscriptionProductsUseCase private constructor(
    private val repository: SubscriptionRepository
) {

    companion object {

        @Volatile
        private var instance: QuerySubscriptionProductsUseCase? = null


        fun getInstance(
            context: Context
        ): QuerySubscriptionProductsUseCase {
            val repo = SubscriptionRepositoryImpl.getInstance(context)
            return instance ?: synchronized(this) {
                instance ?: QuerySubscriptionProductsUseCase(
                    repo
                ).also { instance = it }
            }
        }
    }

    private val _products = MutableStateFlow(Products())
    val products = _products.asStateFlow()

    private val _subscribedId = MutableStateFlow("")
    val subscribedId = _subscribedId.asStateFlow()

    private val _historyFetched = MutableStateFlow(false)
    val historyFetched = _historyFetched.asStateFlow()


    operator fun invoke(activity: Activity, productIds: List<String>) {
        repository.setBillingListener(
            activity = activity,
            object : SubscriptionListener {
                override fun onBillingInitialized() {
                    repository.querySubscriptionProducts(activity,productIds)
                }

                override fun onQueryProductSuccess(skuList: Map<String, ProductDetails>) {
                    _products.update {
                        it.copy(
                            products = skuList
                        )
                    }
                    repository.querySubscriptionHistory(activity)

                }

                override fun subscriptionItemNotFound() {

                }

                override fun checkPurchaseStatus(purchase: Purchase) {
                    try {
                        if (purchase.isAcknowledged) {
                            repository.setSubscribed(activity,purchase)
                            onSubscriptionPurchasedFetched()
                        } else {
                            repository.acknowledgedPurchase(activity,purchase)
                        }
                    } catch (_: Exception) {
                    }
                }

                override fun updatePref(subscribedId: String) {
                    try {
                        _subscribedId.value = subscribedId
                        adKitPref.isAppPurchased = subscribedId.isNotEmpty()
                    } catch (_: Exception) {
                    }
                }

                override fun onSubscriptionPurchasedFetched() {
                    _historyFetched.value = historyFetched.value.not()
                }

            }
        )
    }


    fun isSubscriptionUpdateSupported() = repository.isSubscriptionUpdateSupported()
    fun querySubscriptionProducts(activity: Activity) {
        repository.querySubscriptionHistory(activity)
    }

    fun getBillingPrice(productId: String, billingPeriod: String): String {
        products.value.let { products ->
            products.products?.let {

                it[productId]?.subscriptionOfferDetails?.let { skuDetail ->
                    skuDetail[0].pricingPhases.pricingPhaseList.let { priceList ->
                        if (priceList.size == 1) {
                            return priceList[0].formattedPrice
                        }
                    }
                    val list = skuDetail[0].pricingPhases.pricingPhaseList.filter { priceList ->
                        priceList.billingPeriod == billingPeriod
                    }
                    return if (list.isNotEmpty()) {
                        list[0].formattedPrice
                    } else {
                        "Error fetching Price"
                    }
                }
            }
        }
        return "Empty"
    }
}