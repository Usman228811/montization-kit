package io.monetize.kit.sdk.data.impl

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.text.TextUtils
import androidx.core.net.toUri
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.EntitlementInfo
import com.revenuecat.purchases.Package
import io.monetize.kit.sdk.R
import io.monetize.kit.sdk.core.utils.showToast
import io.monetize.kit.sdk.domain.repo.SubscriptionListener


internal fun Context.showTryAgain(activity: Activity) {
    showToast(activity.getString(R.string.try_again))
}

internal fun Context.showNoInternet(activity: Activity) {
    showToast(activity.getString(R.string.no_internet))
}


data class RevenueCatBuilder(
    val revenueCatKey: String,
    val offeringKey: String,
)

internal fun Activity.canLaunchBillingFlow(): Boolean {
    return !isFinishing && !(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && isDestroyed)
}

internal fun Activity.openBrowsableUrl(url: String) {
    try {
        Intent().apply {
            action = Intent.ACTION_VIEW
            data = url.toUri()
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            addCategory(Intent.CATEGORY_BROWSABLE)
        }.also { intent ->
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            }
        }
    } catch (_: Exception) {
    }
}

internal fun List<ProductDetails>.toProductDetailsMap(): Map<String, ProductDetails> {
    val skuDetailList = mutableMapOf<String, ProductDetails>()
    forEach { details ->
        val sku = details.productId
        if (!TextUtils.isEmpty(sku)) {
            skuDetailList[sku] = details
        }
    }
    return skuDetailList
}

internal fun List<Package>.toPackageMap(): Map<String, Package> {
    val skuDetailList = mutableMapOf<String, Package>()
    forEach { details ->
        val sku = details.identifier
        if (!TextUtils.isEmpty(sku)) {
            skuDetailList[sku] = details
        }
    }
    return skuDetailList
}

internal fun ProductDetails.toBillingFlowParams(): BillingFlowParams? {
    val offerToken = subscriptionOfferDetails?.firstOrNull()?.offerToken ?: return null
    return BillingFlowParams.newBuilder()
        .setProductDetailsParamsList(
            listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(this)
                    .setOfferToken(offerToken)
                    .build()
            )
        )
        .build()
}

internal fun Purchase.primaryProductId(): String = products.firstOrNull().orEmpty()

internal fun MutableList<String>.replaceWithDistinct(values: List<String>): List<String> {
    clear()
    addAll(values.distinct())
    return toList()
}

internal fun MutableList<String>.addDistinct(value: String): List<String> {
    if (value.isNotEmpty() && value !in this) {
        add(value)
    }
    return toList()
}

internal fun SubscriptionListener?.dispatchPurchases(purchases: List<String>) {
    this?.onSubscriptionPurchasedFetched(purchases.distinct())
}

internal fun CustomerInfo.activeEntitlementIds(entitlementIds: List<String> = emptyList()): List<String> {
    val entitlements: Collection<EntitlementInfo> = if (entitlementIds.isEmpty()) {
        this.entitlements.all.values
    } else {
        entitlementIds.mapNotNull { this.entitlements[it] }
    }
    return entitlements.filter { it.isActive }.map { it.identifier }.distinct()
}
