package io.monetize.kit.sdk.domain.usecase

import com.revenuecat.purchases.Package
import io.monetize.kit.sdk.core.utils.init.AdKit.adKitPref
import io.monetize.kit.sdk.core.utils.toInApp
import io.monetize.kit.sdk.domain.model.PremiumOffer
import io.monetize.kit.sdk.domain.repo.BillingRepository
import io.monetize.kit.sdk.domain.repo.SubscriptionListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


class InitRCBillingUseCase private constructor(
    private val billingRepository: BillingRepository
) {


    private val _ucState = MutableStateFlow(OneTimePurchaseState())
    val ucState = _ucState.asStateFlow()

    private var removeAdsIds = listOf<String>()

    operator fun invoke(
        removeAdsIds: List<String>,
        featureIds: List<String>
    ) {
        this.removeAdsIds = removeAdsIds
        billingRepository.initBilling(removeAdsIds, featureIds, object : SubscriptionListener {
            override fun onQueryProductSuccess(skuList: Map<String, Any>, productList: List<Any>) {

                productList as List<Package>
                val offers = productList
                    .map { it.toInApp() }

                _ucState.update {
                    it.copy(offers = offers)
                }
                billingRepository.checkProductPurchaseHistory()
            }

            override fun subscriptionItemNotFound() {

            }

            override fun onSubscriptionPurchasedFetched(purchasesList: List<String>) {
                val uniquePurchases = purchasesList.distinct()
                val hasRemoveAds =
                    uniquePurchases.any { it in this@InitRCBillingUseCase.removeAdsIds }

                adKitPref.isLifeTimePurchased = hasRemoveAds
                _ucState.update {
                    it.copy(purchasesList = uniquePurchases)
                }

            }

        })
    }

    companion object {
        @Volatile
        private var instance: InitRCBillingUseCase? = null


        fun getInstance(billingRepository: BillingRepository): InitRCBillingUseCase {

            return instance ?: synchronized(this) {
                instance ?: InitRCBillingUseCase(billingRepository).also { instance = it }
            }
        }

    }
}

//class InitBillingUseCase(private val billingRepository: BillingRepository) {
//    operator fun invoke(productId: String) = billingRepository.initBilling(productId)
//}