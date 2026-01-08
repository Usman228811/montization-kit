package io.monetize.kit.sdk.core.utils.purchase

import android.app.Activity
import android.content.Context
import io.monetize.kit.sdk.core.utils.init.AdKit.internetController
import io.monetize.kit.sdk.domain.usecase.PriceModel
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
    val isAppSubscribed = queryProducts.isAppSubscribed

    fun initBilling(activity: Activity, productIds: List<String>) {
        queryProducts(activity, productIds)
    }

    fun querySubscriptionProducts(activity: Activity) {
        queryProducts.querySubscriptionProducts(activity)
    }

    fun isSubscriptionUpdateSupported() = queryProducts.isSubscriptionUpdateSupported()

    fun getBillingPrice(
        productId: String,
        offerId: String,
        billingPeriod: String
    ): PriceModel {
        return queryProducts.getBillingPrice(productId, offerId, billingPeriod)
    }

    fun purchase(
        activity: Activity,
        productId: String?
    ) {

        when {
            internetController.isConnected.not() || productId == null -> {

            }

            subscribedId.value == productId -> {
                purchaseProduct.viewUrl(
                    activity,
                    "https://play.google.com/store/account/subscriptions?sku=${productId}&package=${activity.packageName}"
                )
            }

            subscribedId.value == "" -> {

                subscriptionProducts.value.products?.let { products ->
                    products[productId]?.let {
                        purchaseProduct(activity, it)
                    }
                }

            }

            isSubscriptionUpdateSupported() -> {

                subscriptionProducts.value.products?.let { products ->
                    products[productId]?.let {
                        purchaseProduct.changeSubscriptionPlan(activity, it)
                    }
                }
            }
        }

    }
}