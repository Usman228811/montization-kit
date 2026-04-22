package io.monetize.kit.sdk.domain.usecase

import com.android.billingclient.api.ProductDetails
import io.monetize.kit.sdk.core.utils.init.AdKit
import io.monetize.kit.sdk.core.utils.toInApp
import io.monetize.kit.sdk.core.utils.toSubscription
import io.monetize.kit.sdk.domain.model.OfferTexts
import io.monetize.kit.sdk.domain.model.OfferType
import io.monetize.kit.sdk.domain.model.PremiumOffer
import io.monetize.kit.sdk.domain.repo.BillingRepository
import io.monetize.kit.sdk.domain.repo.SubscriptionListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class OneTimePurchaseState(
    val purchasesList: List<String> = emptyList(),
    val offers: List<PremiumOffer> = emptyList(),
)
class InitBillingUseCase private constructor(
    private val billingRepository: BillingRepository
) {


    private val _ucState = MutableStateFlow(OneTimePurchaseState())
    val ucState = _ucState.asStateFlow()

    operator fun invoke(productId: String,) {
        billingRepository.initBilling(productId, object : SubscriptionListener{
            override fun onQueryProductSuccess(
                skuList: Map<String, ProductDetails>,
                productList: List<ProductDetails>
            ) {
                val offers = productList
                    .mapNotNull { it.toInApp() }

                _ucState.update {
                    it.copy(offers = offers)
                }
                billingRepository.checkProductPurchaseHistory()
            }

            override fun subscriptionItemNotFound() {

            }

            override fun onSubscriptionPurchasedFetched(purchasesList: List<String>) {
                val uniquePurchases = purchasesList.distinct()
                AdKit.adKitPref.isLifeTimePurchased = uniquePurchases.isNotEmpty()
                _ucState.update {
                    it.copy(purchasesList = uniquePurchases)
                }

            }

        })
    }

    fun getBillingPrice(
        productId: String,
    ): String {
        val offers = ucState.value.offers

        if (offers.isEmpty()) {
            return ""
        }

        val myOffer = offers.firstOrNull { it.id == productId }
        val offer = myOffer as? PremiumOffer.InAppProduct
        return offer?.price?.formattedPrice ?: ""
    }

    companion object {
        @Volatile
        private var instance: InitBillingUseCase? = null


        fun getInstance(billingRepository: BillingRepository): InitBillingUseCase {

            return instance ?: synchronized(this) {
                instance ?: InitBillingUseCase(billingRepository).also { instance = it }
            }
        }

    }
}

//class InitBillingUseCase(private val billingRepository: BillingRepository) {
//    operator fun invoke(productId: String) = billingRepository.initBilling(productId)
//}