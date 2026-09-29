package io.monetize.kit.sdk.data.impl

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.net.toUri
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import io.monetize.kit.sdk.R
import io.monetize.kit.sdk.core.utils.init.AdKit
import io.monetize.kit.sdk.core.utils.showToast
import io.monetize.kit.sdk.domain.repo.SubscriptionListener
import io.monetize.kit.sdk.domain.repo.SubscriptionRepository


class SubscriptionRepositoryImpl private constructor(
    private val context: Context
) : SubscriptionRepository, PurchasesUpdatedListener {

    private val purchasesList = mutableListOf<String>()
    private var onUserDismissedPaywall: (() -> Unit)? = null

    private var mActivity: Activity? = null

    companion object {
        const val TAG = "SubscriptionRepositoryImpl"

        @Volatile
        private var instance: SubscriptionRepositoryImpl? = null

        fun getInstance(context: Context): SubscriptionRepositoryImpl {
            return instance ?: synchronized(this) {
                instance ?: SubscriptionRepositoryImpl(context.applicationContext)
                    .also { instance = it }
            }
        }
    }

    private var isBillingReady: Boolean = false
    private lateinit var subscriptionClient: BillingClient
    private var subscriptionListener: SubscriptionListener? = null

    private var productIds: List<String>? = null
    private var removeAdsIds: List<String>? = null
    private var featureIds: List<String>? = null

    private var subscribeProductToken: String = ""
    private var subscribedProductId: String = ""

    private val isBillingClientDead: Boolean
        get() = !::subscriptionClient.isInitialized

    val isBillingClientReady: Boolean
        get() = !isBillingClientDead && subscriptionClient.isReady

    override fun purchaseProduct(
        activity: Activity,
        skuDetails: ProductDetails,
        onUserDismissedPaywall: (() -> Unit)?
    ) {
        try {
            this.onUserDismissedPaywall = onUserDismissedPaywall

            if (AdKit.internetController.isConnected.not()) {
                context.showToast(activity.getString(R.string.no_internet))
                return
            }

            if (!isBillingClientReady) {
                context.showToast(activity.getString(R.string.try_again))
                return
            }

            val offerToken = skuDetails.firstOfferToken()
            if (offerToken.isNullOrBlank()) {
                Log.e(TAG, "Subscription purchase: Missing offer token for ${skuDetails.productId}")
                context.showToast(activity.getString(R.string.try_again))
                return
            }

            val productDetailsParams =
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(skuDetails)
                    .setOfferToken(offerToken)
                    .build()

            val flowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(listOf(productDetailsParams))
                .build()

            val billingResult = subscriptionClient.launchBillingFlow(activity, flowParams)

            if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                Log.e(TAG, "Purchase flow failed: ${billingResult.debugMessage}")
                context.showToast(activity.getString(R.string.try_again))
            }

        } catch (e: LinkageError) {
            Log.e(TAG, "purchaseProduct: Billing linkage error", e)
            context.showToast(activity.getString(R.string.try_again))
        } catch (e: Exception) {
            Log.e(TAG, "purchaseProduct: Failed", e)
            context.showToast(activity.getString(R.string.try_again))
        }
    }

    override fun changeSubscriptionPlan(
        activity: Activity,
        skuDetails: ProductDetails
    ) {
        try {
            if (AdKit.internetController.isConnected.not()) {
                context.showToast(activity.getString(R.string.no_internet))
                return
            }

            if (!isBillingClientReady) {
                context.showToast(activity.getString(R.string.try_again))
                return
            }

            val newOfferToken = skuDetails.firstOfferToken()
            if (newOfferToken.isNullOrBlank()) {
                Log.e(TAG, "Subscription update: Missing offer token for ${skuDetails.productId}")
                context.showToast(activity.getString(R.string.try_again))
                return
            }

            queryActiveSubscriptionPurchase(activity) { activePurchase ->
                try {
                    if (activePurchase == null) {
                        Log.e(TAG, "Subscription update: No active subscription purchase found")
                        context.showToast(activity.getString(R.string.try_again))
                        return@queryActiveSubscriptionPurchase
                    }

                    val oldPurchaseToken = activePurchase.purchaseToken
                    val oldProductId = activePurchase.products.firstOrNull().orEmpty()

                    if (oldPurchaseToken.isBlank() || oldProductId.isBlank()) {
                        Log.e(
                            TAG,
                            "Subscription update: Missing old product/token. oldProductId=$oldProductId"
                        )
                        context.showToast(activity.getString(R.string.try_again))
                        return@queryActiveSubscriptionPurchase
                    }

                    if (oldProductId == skuDetails.productId) {
                        Log.d(TAG, "Subscription update: Same product selected: $oldProductId")
                        context.showToast(activity.getString(R.string.try_again))
                        return@queryActiveSubscriptionPurchase
                    }

                    val replacementParams =
                        BillingFlowParams.ProductDetailsParams.SubscriptionProductReplacementParams
                            .newBuilder()
                            .setOldProductId(oldProductId)
                            .setReplacementMode(
                                BillingFlowParams.ProductDetailsParams
                                    .SubscriptionProductReplacementParams
                                    .ReplacementMode
                                    .WITH_TIME_PRORATION
                            )
                            .build()

                    val productDetailsParams =
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(skuDetails)
                            .setOfferToken(newOfferToken)
                            .setSubscriptionProductReplacementParams(replacementParams)
                            .build()

                    val flowParams = BillingFlowParams.newBuilder()
                        .setSubscriptionUpdateParams(
                            BillingFlowParams.SubscriptionUpdateParams.newBuilder()
                                .setOldPurchaseToken(oldPurchaseToken)
                                .build()
                        )
                        .setProductDetailsParamsList(listOf(productDetailsParams))
                        .build()

                    val billingResult = subscriptionClient.launchBillingFlow(activity, flowParams)

                    if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                        Log.e(TAG, "Subscription update flow failed: ${billingResult.debugMessage}")
                        context.showToast(activity.getString(R.string.try_again))
                    }

                } catch (e: LinkageError) {
                    Log.e(TAG, "changeSubscriptionPlan callback: Billing linkage error", e)
                    context.showToast(activity.getString(R.string.try_again))
                } catch (e: Exception) {
                    Log.e(TAG, "changeSubscriptionPlan callback: Failed", e)
                    context.showToast(activity.getString(R.string.try_again))
                }
            }

        } catch (e: LinkageError) {
            Log.e(TAG, "changeSubscriptionPlan: Billing linkage error", e)
            context.showToast(activity.getString(R.string.try_again))
        } catch (e: Exception) {
            Log.e(TAG, "changeSubscriptionPlan: Failed", e)
            context.showToast(activity.getString(R.string.try_again))
        }
    }

    private fun queryActiveSubscriptionPurchase(
        activity: Activity,
        onResult: (Purchase?) -> Unit
    ) {
        if (!isBillingClientReady) {
            activity.runOnUiThread { onResult(null) }
            return
        }

        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        subscriptionClient.queryPurchasesAsync(params) { billingResult, purchases ->
            val activePurchase =
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    purchases.firstOrNull { purchase ->
                        purchase.purchaseState == Purchase.PurchaseState.PURCHASED && purchase.products.any { productId -> checkSubscriptionsId(productId) }
                    }
                } else {
                    Log.e(
                        TAG,
                        "queryActiveSubscriptionPurchase failed: ${billingResult.debugMessage}"
                    )
                    null
                }

            activity.runOnUiThread {
                onResult(activePurchase)
            }
        }
    }

    private fun ProductDetails.firstOfferToken(): String? {
        return subscriptionOfferDetails
            ?.firstOrNull()
            ?.offerToken
    }

    fun buildSubscriptionProductList(
        productIds: List<String>
    ): List<QueryProductDetailsParams.Product> {
        return productIds.map { productId ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        }
    }

    fun querySubscriptionProducts(activity: Activity) {
        if (!isBillingClientReady) return

        val ids = productIds.orEmpty()
        if (ids.isEmpty()) {
            activity.runOnUiThread {
                subscriptionListener?.subscriptionItemNotFound()
            }
            return
        }

        if (!isSubscriptionSupported()) {
            activity.runOnUiThread {
                subscriptionListener?.subscriptionItemNotFound()
            }
            return
        }

        try {
            val queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
                .setProductList(buildSubscriptionProductList(ids))
                .build()

            subscriptionClient.queryProductDetailsAsync(queryProductDetailsParams) { billingResult, result ->
                val productDetailsList = result.productDetailsList

                activity.runOnUiThread {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK &&
                        productDetailsList.isNotEmpty()
                    ) {
                        subscriptionListener?.onQueryProductSuccess(
                            getSkuFromList(productDetailsList),
                            productDetailsList.toMutableList()
                        )
                    } else {
                        Log.e(TAG, "Product query failed: ${billingResult.debugMessage}")
                        subscriptionListener?.subscriptionItemNotFound()
                    }
                }
            }

        } catch (e: LinkageError) {
            Log.e(TAG, "querySubscriptionProducts: Billing linkage error", e)
            activity.runOnUiThread {
                subscriptionListener?.subscriptionItemNotFound()
            }
        } catch (e: Exception) {
            Log.e(TAG, "querySubscriptionProducts: Failed", e)
            activity.runOnUiThread {
                subscriptionListener?.subscriptionItemNotFound()
            }
        }
    }

    private fun getSkuFromList(list: List<ProductDetails>): Map<String, ProductDetails> {
        val skuDetailList: MutableMap<String, ProductDetails> = HashMap()
        list.forEach { details ->
            val productId = details.productId
            if (productId.isNotBlank()) {
                skuDetailList[productId] = details
            }
        }
        return skuDetailList
    }

    private fun resetAllPurchases(activity: Activity) {
        subscribeProductToken = ""
        subscribedProductId = ""
    }

    private fun getSku(skuList: List<String>): String {
        return skuList.firstOrNull().orEmpty()
    }

    override fun querySubscriptionHistory(activity: Activity) {
        purchasesList.clear()

        if (!isBillingClientReady) {
            resetAllPurchases(activity)
            activity.runOnUiThread {
                subscriptionListener?.onSubscriptionPurchasedFetched(emptyList())
            }
            return
        }

        if (!isSubscriptionSupported()) {
            resetAllPurchases(activity)
            activity.runOnUiThread {
                subscriptionListener?.onSubscriptionPurchasedFetched(emptyList())
            }
            return
        }

        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        subscriptionClient.queryPurchasesAsync(params) { billingResult, purchases ->
            var purchasesFound = false

            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                purchases.forEach { purchase ->
                    val purchasedProductId = getSku(purchase.products)

                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
                        checkSubscriptionsId(purchasedProductId)
                    ) {
                        purchasesFound = true

                        if (purchase.isAcknowledged) {
                            purchasesList.add(purchasedProductId)
                            setSubscribed(activity, purchase)
                        } else {
                            acknowledgedPurchase(activity, purchase)
                        }
                    }
                }
            } else {
                Log.e(TAG, "querySubscriptionHistory failed: ${billingResult.debugMessage}")
            }

            if (!purchasesFound) {
                resetAllPurchases(activity)
            }

            activity.runOnUiThread {
                subscriptionListener?.onSubscriptionPurchasedFetched(
                    if (purchasesFound) purchasesList.distinct() else emptyList()
                )
            }
        }
    }

    override fun setSubscribed(activity: Activity, purchase: Purchase) {
        subscribeProductToken = purchase.purchaseToken
        subscribedProductId = purchase.products.firstOrNull().orEmpty()
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        list: List<Purchase>?
    ) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                val activity = mActivity ?: return

                if (list.isNullOrEmpty()) return

                list.forEach { purchase ->
                    val purchasedProductId = getSku(purchase.products)

                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
                        checkSubscriptionsId(purchasedProductId)
                    ) {
                        if (purchase.isAcknowledged) {
                            purchasesList.add(purchasedProductId)
                            setSubscribed(activity, purchase)

                            activity.runOnUiThread {
                                subscriptionListener?.onSubscriptionPurchasedFetched(
                                    purchasesList.distinct()
                                )
                            }
                        } else {
                            acknowledgedPurchase(activity, purchase)
                        }
                    }
                }
            }

            BillingClient.BillingResponseCode.USER_CANCELED -> {
                Log.d(TAG, "Subscription: User dismissed the paywall")
                onUserDismissedPaywall?.invoke()
            }

            else -> {
                Log.e(TAG, "onPurchasesUpdated failed: ${billingResult.debugMessage}")
            }
        }
    }

    override fun getSelectedSubscriptionId(selectedPosition: Int): String {
        return ""
    }

    override fun isSubscriptionSupported(): Boolean {
        if (!isBillingClientReady) return false

        return subscriptionClient
            .isFeatureSupported(BillingClient.FeatureType.SUBSCRIPTIONS)
            .responseCode == BillingClient.BillingResponseCode.OK
    }

    override fun isSubscriptionUpdateSupported(): Boolean {
        if (!isBillingClientReady) return false

        return subscriptionClient
            .isFeatureSupported(BillingClient.FeatureType.SUBSCRIPTIONS_UPDATE)
            .responseCode == BillingClient.BillingResponseCode.OK
    }

    private fun checkSubscriptionsId(sku: String?): Boolean {
        val ids = productIds.orEmpty()
        return !sku.isNullOrBlank() && ids.isNotEmpty() && ids.contains(sku)
    }

    override fun acknowledgedPurchase(activity: Activity, purchase: Purchase) {
        if (!isBillingClientReady) return

        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        subscriptionClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val purchasedProductId = purchase.products.firstOrNull().orEmpty()

                setSubscribed(activity, purchase)

                if (purchasedProductId.isNotBlank()) {
                    purchasesList.add(purchasedProductId)
                }

                activity.runOnUiThread {
                    subscriptionListener?.onSubscriptionPurchasedFetched(
                        purchasesList.distinct()
                    )
                }
            } else {
                Log.e(TAG, "Acknowledge failed: ${billingResult.debugMessage}")
            }
        }
    }

    override fun setBillingListener(
        activity: Activity,
        removeAdsIds: List<String>,
        featureIds: List<String>,
        listener: SubscriptionListener?
    ) {
        mActivity = activity
        subscriptionListener = listener
        this.removeAdsIds = removeAdsIds
        this.featureIds = featureIds
        productIds = (removeAdsIds + featureIds).distinct()

        if (isBillingClientReady) {
            isBillingReady = true
            querySubscriptionProducts(activity)
            querySubscriptionHistory(activity)
        } else {
            setupConnection(activity)
        }
    }

    private fun setupConnection(activity: Activity) {
        try {
            if (!::subscriptionClient.isInitialized) {
                subscriptionClient = BillingClient.newBuilder(context)
                    .enablePendingPurchases(
                        PendingPurchasesParams.newBuilder()
                            .enableOneTimeProducts()
                            .build()
                    )
                    .setListener(this)
                    .build()
            }

            if (subscriptionClient.isReady) {
                isBillingReady = true
                querySubscriptionProducts(activity)
                querySubscriptionHistory(activity)
                return
            }

            subscriptionClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        isBillingReady = true
                        querySubscriptionProducts(activity)
                        querySubscriptionHistory(activity)
                    } else {
                        isBillingReady = false
                        Log.e(TAG, "Billing setup failed: ${billingResult.debugMessage}")
                    }
                }

                override fun onBillingServiceDisconnected() {
                    isBillingReady = false
                    Log.w(TAG, "Billing service disconnected")
                }
            })

        } catch (e: LinkageError) {
            isBillingReady = false
            Log.e(TAG, "setupConnection: Billing linkage error", e)
        } catch (e: Exception) {
            isBillingReady = false
            Log.e(TAG, "setupConnection: Failed", e)
        }
    }

    override fun viewUrl(activity: Activity, url: String) {
        try {
            Intent().apply {
                action = Intent.ACTION_VIEW
                data = url.toUri()
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                addCategory(Intent.CATEGORY_BROWSABLE)
            }.also { intent ->
                if (intent.resolveActivity(activity.packageManager) != null) {
                    activity.startActivity(intent)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "viewUrl failed", e)
        }
    }
}
