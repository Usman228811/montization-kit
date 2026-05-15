package io.monetize.kit.sdk.domain.usecase

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.ProductDetails
import io.monetize.kit.sdk.core.utils.init.AdKit.adKitPref
import io.monetize.kit.sdk.core.utils.toSubscription
import io.monetize.kit.sdk.data.impl.PlaySubscriptionRepositoryImpl
import io.monetize.kit.sdk.domain.model.OfferTexts
import io.monetize.kit.sdk.domain.model.PremiumOffer
import io.monetize.kit.sdk.domain.repo.BillingQueryResult
import io.monetize.kit.sdk.domain.repo.PlayBillingQueryResult
import io.monetize.kit.sdk.domain.repo.SubscriptionListener
import io.monetize.kit.sdk.domain.repo.SubscriptionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

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

        fun getInstance(context: Context): QuerySubscriptionProductsUseCase {
            val repo = PlaySubscriptionRepositoryImpl.getInstance(context)
            return instance ?: synchronized(this) {
                instance ?: QuerySubscriptionProductsUseCase(repo).also { instance = it }
            }
        }
    }

    private var productsMap: Map<String, ProductDetails>? = null

    private val _ucState = MutableStateFlow(SubscriptionState())
    val ucState = _ucState.asStateFlow()

    fun getProducts(): Map<String, ProductDetails>? = productsMap

    private var removeAdsIds = emptyList<String>()

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
            listener = object : SubscriptionListener {
                override fun onQueryProductSuccess(result: BillingQueryResult) {
                    val playResult = result as? PlayBillingQueryResult ?: return
                    productsMap = playResult.skuList
                    val offers = playResult.productList.mapNotNull { it.toSubscription() }
                    _ucState.updateSubscriptionOffers(offers)
                    repository.querySubscriptionHistory(activity)
                }

                override fun onSubscriptionPurchasedFetched(purchasesList: List<String>) {
                    val uniquePurchases = purchasesList.distinctPurchases()
                    adKitPref.isAppSubscribed = uniquePurchases.any { it in this@QuerySubscriptionProductsUseCase.removeAdsIds }
                    _ucState.updateSubscriptionPurchases(uniquePurchases)
                }

                override fun subscriptionItemNotFound() = Unit
            }
        )
    }

    fun isSubscriptionUpdateSupported() = repository.isSubscriptionUpdateSupported()

    fun buildOfferTexts(offerId: String): OfferTexts {
        return buildSubscriptionOfferTexts(ucState.value.offers, offerId)
    }
}
