package io.monetize.kit.sdk.data.impl

import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.os.Build
import android.text.TextUtils
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.EntitlementInfo
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.PurchaseParams
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.getCustomerInfoWith
import com.revenuecat.purchases.getOfferingsWith
import com.revenuecat.purchases.purchaseWith
import io.monetize.kit.sdk.R
import io.monetize.kit.sdk.core.utils.init.AdKit.internetController
import io.monetize.kit.sdk.core.utils.showToast
import io.monetize.kit.sdk.domain.repo.BillingRepository
import io.monetize.kit.sdk.domain.repo.PurchasePriceModel
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
    mContext: Context,
) : BillingRepository {


    private val OFFERINGS_ID = "default_offerings"
    private val context = mContext

    private var onUserDismissedPaywall: (() -> Unit)? = null

    private val purchasesList = mutableListOf<String>()

    companion object {

        const val TAG = "BillingRepositoryImpl"

        @Volatile
        private var instance: RCLifeTimeRepositoryImpl? = null


        fun getInstance(
            context: Context,
        ): RCLifeTimeRepositoryImpl {
            return instance ?: synchronized(this) {
                instance ?: RCLifeTimeRepositoryImpl(
                    context,
                ).also { instance = it }
            }
        }
    }

    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _productPriceFlow = MutableStateFlow(PurchasePriceModel())


    private var skuMap: Map<String, Package> = emptyMap()
    private lateinit var billingClient: BillingClient
    private var productIds: List<String>? = null
    private var removeAdsIds: List<String>? = null
    private var featureIds: List<String>? = null

    private var isBillingReady: Boolean = false
    private var subscriptionListener: SubscriptionListener? = null


    override fun initBilling(
        removeAdsIds: List<String>,
        featureIds: List<String>, subscriptionListener: SubscriptionListener
    ) {
        this.subscriptionListener = subscriptionListener
        this.removeAdsIds = removeAdsIds
        this.featureIds = featureIds
        this.productIds = (removeAdsIds + featureIds).distinct()


        coroutineScope.launch {
            try {
                queryPackageDetails(productIds ?: listOf())
            } catch (e: Exception) {
                subscriptionListener.subscriptionItemNotFound()
            }
//            if (isBillingReady) {
//            } else {
//                setupBillingClient()
//            }
        }
    }

    suspend fun queryPackageDetails(packageIds: List<String>) =
        suspendCancellableCoroutine { continuation ->
            Purchases.sharedInstance.getOfferingsWith(onSuccess = { offerings ->
                val packages: List<Package> =
                    offerings[OFFERINGS_ID]?.availablePackages?.filter { pkg ->
                        pkg.identifier in packageIds
                    }.orEmpty()

                skuMap = getSkuFromList(packages)
                continuation.resume(
                    subscriptionListener?.onQueryProductSuccess(
                        skuMap,
                        packages
                    )
                )
            }, onError = { error ->
                continuation.cancel(
                    Exception("queryPackageDetails: Failed to query products: msg= ${error.message}")
                )
                subscriptionListener?.subscriptionItemNotFound()
            })
        }

    override fun productPriceFlow(): StateFlow<PurchasePriceModel> {
        return _productPriceFlow.asStateFlow()
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
                    when (result.responseCode) {
                        BillingClient.BillingResponseCode.OK -> {
                            isProductPurchased(purchases)
                        }

                        BillingClient.BillingResponseCode.USER_CANCELED -> {
                            onUserDismissedPaywall?.invoke()
                            Log.d(TAG, "One-Time-Purchase: User dismissed the paywall")
                        }

                    }
                }
                .build()
        }
        if (isBillingReady) {
            return
        }

        if (!billingClient.isReady) {
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingServiceDisconnected() {
                    isBillingReady = false
//                    "Service Disconnected".logIt(BILLING_TAG)
                }

                override fun onBillingSetupFinished(result: BillingResult) {
                    if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                        isBillingReady = true
                        queryProductSkuForPurchase()
                    } else {
//                        "Setup Failed: ${result.responseCode}".logIt(BILLING_TAG)
                    }
                }
            })
        }
    }


    fun buildProductList(productIds: List<String>): List<QueryProductDetailsParams.Product> {
        return productIds.map { productId ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        }
    }

    private fun getActiveEntitlements(
        info: CustomerInfo, entitlementIds: List<String> = emptyList()
    ): List<EntitlementInfo> {
        val entitlements = if (entitlementIds.isEmpty()) {
            info.entitlements.all.values
        } else {
            entitlementIds.mapNotNull { info.entitlements[it] }
        }

        return entitlements.filter { it.isActive }
    }

    private fun queryProductSkuForPurchase() {

//        if (!isBillingClientReady()) return
//
//        productIds?.let { productIds->
//            val list = buildProductList(productIds)
//            val queryParams = QueryProductDetailsParams.newBuilder()
//                .setProductList(
//                    list
//                )
//                .build()
//
//            billingClient.queryProductDetailsAsync(queryParams) { result, queryProductDetailsResult ->
//                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
//                    val productList = queryProductDetailsResult.productDetailsList
//                    if (productList.isNotEmpty()) {
//                        skuMap = getSkuFromList(productList)
//                        if (productList.isNotEmpty()) {
//                            subscriptionListener?.onQueryProductSuccess(
//                                skuMap,
//                                productList
//                            )
//                        } else {
//                            subscriptionListener?.subscriptionItemNotFound()
//                        }
//                    }
//                } else {
////                "Product Query Failed: ${result.responseCode}".logIt(BILLING_TAG)
//                }
//            }
//        }


    }

    private fun getSkuFromList(list: List<Package>): Map<String, Package> {
        val skuDetailList: MutableMap<String, Package> = HashMap()
        list.forEach {
            it.identifier.let { sku ->
                if (!TextUtils.isEmpty(sku)) {
                    skuDetailList[sku] = it
                }
            }
        }
        return skuDetailList
    }

    override fun purchaseProduct(
        activity: Activity,
        productId: String, onUserDismissedPaywall: (() -> Unit)?
    ) {
        try {
            this.onUserDismissedPaywall = onUserDismissedPaywall
            if (internetController.isConnected.not()) {
                context.showToast(activity.getString(R.string.no_internet))
                return
            }
//            if (isBillingClientDead) {
//                context.showToast(activity.getString(R.string.no_internet))
//                return
//            }
//            Log.d(com.mobile.billing.revenuecat.RevenueCatBillingClient.Companion.TAG, "purchasePremiumOffer: Starting purchase for package: ${packageDetails.identifier}")

            if (activity.isFinishing || (Build.VERSION.SDK_INT >= 17 && activity.isDestroyed)) {
//                Log.w(com.mobile.billing.revenuecat.RevenueCatBillingClient.Companion.TAG, "purchasePremiumOffer: activity not in a valid state to launch billing flow")
                activity.let {
                    context.showToast(activity.getString(R.string.try_again))
                }
                return
            }
            skuMap[productId]?.let { details ->

                val params = PurchaseParams.Builder(activity, details).build()
                Purchases.sharedInstance.purchaseWith(
                    purchaseParams = params,
                    onSuccess = { _, info ->
                        val activeEntitlements = getActiveEntitlements(info)
                        val isActive = activeEntitlements.isNotEmpty()
                        if (isActive) {

                            activeEntitlements.forEach {
                                purchasesList.add(it.identifier)
                            }

                            activity.runOnUiThread {
                                subscriptionListener?.onSubscriptionPurchasedFetched(
                                    purchasesList
                                )
                            }
                        } else {
                            activity.let {
                                context.showToast(activity.getString(R.string.try_again))
                            }
                        }
                    },
                    onError = { error, userCancelled ->
                        if (userCancelled) {
                            onUserDismissedPaywall?.invoke()
                        } else {
                            activity.let {
                                context.showToast(activity.getString(R.string.try_again))
                            }
                        }
                    })
            }


        } catch (e: IntentSender.SendIntentException) {
            activity.let {
                context.showToast(activity.getString(R.string.try_again))
            }

        } catch (e: Exception) {
            activity.let {
                context.showToast(activity.getString(R.string.try_again))
            }
        }


    }

    override fun checkProductPurchaseHistory() {


        purchasesList.clear()

        Purchases.sharedInstance.getCustomerInfoWith(onSuccess = { info ->
            val activeEntitlements = getActiveEntitlements(info, listOf())
            val isActive = activeEntitlements.isNotEmpty()
            if (isActive) {
//                Log.d(com.mobile.billing.revenuecat.RevenueCatBillingClient.Companion.TAG, "queryPurchases: purchases found: $activeEntitlements")
//                onPurchasesFound(activeEntitlements)
                activeEntitlements.forEach {
                    purchasesList.add(it.identifier)
                }

                subscriptionListener?.onSubscriptionPurchasedFetched(
                    purchasesList
                )
            } else {
//                Log.d(com.mobile.billing.revenuecat.RevenueCatBillingClient.Companion.TAG, "queryPurchases: no purchases found")
                subscriptionListener?.onSubscriptionPurchasedFetched(
                    emptyList()
                )
            }
        }, onError = { error ->
//            Log.d(com.mobile.billing.revenuecat.RevenueCatBillingClient.Companion.TAG, "queryPurchases: failed error message: ${error.message}")
            subscriptionListener?.onSubscriptionPurchasedFetched(
                emptyList()
            )
        })


//        if (!isBillingClientReady()) return
//
//        purchasesList.clear()
//        billingClient.queryPurchasesAsync(
//            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP)
//                .build()
//        ) { p0, p1 ->
//            var purchasesFound = false
//            if (p0.responseCode == BillingClient.BillingResponseCode.OK) {
//                if (p1.isNotEmpty()) {
//                    for (purchase in p1) {
//                        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
//                            purchasesFound = true
//                            if (purchase.isAcknowledged) {
//                                purchasesList.add(
//                                    purchase.products.firstOrNull().orEmpty()
//                                )
//                                subscriptionListener?.onSubscriptionPurchasedFetched(
//                                    purchasesList
//                                )
//                            } else {
//                                acknowledgePurchase(purchase)
//                            }
//                        }
//                    }
//                }
//            }
//            if (!purchasesFound) {
//                subscriptionListener?.onSubscriptionPurchasedFetched(emptyList())
//            }
//
//        }
    }


    private fun isProductPurchased(list: List<Purchase>?) {


        if (!list.isNullOrEmpty()) {

            for (purchase in list) {

                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    if (purchase.isAcknowledged) {
                        purchasesList.add(purchase.products.firstOrNull().orEmpty())
                        subscriptionListener?.onSubscriptionPurchasedFetched(
                            purchasesList
                        )
                    } else {
                        acknowledgePurchase(
                            purchase
                        )
                    }
                }
            }
        }


//        val purchase = list?.toList()?.find { it.purchaseState == Purchase.PurchaseState.PURCHASED }
//            ?: return false
//        return if (purchase.products.contains(productId)) {
//            if (purchase.isAcknowledged) updatePurchaseStatus(true) else acknowledgePurchase(
//                purchase
//            )
//            true
//        } else false
    }

    private fun acknowledgePurchase(purchase: Purchase) {
        if (!isBillingClientReady()) return

        val acknowledgeParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        coroutineScope.launch {
            billingClient.acknowledgePurchase(acknowledgeParams) { result ->
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    purchasesList.add(purchase.products.firstOrNull().orEmpty())
                    subscriptionListener?.onSubscriptionPurchasedFetched(
                        purchasesList
                    )
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
