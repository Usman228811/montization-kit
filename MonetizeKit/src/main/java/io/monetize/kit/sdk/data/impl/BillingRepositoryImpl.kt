package io.monetize.kit.sdk.data.impl

import android.app.Activity
import android.content.Context
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
import io.monetize.kit.sdk.R
import io.monetize.kit.sdk.core.utils.AdKitInternetController
import io.monetize.kit.sdk.core.utils.AdKitPref
import io.monetize.kit.sdk.core.utils.showToast
import io.monetize.kit.sdk.domain.repo.BillingRepository
import io.monetize.kit.sdk.domain.repo.PurchasePriceModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BillingRepositoryImpl(
    private val context: Context,
    private val internetController: AdKitInternetController,
    private val myPref: AdKitPref,
) : BillingRepository {

    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _productPriceFlow = MutableStateFlow(PurchasePriceModel())


    private val _appPurchased = Channel<Boolean>()

    private var purchaseSku: ProductDetails? = null
    private lateinit var billingClient: BillingClient
    private var productId = ""

    override fun initBilling(productId: String) {
        this.productId = productId
        coroutineScope.launch { setupBillingClient() }
    }

    override fun productPriceFlow(): StateFlow<PurchasePriceModel> {
        return _productPriceFlow.asStateFlow()
    }

    override fun appPurchased(): Flow<Boolean> {
        return _appPurchased.receiveAsFlow()
    }

    private val isBillingClientInitialized: Boolean
        get() = ::billingClient.isInitialized


    private fun setupBillingClient() {
        if (!isBillingClientInitialized) {
            billingClient = BillingClient.newBuilder(context)
                .enablePendingPurchases(
                    PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
                )
                .setListener { result, purchases ->
                    if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                        isProductPurchased(purchases)
                    }
                }
                .build()
        }

        if (!billingClient.isReady) {
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingServiceDisconnected() {
//                    "Service Disconnected".logIt(BILLING_TAG)
                }

                override fun onBillingSetupFinished(result: BillingResult) {
                    if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                        checkProductPurchaseHistory()
                    } else {
//                        "Setup Failed: ${result.responseCode}".logIt(BILLING_TAG)
                    }
                }
            })
        }
    }

    private fun queryProductSkuForPurchase() {

        if (!internetController.isConnected || !isBillingClientReady()) return

        val queryParams = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(productId)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                )
            )
            .build()

        billingClient.queryProductDetailsAsync(queryParams) { result, productList ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK && productList.isNotEmpty()) {
                purchaseSku = productList.find { it.productId == productId }
                _productPriceFlow.update {
                    it.copy(price = purchaseSku?.oneTimePurchaseOfferDetails?.formattedPrice ?: "")
                }
            } else {
//                "Product Query Failed: ${result.responseCode}".logIt(BILLING_TAG)
            }
        }
    }

    override fun purchaseProduct(activity: Activity?) {
        if (activity == null) return
        if (!internetController.isConnected) {
            context.showToast(activity.getString(R.string.no_internet))
            return
        }
        if (!isBillingClientReady() || purchaseSku == null) {
            context.showToast(activity.getString(R.string.try_again))
            return
        }

        val billingParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(purchaseSku!!)
                        .build()
                )
            )
            .build()

        billingClient.launchBillingFlow(activity, billingParams)
    }

    private fun checkProductPurchaseHistory() {
        if (!internetController.isConnected || !isBillingClientReady()) return

        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP)
                .build()
        ) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                if (!isProductPurchased(purchases)) {
                    updatePurchaseStatus(false)
                    queryProductSkuForPurchase()
                }
            } else {
//                "Query Purchases Failed: ${result.responseCode}".logIt(BILLING_TAG)
            }
        }
    }

    private fun updatePurchaseStatus(isPurchased: Boolean) {
        myPref.isAppPurchased = isPurchased
        if (isPurchased) {
//            context.userAnalytics("Premium_buy_successful")
            coroutineScope.launch { _appPurchased.send(true) }
        }
    }


    private fun isProductPurchased(list: List<Purchase>?): Boolean {
        val purchase = list?.toList()?.find { it.purchaseState == Purchase.PurchaseState.PURCHASED }
            ?: return false
        return if (purchase.products.contains(productId)) {
            if (purchase.isAcknowledged) updatePurchaseStatus(true) else acknowledgePurchase(
                purchase
            )
            true
        } else false
    }

    private fun acknowledgePurchase(purchase: Purchase) {
        if (!isBillingClientReady()) return

        val acknowledgeParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        coroutineScope.launch {
            billingClient.acknowledgePurchase(acknowledgeParams) { result ->
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    updatePurchaseStatus(true)
//                    "Acknowledgment Successful".logIt(BILLING_TAG)
                } else {
//                    "Acknowledgment Failed: ${result.responseCode}".logIt(BILLING_TAG)
                }
            }
        }
    }

    private fun isBillingClientReady(): Boolean =
        isBillingClientInitialized && billingClient.isReady
}
