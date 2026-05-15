package io.monetize.kit.sdk.domain.repo

import android.app.Activity
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.revenuecat.purchases.Package

interface SubscriptionRepository {
    fun setBillingListener(activity: Activity,removeAdsIds: List<String>,
                           featureIds: List<String>, listener: SubscriptionListener?)
    fun querySubscriptionHistory(activity: Activity)
    fun purchaseProduct(activity: Activity,skuDetails: ProductDetails,onUserDismissedPaywall :(()->Unit) ?= null)
    fun purchaseProduct(activity: Activity,skuDetails: Package,onUserDismissedPaywall :(()->Unit) ?= null)
    fun changeSubscriptionPlan(activity: Activity,skuDetails: ProductDetails)
    fun getSelectedSubscriptionId(selectedPosition: Int): String
    fun isSubscriptionSupported(): Boolean
    fun isSubscriptionUpdateSupported(): Boolean
    fun setSubscribed(activity: Activity,purchase: Purchase)
    fun acknowledgedPurchase(activity: Activity,purchase: Purchase)
    fun viewUrl( activity: Activity, url: String)

}

interface SubscriptionListener {
    fun onQueryProductSuccess(skuList: Map<String, Any>, productList: List<Any>)
    fun subscriptionItemNotFound()
    fun onSubscriptionPurchasedFetched(purchasesList:List<String>)
}