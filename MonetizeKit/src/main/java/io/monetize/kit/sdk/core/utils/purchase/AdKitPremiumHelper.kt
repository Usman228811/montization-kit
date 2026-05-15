package io.monetize.kit.sdk.core.utils.purchase

import android.app.Activity
import android.app.Application
import com.revenuecat.purchases.LogLevel
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import io.monetize.kit.sdk.core.utils.init.AdKit
import io.monetize.kit.sdk.domain.model.OfferTexts
import io.monetize.kit.sdk.domain.model.OfferType
import io.monetize.kit.sdk.domain.model.PremiumOffer
import io.monetize.kit.sdk.domain.usecase.OneTimePurchaseState
import io.monetize.kit.sdk.domain.usecase.SubscriptionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class PremiumAccessState(
    val isPremium: Boolean = false,
    val oneTimePurchases: List<String> = emptyList(),
    val subscriptionPurchases: List<String> = emptyList(),
    val allPurchases: List<String> = emptyList(),
    val oneTimeOffers: List<PremiumOffer> = emptyList(),
    val subscriptionOffers: List<PremiumOffer> = emptyList(),
)

sealed class BillingItem {
    data class Lifetime(
        val productId: String,
        val type: Type
    ) : BillingItem()

    data class Subscription(
        val productId: String,
        val type: Type
    ) : BillingItem()

    enum class Type {
        REMOVE_ADS,
        FEATURE
    }
}

enum class PremiumProductType {
    ONE_TIME,
    SUBSCRIPTION,
    UNKNOWN
}

class AdKitPremiumHelper private constructor(
    private val purchaseHelper: AdKitPurchaseHelper,
    private val subscriptionHelper: AdKitSubscriptionHelper
) {


    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val premiumState: StateFlow<PremiumAccessState> = combine(
        purchaseHelper.oneTimePurchaseState,
        subscriptionHelper.subscriptionState
    ) { oneTimeState: OneTimePurchaseState, subscriptionState: SubscriptionState ->
        val oneTimePurchases = oneTimeState.purchasesList.distinct()
        val subscriptionPurchases = subscriptionState.purchasesList.distinct()
        val allPurchases = (oneTimePurchases + subscriptionPurchases).distinct()

        PremiumAccessState(
            isPremium = allPurchases.isNotEmpty(),
            oneTimePurchases = oneTimePurchases,
            subscriptionPurchases = subscriptionPurchases,
            allPurchases = allPurchases,
            oneTimeOffers = oneTimeState.offers,
            subscriptionOffers = subscriptionState.offers
        )
    }.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = PremiumAccessState()
    )

    private val logLevel: LogLevel = LogLevel.DEBUG

    fun initBilling(
        activity: Activity,
        items: List<BillingItem>
    ) {

        if (AdKit.getRevenueCatKey().isNotEmpty()) {
            Purchases.logLevel = logLevel

            val configuration = PurchasesConfiguration.Builder(
                activity, AdKit.getRevenueCatKey()
            ).build()

            Purchases.configure(configuration)
        }


        val lifetimeRemoveAds = mutableListOf<String>()
        val lifetimeFeatures = mutableListOf<String>()
        val subRemoveAds = mutableListOf<String>()
        val subFeatures = mutableListOf<String>()

        items.forEach { item ->
            when (item) {
                is BillingItem.Lifetime -> {
                    when (item.type) {
                        BillingItem.Type.REMOVE_ADS -> lifetimeRemoveAds.add(item.productId)
                        BillingItem.Type.FEATURE -> lifetimeFeatures.add(item.productId)
                    }
                }

                is BillingItem.Subscription -> {
                    when (item.type) {
                        BillingItem.Type.REMOVE_ADS -> subRemoveAds.add(item.productId)
                        BillingItem.Type.FEATURE -> subFeatures.add(item.productId)
                    }
                }
            }
        }

        if (lifetimeRemoveAds.isNotEmpty() || lifetimeFeatures.isNotEmpty()) {
            purchaseHelper.initBilling(
                isForRevenueCat = AdKit.getRevenueCatKey().isNotEmpty(),
                removeAdsIds = lifetimeRemoveAds,
                featureIds = lifetimeFeatures
            )
        }

        if (subRemoveAds.isNotEmpty() || subFeatures.isNotEmpty()) {
            subscriptionHelper.initBilling(
                activity = activity,
                isForRevenueCat = AdKit.getRevenueCatKey().isNotEmpty(),
                removeAdsIds = subRemoveAds,
                featureIds = subFeatures
            )
        }
    }

    fun getProductType(productId: String): PremiumProductType {
        return when {
            premiumState.value.oneTimeOffers.any { it.id == productId } -> PremiumProductType.ONE_TIME
            premiumState.value.subscriptionOffers.any { it.id == productId } -> PremiumProductType.SUBSCRIPTION
            else -> PremiumProductType.UNKNOWN
        }
    }

    fun getBillingPrice(productId: String): OfferTexts {
        return when (getProductType(productId)) {
            PremiumProductType.ONE_TIME -> {
                val oneTimeOffer = premiumState.value.oneTimeOffers
                    .firstOrNull { it.id == productId } as? PremiumOffer.InAppProduct

                OfferTexts(
                    type = OfferType.STRAIGHT,
                    period = null,
                    freeTrialText = null,
                    paidTrialText = null,
                    mainOfferText = oneTimeOffer?.price?.formattedPrice
                )
            }

            PremiumProductType.SUBSCRIPTION -> subscriptionHelper.getBillingPrice(productId)
            PremiumProductType.UNKNOWN -> OfferTexts(
                type = OfferType.STRAIGHT,
                period = null,
                freeTrialText = null,
                paidTrialText = null,
                mainOfferText = null
            )
        }
    }

    fun purchase(
        activity: Activity,
        productId: String?,
        isForUpdatePlan: Boolean = false,
        onUserDismissedPaywall: (() -> Unit)? = null
    ) {
        when (productId?.let(::getProductType)) {
            PremiumProductType.ONE_TIME -> {
                purchaseHelper.purchaseProduct(
                    activity = activity,
                    productId = productId,
                    onUserDismissedPaywall = onUserDismissedPaywall
                )
            }

            PremiumProductType.SUBSCRIPTION -> {
                subscriptionHelper.purchase(
                    activity = activity,
                    productId = productId,
                    isForUpdatePlan = isForUpdatePlan,
                    onUserDismissedPaywall = onUserDismissedPaywall
                )
            }

            PremiumProductType.UNKNOWN, null -> Unit
        }
    }

    fun isSubscriptionUpdateSupported(): Boolean =
        subscriptionHelper.isSubscriptionUpdateSupported()

    fun getOfferType(productId: String): OfferType = getBillingPrice(productId).type

    companion object {
        @Volatile
        private var instance: AdKitPremiumHelper? = null

        internal fun getInstance(context: Application): AdKitPremiumHelper {
            return instance ?: synchronized(this) {
                instance ?: AdKitPremiumHelper(
                    purchaseHelper = AdKitPurchaseHelper.getInstance(context),
                    subscriptionHelper = AdKitSubscriptionHelper.getInstance(context)
                ).also { instance = it }
            }
        }
    }
}
