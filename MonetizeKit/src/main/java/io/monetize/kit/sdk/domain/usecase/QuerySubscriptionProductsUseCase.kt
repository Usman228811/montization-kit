package io.monetize.kit.sdk.domain.usecase

import android.app.Activity
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import io.monetize.kit.sdk.core.utils.AdSdkPref
import io.monetize.kit.sdk.domain.repo.SubscriptionListener
import io.monetize.kit.sdk.domain.repo.SubscriptionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow


class QuerySubscriptionProductsUseCase(

    private val pref: AdSdkPref,
    private val repository: SubscriptionRepository
) {

    private val _products = MutableStateFlow<Map<String, ProductDetails>?>(null)
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
                    repository.querySubscriptionProducts(productIds)
                }

                override fun onQueryProductSuccess(skuList: Map<String, ProductDetails>) {
                    _products.value = skuList
                    repository.querySubscriptionHistory()

                }

                override fun subscriptionItemNotFound() {

                }

                override fun checkPurchaseStatus(purchase: Purchase) {
                    try {
                        if (purchase.isAcknowledged) {
                            repository.setSubscribed(purchase)
                            onSubscriptionPurchasedFetched()
                        } else {
                            repository.acknowledgedPurchase(purchase)
                        }
                    } catch (_: Exception) {
                    }
                }

                override fun updatePref(subscribedId: String) {
                    try {
                        _subscribedId.value = subscribedId
                        pref.isAppPurchased = subscribedId.isNotEmpty()
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
    fun querySubscriptionProducts() {
        repository.querySubscriptionHistory()
    }

    fun getBillingPrice(productId: String, billingPeriod: String): String {
        products.value?.let {
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
        return "Empty"
    }
}