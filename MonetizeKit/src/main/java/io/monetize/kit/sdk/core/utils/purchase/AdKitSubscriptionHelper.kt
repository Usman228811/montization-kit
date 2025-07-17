package io.monetize.kit.sdk.core.utils.purchase

import android.app.Activity
import android.content.Context
import io.monetize.kit.sdk.core.utils.AdKitInternetController
import io.monetize.kit.sdk.core.utils.init.AdKit.internetController
import io.monetize.kit.sdk.domain.usecase.PurchaseSubscriptionUseCase
import io.monetize.kit.sdk.domain.usecase.QuerySubscriptionProductsUseCase

class AdKitSubscriptionHelper private constructor(
    private val queryProducts: QuerySubscriptionProductsUseCase,
    private val purchaseProduct: PurchaseSubscriptionUseCase
) {

    companion object {
        @Volatile
        private var instance: AdKitSubscriptionHelper? = null


        internal fun getInstance(
            context: Context
        ): AdKitSubscriptionHelper {
            return instance ?: synchronized(this) {
                instance ?: AdKitSubscriptionHelper(
                    QuerySubscriptionProductsUseCase.getInstance(context),
                    PurchaseSubscriptionUseCase.getInstance(context),

                    ).also { instance = it }
            }
        }
    }


    val subscriptionProducts = queryProducts.products
    val historyFetched = queryProducts.historyFetched
    val subscribedId = queryProducts.subscribedId

    fun loadProducts(activity: Activity, productIds: List<String>) {
        queryProducts(activity, productIds)
    }

    fun querySubscriptionProducts() {
        queryProducts.querySubscriptionProducts()
    }

    fun isSubscriptionUpdateSupported() = queryProducts.isSubscriptionUpdateSupported()

    fun getBillingPrice(productId: String, billingPeriod: String): String {
        return queryProducts.getBillingPrice(productId, billingPeriod)
    }

    fun purchase(
        activity: Activity,
        productId: String?
    ) {

        when {
            internetController.isConnected.not() || productId == null -> {

            }

            subscribedId.value == productId -> {
                purchaseProduct.viewUrl(activity,"https://play.google.com/store/account/subscriptions?sku=${productId}&package=${activity.packageName}")
            }

            subscribedId.value == "" -> {
                subscriptionProducts.value?.get(productId)?.let {
                    purchaseProduct(it)
                }
            }

            isSubscriptionUpdateSupported() -> {
                subscriptionProducts.value?.get(productId)?.let {
                    purchaseProduct.changeSubscriptionPlan(it)
                }
            }
        }

    }
}