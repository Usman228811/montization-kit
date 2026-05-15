package io.monetize.kit.sdk.core.utils.purchase

import android.app.Activity
import android.app.Application
import io.monetize.kit.sdk.core.utils.init.AdKit.init
import io.monetize.kit.sdk.core.utils.init.AdKit.internetController
import io.monetize.kit.sdk.domain.model.OfferTexts
import io.monetize.kit.sdk.domain.usecase.OneTimePurchaseState
import io.monetize.kit.sdk.domain.usecase.PurchaseSubscriptionUseCase
import io.monetize.kit.sdk.domain.usecase.QuerySubscriptionProductsRCUseCase
import io.monetize.kit.sdk.domain.usecase.QuerySubscriptionProductsUseCase
import io.monetize.kit.sdk.domain.usecase.SubscriptionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

class AdKitSubscriptionHelper private constructor(
    private val queryProducts: QuerySubscriptionProductsUseCase,
    private val queryProductsRc: QuerySubscriptionProductsRCUseCase,
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
                    QuerySubscriptionProductsRCUseCase.getInstance(context.applicationContext),
                    PurchaseSubscriptionUseCase.getInstance(context.applicationContext),

                    ).also { instance = it }
            }
        }
    }


    private val _isRevenueCat = MutableStateFlow(false)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)


    fun initBilling(
        activity: Activity,
        removeAdsIds: List<String>,
        featureIds: List<String>,
        isForRevenueCat: Boolean
    ) {
        _isRevenueCat.value = isForRevenueCat
        if (isForRevenueCat) {
            queryProductsRc(
                activity = activity,
                removeAdsIds = removeAdsIds,
                featureIds = featureIds
            )

        } else {
            queryProducts(activity = activity, removeAdsIds = removeAdsIds, featureIds = featureIds)

        }
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    val subscriptionState: StateFlow<SubscriptionState> = _isRevenueCat.flatMapLatest { isRc ->
        if (isRc) {
            queryProductsRc.ucState
        } else {
            queryProducts.ucState

        }
    }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = SubscriptionState()
        )


    fun isSubscriptionUpdateSupported() = if (_isRevenueCat.value) {
        queryProductsRc.isSubscriptionUpdateSupported()
    } else {
        queryProducts.isSubscriptionUpdateSupported()
    }

    fun getBillingPrice(
        productId: String,
    ): OfferTexts {
        return if (_isRevenueCat.value) {
            queryProductsRc.buildOfferTexts(productId)
        }else{
            queryProducts.buildOfferTexts(productId)
        }
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

                if (_isRevenueCat.value) {
                    queryProductsRc.getProducts()?.let { products ->
                        products[productId]?.let {
                            purchaseProduct.purchaseRcProduct(activity, it, onUserDismissedPaywall)
                        }
                    }
                }else{
                    queryProducts.getProducts()?.let { products ->
                        products[productId]?.let {
                            purchaseProduct.purchasePlayProduct(activity, it, onUserDismissedPaywall)
                        }
                    }
                }


            }

            !isForUpdatePlan -> {
                if (_isRevenueCat.value) {
                    queryProductsRc.getProducts()?.let { products ->
                        products[productId]?.let {
                            purchaseProduct.purchaseRcProduct(activity, it, onUserDismissedPaywall)
                        }
                    }
                }else{
                    queryProducts.getProducts()?.let { products ->
                        products[productId]?.let {
                            purchaseProduct.purchasePlayProduct(activity, it, onUserDismissedPaywall)
                        }
                    }
                }
            }

            isSubscriptionUpdateSupported() -> {

//                queryProducts.getProducts()?.let { products ->
//                    products[productId]?.let {
//                        purchaseProduct.changeSubscriptionPlan(activity, it)
//                    }
//                }
            }
        }

    }
}