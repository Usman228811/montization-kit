package io.monetize.kit.sdk.data.impl

import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import io.monetize.kit.sdk.core.utils.init.AdKit.internetController
import io.monetize.kit.sdk.domain.repo.BillingRepository
import io.monetize.kit.sdk.domain.repo.PurchasePriceModel
import io.monetize.kit.sdk.domain.repo.PlayBillingQueryResult
import io.monetize.kit.sdk.domain.repo.SubscriptionListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlayLifeTimeRepositoryImpl private constructor(
    private val context: Context,
) : BillingRepository {

    private var onUserDismissedPaywall: (() -> Unit)? = null
    private val purchasesList = mutableListOf<String>()

    companion object {
        private const val TAG = "BillingRepositoryImpl"

        @Volatile
        private var instance: PlayLifeTimeRepositoryImpl? = null

        fun getInstance(
            context: Context,
        ): PlayLifeTimeRepositoryImpl {
            return instance ?: synchronized(this) {
                instance ?: PlayLifeTimeRepositoryImpl(context).also { instance = it }
            }
        }
    }

    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val productPriceFlow = MutableStateFlow(PurchasePriceModel())

    private var skuMap: Map<String, ProductDetails> = emptyMap()
    private lateinit var billingClient: BillingClient
    private var productIds: List<String> = emptyList()
    private var isBillingReady: Boolean = false
    private var subscriptionListener: SubscriptionListener? = null

    override fun initBilling(
        removeAdsIds: List<String>,
        featureIds: List<String>,
        subscriptionListener: SubscriptionListener,
    ) {
        this.subscriptionListener = subscriptionListener
        this.productIds = (removeAdsIds + featureIds).distinct()
        coroutineScope.launch {
            if (isBillingReady) {
                queryProductSkuForPurchase()
            } else {
                setupBillingClient()
            }
        }
    }

    override fun productPriceFlow(): StateFlow<PurchasePriceModel> = productPriceFlow.asStateFlow()

    private val isBillingClientInitialized: Boolean
        get() = ::billingClient.isInitialized

    private fun setupBillingClient() {
        if (!isBillingClientInitialized) {
            billingClient = BillingClient.newBuilder(context)
                .enablePendingPurchases(
                    PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
                )
                .setListener { result, purchases ->
                    when (result.responseCode) {
                        BillingClient.BillingResponseCode.OK -> handlePurchases(purchases)
                        BillingClient.BillingResponseCode.USER_CANCELED -> {
                            onUserDismissedPaywall?.invoke()
                            Log.d(TAG, "One-Time-Purchase: User dismissed the paywall")
                        }
                    }
                }
                .build()
        }

        if (isBillingReady || billingClient.isReady) {
            return
        }

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                isBillingReady = false
            }

            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    isBillingReady = true
                    queryProductSkuForPurchase()
                }
            }
        })
    }

    private fun buildProductList(productIds: List<String>): List<QueryProductDetailsParams.Product> {
        return productIds.map { productId ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        }
    }

    private fun queryProductSkuForPurchase() {
        try {
            if (!isBillingClientReady()) return
            if (productIds.isEmpty()) {
                subscriptionListener?.subscriptionItemNotFound()
                return
            }

            val queryParams = QueryProductDetailsParams.newBuilder()
                .setProductList(buildProductList(productIds))
                .build()

            billingClient.queryProductDetailsAsync(queryParams) { result, queryProductDetailsResult ->
                if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                    subscriptionListener?.subscriptionItemNotFound()
                    return@queryProductDetailsAsync
                }
                val productList = queryProductDetailsResult.productDetailsList
                if (productList.isEmpty()) {
                    subscriptionListener?.subscriptionItemNotFound()
                    return@queryProductDetailsAsync
                }
                skuMap = productList.toProductDetailsMap()
                subscriptionListener?.onQueryProductSuccess(
                    PlayBillingQueryResult(
                        skuList = skuMap,
                        productList = productList
                    )
                )
            }
        } catch (error: LinkageError) {
            subscriptionListener?.subscriptionItemNotFound()
        } catch (e: Exception) {
            subscriptionListener?.subscriptionItemNotFound()
        }
    }

    override fun purchaseProduct(
        activity: Activity,
        productId: String,
        onUserDismissedPaywall: (() -> Unit)?,
    ) {
        try {
            this.onUserDismissedPaywall = onUserDismissedPaywall
            if (!internetController.isConnected) {
                context.showNoInternet(activity)
                return
            }
            if (!isBillingClientReady()) {
                context.showTryAgain(activity)
                return
            }

            val details = skuMap[productId]
            if (details == null) {
                context.showTryAgain(activity)
                return
            }

            val billingParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(
                    listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(details)
                            .build()
                    )
                )
                .build()
            val billingResult = billingClient.launchBillingFlow(activity, billingParams)
            if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                context.showTryAgain(activity)
            }
        } catch (_: IntentSender.SendIntentException) {
            context.showTryAgain(activity)
        } catch (_: Exception) {
            context.showTryAgain(activity)
        }
    }

    override fun checkProductPurchaseHistory() {
        if (!isBillingClientReady()) return

        purchasesList.clear()
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        ) { billingResult, purchases ->
            if (billingResult.responseCode != BillingClient.BillingResponseCode.OK || purchases.isNullOrEmpty()) {
                subscriptionListener.dispatchPurchases(emptyList())
                return@queryPurchasesAsync
            }
            handlePurchases(purchases)
            if (purchasesList.isEmpty()) {
                subscriptionListener.dispatchPurchases(emptyList())
            }
        }
    }

    private fun handlePurchases(purchases: List<Purchase>?) {
        if (purchases.isNullOrEmpty()) {
            return
        }
        for (purchase in purchases) {
            if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) {
                continue
            }
            if (purchase.isAcknowledged) {
                notifyPurchase(purchase)
            } else {
                acknowledgePurchase(purchase)
            }
        }
    }

    private fun notifyPurchase(purchase: Purchase) {
        val updatedPurchases = purchasesList.addDistinct(purchase.primaryProductId())
        subscriptionListener.dispatchPurchases(updatedPurchases)
    }

    private fun acknowledgePurchase(purchase: Purchase) {
        if (!isBillingClientReady()) return

        val acknowledgeParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        coroutineScope.launch {
            billingClient.acknowledgePurchase(acknowledgeParams) { result ->
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    notifyPurchase(purchase)
                }
            }
        }
    }

    private fun isBillingClientReady(): Boolean =
        isBillingClientInitialized && billingClient.isReady
}
