package io.monetize.kit.sdk.data.impl

import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesResponseListener
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.revenuecat.purchases.Package
import io.monetize.kit.sdk.R
import io.monetize.kit.sdk.core.utils.init.AdKit
import io.monetize.kit.sdk.core.utils.showToast
import io.monetize.kit.sdk.domain.repo.PlayBillingQueryResult
import io.monetize.kit.sdk.domain.repo.SubscriptionListener
import io.monetize.kit.sdk.domain.repo.SubscriptionRepository

class PlaySubscriptionRepositoryImpl private constructor(
    private val context: Context
) : SubscriptionRepository, PurchasesUpdatedListener {

    private val purchasesList = mutableListOf<String>()
    private var onUserDismissedPaywall: (() -> Unit)? = null
    private var currentActivity: Activity? = null

    companion object {
        private const val TAG = "SubscriptionRepositoryImpl"

        @Volatile
        private var instance: PlaySubscriptionRepositoryImpl? = null

        fun getInstance(
            context: Context,
        ): PlaySubscriptionRepositoryImpl {
            return instance ?: synchronized(this) {
                instance ?: PlaySubscriptionRepositoryImpl(context).also { instance = it }
            }
        }
    }

    private var isBillingReady: Boolean = false
    private lateinit var subscriptionClient: BillingClient
    private var subscriptionListener: SubscriptionListener? = null
    private var productIds: List<String> = emptyList()
    private var subscribeProductToken = ""

    private val isBillingClientDead: Boolean
        get() = !::subscriptionClient.isInitialized


    val isBillingClientReady: Boolean
        get() = !isBillingClientDead && subscriptionClient.isReady

    // The client only has to exist: with auto service reconnection a call made while
    // disconnected reconnects first, so don't skip calls just because isReady is false
    private val canCallBilling: Boolean
        get() = !isBillingClientDead

    override fun purchaseProduct(
        activity: Activity,
        skuDetails: Package,
        onUserDismissedPaywall: (() -> Unit)?,
    ) = Unit

    override fun purchaseProduct(
        activity: Activity,
        skuDetails: ProductDetails,
        onUserDismissedPaywall: (() -> Unit)?,
    ) {
        try {
            this.onUserDismissedPaywall = onUserDismissedPaywall
            if (AdKit.internetController.isConnected.not()) {
                context.showNoInternet(activity)
                return
            }
            if (!canCallBilling) {
                context.showTryAgain(activity)
                return
            }

            val billingParams = skuDetails.toBillingFlowParams()
            if (billingParams == null) {
                context.showTryAgain(activity)
                return
            }

            val billingResult = subscriptionClient.launchBillingFlow(activity, billingParams)
            if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                context.showTryAgain(activity)
            }
        } catch (_: IntentSender.SendIntentException) {
            context.showTryAgain(activity)
        } catch (_: Exception) {
            context.showTryAgain(activity)
        }
    }

    override fun changeSubscriptionPlan(activity: Activity, skuDetails: Package) = Unit

    override fun changeSubscriptionPlan(
        activity: Activity,
        skuDetails: ProductDetails
    ) {
        try {
            if (AdKit.internetController.isConnected.not()) {
                context.showToast(activity.getString(R.string.no_internet))
                return
            }

            if (!canCallBilling) {
                context.showToast(activity.getString(R.string.try_again))
                return
            }

            val newOfferToken = skuDetails.firstOfferToken()
            if (newOfferToken.isNullOrBlank()) {
                context.showToast(activity.getString(R.string.try_again))
                return
            }

            queryActiveSubscriptionPurchase(activity) { activePurchase ->
                try {
                    if (activePurchase == null) {
                        context.showToast(activity.getString(R.string.try_again))
                        return@queryActiveSubscriptionPurchase
                    }

                    val oldPurchaseToken = activePurchase.purchaseToken
                    val oldProductId = activePurchase.products.firstOrNull().orEmpty()

                    if (oldPurchaseToken.isBlank() || oldProductId.isBlank()) {
                        context.showToast(activity.getString(R.string.try_again))
                        return@queryActiveSubscriptionPurchase
                    }

                    if (oldProductId == skuDetails.productId) {
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
                        context.showToast(activity.getString(R.string.try_again))
                    }

                } catch (_: LinkageError) {
                    context.showToast(activity.getString(R.string.try_again))
                } catch (_: Exception) {
                    context.showToast(activity.getString(R.string.try_again))
                }
            }

        } catch (_: LinkageError) {
            context.showToast(activity.getString(R.string.try_again))
        } catch (_: Exception) {
            context.showToast(activity.getString(R.string.try_again))
        }
    }

    private fun buildSubscriptionProductList(productIds: List<String>): List<QueryProductDetailsParams.Product> {
        return productIds.map { productId ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        }
    }

    private fun querySubscriptionProducts(activity: Activity) {
        if (isBillingClientDead || productIds.isEmpty() || !isSubscriptionSupported()) {
            return
        }

        val queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
            .setProductList(buildSubscriptionProductList(productIds))
            .build()
        subscriptionClient.queryProductDetailsAsync(queryProductDetailsParams) { billingResult, details ->
            activity.runOnUiThread {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK &&
                    details.productDetailsList.isNotEmpty()
                ) {
                    subscriptionListener?.onQueryProductSuccess(
                        PlayBillingQueryResult(
                            skuList = details.productDetailsList.toProductDetailsMap(),
                            productList = details.productDetailsList
                        )
                    )
                } else {
                    subscriptionListener?.subscriptionItemNotFound()
                }
            }
        }
    }

    private fun resetAllPurchases() {
        subscribeProductToken = ""
        purchasesList.clear()
    }

    private fun getSku(skuList: MutableList<String>): String = skuList.firstOrNull().orEmpty()

    override fun querySubscriptionHistory(activity: Activity) {
        // Only an OK purchase query says what the user owns. When billing isn't available or
        // the query fails, keep the saved subscription state instead of reporting "no purchases".
        try {
            if (isBillingClientDead) {
                return
            }

            if (subscriptionClient.isFeatureSupported(BillingClient.FeatureType.SUBSCRIPTIONS).responseCode !=
                BillingClient.BillingResponseCode.OK
            ) {
                Log.e(TAG, "querySubscriptionHistory skipped: subscriptions are not available right now")
                return
            }

            subscriptionClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build(),
                object : PurchasesResponseListener {
                    override fun onQueryPurchasesResponse(
                        billingResult: BillingResult,
                        purchases: MutableList<Purchase>
                    ) {
                        if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                            Log.e(TAG, "querySubscriptionHistory failed: ${billingResult.debugMessage}")
                            return
                        }

                        purchasesList.clear()
                        var purchasesFound = false
                        for (purchase in purchases) {
                            if (processSubscriptionPurchase(activity, purchase)) {
                                purchasesFound = true
                            }
                        }

                        if (!purchasesFound) {
                            resetAllPurchases()
                            activity.runOnUiThread { subscriptionListener.dispatchPurchases(emptyList()) }
                        }
                    }
                }
            )
        } catch (e: LinkageError) {
            Log.e(TAG, "querySubscriptionHistory: Billing linkage error", e)
        } catch (e: Exception) {
            Log.e(TAG, "querySubscriptionHistory: Failed", e)
        }
    }

    private fun processSubscriptionPurchase(activity: Activity, purchase: Purchase): Boolean {
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) {
            return false
        }
        if (!checkSubscriptionsId(getSku(purchase.products))) {
            return false
        }
        // Grant it right away; acknowledging only stops Google refunding it after 3 days
        notifyPurchase(activity, purchase)
        if (!purchase.isAcknowledged) {
            acknowledgedPurchase(activity, purchase)
        }
        return true
    }

    private fun notifyPurchase(activity: Activity, purchase: Purchase) {
        setSubscribed(activity, purchase)
        val updatedPurchases = purchasesList.addDistinct(purchase.primaryProductId())
        activity.runOnUiThread {
            subscriptionListener.dispatchPurchases(updatedPurchases)
        }
    }

    override fun setSubscribed(activity: Activity, purchase: Purchase) {
        subscribeProductToken = purchase.purchaseToken
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, list: List<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                currentActivity?.let { activity ->
                    if (!list.isNullOrEmpty()) {
                        for (purchase in list) {
                            processSubscriptionPurchase(activity, purchase)
                        }
                    }
                }
            }

            BillingClient.BillingResponseCode.USER_CANCELED -> {
                Log.d(TAG, "Subscription: User dismissed the paywall")
                onUserDismissedPaywall?.invoke()
            }
        }
    }


    override fun isSubscriptionSupported(): Boolean {
        if (!canCallBilling) {
            return false
        }
        return subscriptionClient.isFeatureSupported(BillingClient.FeatureType.SUBSCRIPTIONS).responseCode ==
                BillingClient.BillingResponseCode.OK
    }

    override fun isSubscriptionUpdateSupported(): Boolean {
        if (!canCallBilling) {
            return false
        }
        return subscriptionClient.isFeatureSupported(BillingClient.FeatureType.SUBSCRIPTIONS_UPDATE).responseCode ==
                BillingClient.BillingResponseCode.OK
    }

    private fun checkSubscriptionsId(sku: String?): Boolean {
        return sku != null && productIds.isNotEmpty() && productIds.contains(sku)
    }

    override fun acknowledgedPurchase(activity: Activity, purchase: Purchase) {
        acknowledgePurchaseWithRetry(activity, purchase, attempt = 1)
    }

    private fun acknowledgePurchaseWithRetry(activity: Activity, purchase: Purchase, attempt: Int) {
        if (isBillingClientDead) {
            return
        }
        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        subscriptionClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                notifyPurchase(activity, purchase)
            } else if (attempt < MAX_ACKNOWLEDGE_ATTEMPTS && billingResult.responseCode in RETRYABLE_ACKNOWLEDGE_CODES) {
                Log.w(TAG, "Acknowledge failed (${billingResult.responseCode}), retry $attempt")
                Handler(Looper.getMainLooper()).postDelayed({
                    acknowledgePurchaseWithRetry(activity, purchase, attempt + 1)
                }, acknowledgeRetryDelayMillis(attempt))
            } else {
                Log.e(TAG, "Acknowledge failed (${billingResult.responseCode}): ${billingResult.debugMessage}")
            }
        }
    }

    override fun setBillingListener(
        activity: Activity,
        removeAdsIds: List<String>,
        featureIds: List<String>,
        listener: SubscriptionListener?
    ) {
        currentActivity = activity
        subscriptionListener = listener
        productIds = (removeAdsIds + featureIds).distinct()
        if (isBillingReady) {
            querySubscriptionProducts(activity)
        } else {
            setupConnection(activity)
        }
    }

    private fun setupConnection(activity: Activity) {
        try {
            if (!::subscriptionClient.isInitialized) {
                subscriptionClient = BillingClient
                    .newBuilder(context)
                    .enablePendingPurchases(
                        PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
                    )
                    // After a disconnect, the next billing call reconnects first instead of failing
                    .enableAutoServiceReconnection()
                    .setListener(this)
                    .build()
            }
            if (isBillingReady || subscriptionClient.isReady) {
                return
            }
            subscriptionClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        isBillingReady = true
                        querySubscriptionProducts(activity)
                    }
                }

                override fun onBillingServiceDisconnected() {
                    isBillingReady = false
                }
            })
        } catch (_: Exception) {
        }
    }

    override fun viewUrl(activity: Activity, url: String) {
        activity.openBrowsableUrl(url)
    }

    private fun ProductDetails.firstOfferToken(): String? {
        return subscriptionOfferDetails
            ?.firstOrNull()
            ?.offerToken
    }

    private fun queryActiveSubscriptionPurchase(
        activity: Activity,
        onResult: (Purchase?) -> Unit
    ) {
        if (!canCallBilling) {
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
                    null
                }

            activity.runOnUiThread {
                onResult(activePurchase)
            }
        }
    }

}
