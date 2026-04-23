package io.monetize.kit.sdk.domain.usecase

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.ProductDetails
import io.monetize.kit.sdk.core.utils.init.AdKit.adKitPref
import io.monetize.kit.sdk.core.utils.toSubscription
import io.monetize.kit.sdk.data.impl.SubscriptionRepositoryImpl
import io.monetize.kit.sdk.domain.model.OfferTexts
import io.monetize.kit.sdk.domain.model.OfferType
import io.monetize.kit.sdk.domain.model.PremiumOffer
import io.monetize.kit.sdk.domain.model.offer.period.Period
import io.monetize.kit.sdk.domain.repo.SubscriptionListener
import io.monetize.kit.sdk.domain.repo.SubscriptionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


data class SubscriptionState(
    val purchasesList: List<String> = emptyList(),
    val offers: List<PremiumOffer> = emptyList()
)


class QuerySubscriptionProductsUseCase private constructor(
    private val repository: SubscriptionRepository
) {

    companion object {

        @Volatile
        private var instance: QuerySubscriptionProductsUseCase? = null


        fun getInstance(
            context: Context
        ): QuerySubscriptionProductsUseCase {
            val repo = SubscriptionRepositoryImpl.getInstance(context)
            return instance ?: synchronized(this) {
                instance ?: QuerySubscriptionProductsUseCase(
                    repo
                ).also { instance = it }
            }
        }
    }

    var productsMap: Map<String, ProductDetails>? = null

    private val _ucState = MutableStateFlow(SubscriptionState())
    val ucState = _ucState.asStateFlow()

    fun getProducts(): Map<String, ProductDetails>? {
        return productsMap
    }

    private var removeAdsIds = listOf<String>()
    private var featureIds = listOf<String>()

    operator fun invoke(
        activity: Activity,
        removeAdsIds: List<String>,
        featureIds: List<String>
    ) {
        this.removeAdsIds = removeAdsIds
        repository.setBillingListener(
            activity = activity,
            removeAdsIds = removeAdsIds,
            featureIds = featureIds,
            object : SubscriptionListener {


                override fun onQueryProductSuccess(
                    skuList: Map<String, ProductDetails>,
                    productList: List<ProductDetails>
                ) {

                    productsMap = skuList

                    val offers = productList
                        .mapNotNull { it.toSubscription() }

                    _ucState.update {
                        it.copy(offers = offers)
                    }

                    repository.querySubscriptionHistory(activity)
                }

                override fun onSubscriptionPurchasedFetched(purchasesList: List<String>) {

                    val uniquePurchases = purchasesList.distinct()

                    val hasRemoveAds = uniquePurchases.any { it in this@QuerySubscriptionProductsUseCase.removeAdsIds }

                    adKitPref.isAppSubscribed = hasRemoveAds

                    _ucState.update {
                        it.copy(purchasesList = uniquePurchases)
                    }
                }

                override fun subscriptionItemNotFound() {}
            }
        )
    }


    fun isSubscriptionUpdateSupported() = repository.isSubscriptionUpdateSupported()


    fun buildOfferTexts(
        offerId: String
    ): OfferTexts {

        val offers = ucState.value.offers

        if (offers.isEmpty()) {
            return OfferTexts(OfferType.STRAIGHT, null, null, null, null)
        }

        val myOffer = offers.firstOrNull { it.id == offerId }
        val offer = myOffer as? PremiumOffer.Subscription
            ?: return OfferTexts(OfferType.STRAIGHT, null, null, null, null)

        val periodMap = mapOf(
            Period.DAY to "day",
            Period.WEEK to "week",
            Period.MONTH to "month",
            Period.YEAR to "year",
        )

        // 🔹 Detect Type
        val type = when {
            offer.trialPhase != null -> OfferType.FREE_TRIAL
            offer.paidPhases.size > 1 -> OfferType.PAID_TRIAL
            else -> OfferType.STRAIGHT
        }

        // 🔹 Free Trial
        val freeTrialText = offer.trialPhase?.let { trial ->
            "${trial.period.count}-${periodMap[trial.period.period]} FREE Trial"
        }

        // 🔹 Paid Trial
        val paidTrialText =
            if (type == OfferType.PAID_TRIAL) {
                val firstPhase = offer.paidPhases.first()
                "${firstPhase.price.formattedPrice} for ${firstPhase.period.count}-${periodMap[firstPhase.period.period]}"
            } else null

        // 🔹 Main Offer
        val mainOfferText = offer.paidPhases.lastOrNull()?.price?.formattedPrice
        // 🔹 Main Offer
        val mPeriod = offer.paidPhases.lastOrNull()?.let { last ->
            "${periodMap[last.period.period]}"
        }

        return OfferTexts(
            type = type,
            period = mPeriod,
            freeTrialText = freeTrialText,
            paidTrialText = paidTrialText,
            mainOfferText = mainOfferText
        )
    }

}