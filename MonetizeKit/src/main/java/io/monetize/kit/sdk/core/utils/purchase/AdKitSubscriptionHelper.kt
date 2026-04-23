package io.monetize.kit.sdk.core.utils.purchase

import android.app.Activity
import android.app.Application
import io.monetize.kit.sdk.core.utils.init.AdKit.internetController
import io.monetize.kit.sdk.domain.model.OfferTexts
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
            context: Application
        ): AdKitSubscriptionHelper {
            return instance ?: synchronized(this) {
                instance ?: AdKitSubscriptionHelper(
                    QuerySubscriptionProductsUseCase.getInstance(context.applicationContext),
                    PurchaseSubscriptionUseCase.getInstance(context.applicationContext),

                    ).also { instance = it }
            }
        }
    }


    val subscriptionState = queryProducts.ucState

    fun initBilling(
        activity: Activity,
        removeAdsIds: List<String>,
        featureIds: List<String>
    ) {
        queryProducts(activity = activity, removeAdsIds = removeAdsIds, featureIds = featureIds)
    }

    fun isSubscriptionUpdateSupported() = queryProducts.isSubscriptionUpdateSupported()

    fun getBillingPrice(
        productId: String,
    ): OfferTexts {
        return queryProducts.buildOfferTexts(productId)
    }

    private fun isAlreadySubscribed(productId: String): Boolean {
        return subscriptionState.value.purchasesList.contains(productId)
    }


    fun purchase(
        activity: Activity,
        productId: String?,
        isForUpdatePlan: Boolean, onUserDismissedPaywall: (() -> Unit)? = null,

        ) {

        when {
            internetController.isConnected.not() || productId == null -> {

            }

            isAlreadySubscribed(productId) -> {
                purchaseProduct.viewUrl(
                    activity,
                    "https://play.google.com/store/account/subscriptions?sku=${productId}&package=${activity.packageName}"
                )
            }

            subscriptionState.value.purchasesList.isEmpty() -> {

                queryProducts.getProducts()?.let { products ->
                    products[productId]?.let {
                        purchaseProduct(activity, it, onUserDismissedPaywall)
                    }
                }

            }

            !isForUpdatePlan -> {
                queryProducts.getProducts()?.let { products ->
                    products[productId]?.let {
                        purchaseProduct(activity, it, onUserDismissedPaywall)
                    }
                }
            }

            isSubscriptionUpdateSupported() -> {

                queryProducts.getProducts()?.let { products ->
                    products[productId]?.let {
                        purchaseProduct.changeSubscriptionPlan(activity, it)
                    }
                }
            }
        }

    }
}