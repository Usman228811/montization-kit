package io.monetize.kit.sdk.data.impl

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.os.Build
import android.text.TextUtils
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
import com.android.billingclient.api.PurchasesResponseListener
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.EntitlementInfo
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.PurchaseParams
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.getCustomerInfoWith
import com.revenuecat.purchases.getOfferingsWith
import com.revenuecat.purchases.purchaseWith
import io.monetize.kit.sdk.R
import io.monetize.kit.sdk.core.utils.init.AdKit
import io.monetize.kit.sdk.core.utils.showToast
import io.monetize.kit.sdk.domain.repo.SubscriptionListener
import io.monetize.kit.sdk.domain.repo.SubscriptionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.collections.orEmpty
import kotlin.coroutines.resume
import kotlin.text.orEmpty


class RCSubscriptionRepositoryImpl private constructor(
    private val context: Context
) : SubscriptionRepository {

    private val OFFERINGS_ID = "default_offerings"

    private val purchasesList = mutableListOf<String>()
    private var onUserDismissedPaywall: (() -> Unit)? = null


    private var mActivity: Activity? = null

    companion object {
        const val TAG = "SubscriptionRepositoryImpl"

        @Volatile
        private var instance: RCSubscriptionRepositoryImpl? = null


        fun getInstance(
            context: Context,
        ): RCSubscriptionRepositoryImpl {
            return instance ?: synchronized(this) {
                instance ?: RCSubscriptionRepositoryImpl(
                    context
                ).also { instance = it }
            }
        }
    }

    private var subscriptionListener: SubscriptionListener? = null

    //    private var mActivity: Activity? = null
    private var productIds: List<String>? = null
    private var removeAdsIds: List<String>? = null
    private var featureIds: List<String>? = null
    private var subscribeProductToken = ""



    override fun purchaseProduct(
        activity: Activity,
        skuDetails: Package,
        onUserDismissedPaywall: (() -> Unit)?
    ) {
        try {
            this.onUserDismissedPaywall = onUserDismissedPaywall
            if (AdKit.internetController.isConnected.not()) {
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

            val params = PurchaseParams.Builder(activity, skuDetails).build()
            Purchases.sharedInstance.purchaseWith(purchaseParams = params, onSuccess = { _, info ->
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
                }else{
                    activity.let {
                        context.showToast(activity.getString(R.string.try_again))
                    }
                }
            }, onError = { error, userCancelled ->
                if (userCancelled) {
                    onUserDismissedPaywall?.invoke()
                }else{
                    activity.let {
                        context.showToast(activity.getString(R.string.try_again))
                    }
                }
            })

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

    override fun purchaseProduct(
        activity: Activity,
        skuDetails: ProductDetails,
        onUserDismissedPaywall: (() -> Unit)?
    ) {
    }

    override fun changeSubscriptionPlan(activity: Activity, skuDetails: ProductDetails) {
//        try {
//            if (isBillingClientDead) {
//                return
//            }
//            val offerToken = skuDetails.subscriptionOfferDetails?.get(0)!!.offerToken
//            val list: MutableList<BillingFlowParams.ProductDetailsParams> = ArrayList()
//            list.add(
//                BillingFlowParams.ProductDetailsParams.newBuilder()
//                    .setProductDetails(skuDetails)
//                    .setOfferToken(offerToken)
//                    .build()
//            )
//            val flowParams = BillingFlowParams.newBuilder()
//                .setSubscriptionUpdateParams(
//                    BillingFlowParams.SubscriptionUpdateParams.newBuilder()
//                        .setOldPurchaseToken(subscribeProductToken)
//                        .setSubscriptionReplacementMode(BillingFlowParams.SubscriptionUpdateParams.ReplacementMode.WITH_TIME_PRORATION)
//                        .build()
//                )
//                .setProductDetailsParamsList(list)
//                .build()
//            if (subscriptionClient.launchBillingFlow(
//                    activity, flowParams
//                ).responseCode == BillingClient.BillingResponseCode.OK
//            ) {
////                JavaUtils.sendAnalytics(context, "SUBSCRIBE_UPDATE_CLICK")
//            }
//        } catch (e: IntentSender.SendIntentException) {
//            activity.let {
//                context.showToast(activity.getString(R.string.try_again))
//            }
//
//        } catch (e: Exception) {
//            activity.let {
//                context.showToast(activity.getString(R.string.try_again))
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


    private fun getSku(skuList: MutableList<String>): String {
        return if (skuList.size > 0) {
            skuList[0]
        } else ""
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

    override fun querySubscriptionHistory(activity: Activity) {

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
    }

    override fun setSubscribed(activity: Activity, purchase: Purchase) {
        subscribeProductToken = purchase.purchaseToken
    }


    override fun getSelectedSubscriptionId(selectedPosition: Int): String {
        return ""
    }

    override fun isSubscriptionSupported(): Boolean {
        return true
    }

    override fun isSubscriptionUpdateSupported(): Boolean {
        return true
    }

    private fun checkSubscriptionsId(sku: String?): Boolean {
        productIds?.let { productIds ->
            return sku != null && productIds.isNotEmpty() && productIds.contains(sku)
        }
        return false
    }

    override fun acknowledgedPurchase(activity: Activity, purchase: Purchase) {

    }

    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())


    override fun setBillingListener(
        activity: Activity,
        removeAdsIds: List<String>,
        featureIds: List<String>,
        listener: SubscriptionListener?
    ) {
        mActivity = activity
        this.subscriptionListener = listener
        this.removeAdsIds = removeAdsIds
        this.featureIds = featureIds
        this.productIds = (removeAdsIds + featureIds).distinct()
        coroutineScope.launch {
            try {
                queryPackageDetails(productIds?: listOf())
            } catch (e: Exception) {
                activity.runOnUiThread {
                    subscriptionListener?.subscriptionItemNotFound()
                }
            }
        }
    }

    suspend fun queryPackageDetails(packageIds: List<String>) =
        suspendCancellableCoroutine { continuation ->
            Purchases.sharedInstance.getOfferingsWith(onSuccess = { offerings ->
                val packages: List<Package> = offerings[OFFERINGS_ID]?.availablePackages?.filter { pkg ->
                    pkg.identifier in packageIds
                }.orEmpty()

                continuation.resume(
                    subscriptionListener?.onQueryProductSuccess(
                        getSkuFromList(packages),
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


//    init {
//        setupConnection()
//    }

    override fun viewUrl(activity: Activity, url: String) {
        try {
            Intent().apply {
                action = Intent.ACTION_VIEW
                data = url.toUri()
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                addCategory(Intent.CATEGORY_BROWSABLE)
            }.also {
                if (it.resolveActivity(activity.packageManager) != null) {
                    activity.startActivity(it)
                }
            }
        } catch (ignored: Exception) {
        }
    }
}