package io.monetize.kit.sdk.data.impl

import android.app.Activity
import android.content.Context
import android.content.IntentSender
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.PurchaseParams
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.getCustomerInfoWith
import com.revenuecat.purchases.getOfferingsWith
import com.revenuecat.purchases.purchaseWith
import io.monetize.kit.sdk.core.utils.init.AdKit
import io.monetize.kit.sdk.core.utils.init.AdKit.internetController
import io.monetize.kit.sdk.domain.repo.BillingRepository
import io.monetize.kit.sdk.domain.repo.PurchasePriceModel
import io.monetize.kit.sdk.domain.repo.RevenueCatBillingQueryResult
import io.monetize.kit.sdk.domain.repo.SubscriptionListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class RCLifeTimeRepositoryImpl private constructor(
    private val context: Context,
) : BillingRepository {

    private var onUserDismissedPaywall: (() -> Unit)? = null
    private val purchasesList = mutableListOf<String>()

    companion object {
        @Volatile
        private var instance: RCLifeTimeRepositoryImpl? = null

        fun getInstance(
            context: Context,
        ): RCLifeTimeRepositoryImpl {
            return instance ?: synchronized(this) {
                instance ?: RCLifeTimeRepositoryImpl(context).also { instance = it }
            }
        }
    }

    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val productPriceFlow = MutableStateFlow(PurchasePriceModel())

    private var skuMap: Map<String, Package> = emptyMap()
    private var productIds: List<String> = emptyList()
    private var subscriptionListener: SubscriptionListener? = null

    override fun initBilling(
        removeAdsIds: List<String>,
        featureIds: List<String>,
        subscriptionListener: SubscriptionListener,
    ) {
        this.subscriptionListener = subscriptionListener
        productIds = (removeAdsIds + featureIds).distinct()
        coroutineScope.launch {
            try {
                queryPackageDetails(productIds)
            } catch (_: Exception) {
                subscriptionListener.subscriptionItemNotFound()
            }
        }
    }

    private suspend fun queryPackageDetails(packageIds: List<String>) =
        suspendCancellableCoroutine { continuation ->
            Purchases.sharedInstance.getOfferingsWith(
                onSuccess = { offerings ->
                    val packages = offerings[AdKit.getRevenueCatOfferingKey()]
                        ?.availablePackages
                        ?.filter { it.identifier in packageIds }
                        .orEmpty()

                    skuMap = packages.toPackageMap()
                    continuation.resume(
                        subscriptionListener?.onQueryProductSuccess(
                            RevenueCatBillingQueryResult(
                                skuList = skuMap,
                                productList = packages
                            )
                        )
                    )
                },
                onError = { error ->
                    continuation.cancel(
                        Exception("queryPackageDetails: Failed to query products: msg= ${error.message}")
                    )
                    subscriptionListener?.subscriptionItemNotFound()
                }
            )
        }

    override fun productPriceFlow(): StateFlow<PurchasePriceModel> = productPriceFlow.asStateFlow()

    override fun purchaseProduct(
        activity: Activity,
        productId: String,
        onUserDismissedPaywall: (() -> Unit)?,
    ) {
        try {
            this.onUserDismissedPaywall = onUserDismissedPaywall
            if (internetController.isConnected.not()) {
                context.showNoInternet(activity)
                return
            }
            if (!activity.canLaunchBillingFlow()) {
                context.showTryAgain(activity)
                return
            }

            val details = skuMap[productId]
            if (details == null) {
                context.showTryAgain(activity)
                return
            }

            val params = PurchaseParams.Builder(activity, details).build()
            Purchases.sharedInstance.purchaseWith(
                purchaseParams = params,
                onSuccess = { _, info ->
                    val updatedPurchases = purchasesList.replaceWithDistinct(info.activeEntitlementIds())
                    if (updatedPurchases.isNotEmpty()) {
                        activity.runOnUiThread {
                            subscriptionListener.dispatchPurchases(updatedPurchases)
                        }
                    } else {
                        context.showTryAgain(activity)
                    }
                },
                onError = { _, userCancelled ->
                    if (userCancelled) {
                        onUserDismissedPaywall?.invoke()
                    } else {
                        context.showTryAgain(activity)
                    }
                }
            )
        } catch (_: IntentSender.SendIntentException) {
            context.showTryAgain(activity)
        } catch (_: Exception) {
            context.showTryAgain(activity)
        }
    }

    override fun checkProductPurchaseHistory() {
        Purchases.sharedInstance.getCustomerInfoWith(
            onSuccess = { info ->
                subscriptionListener.dispatchPurchases(
                    purchasesList.replaceWithDistinct(info.activeEntitlementIds())
                )
            },
            onError = { _ ->
                purchasesList.clear()
                subscriptionListener.dispatchPurchases(emptyList())
            }
        )
    }
}
