package io.monetize.kit.sdk.core.utils.purchase

import android.app.Activity
import android.app.Application
import io.monetize.kit.sdk.data.impl.BillingRepositoryImpl
import io.monetize.kit.sdk.domain.repo.BillingRepository
import io.monetize.kit.sdk.domain.usecase.InitBillingUseCase
import io.monetize.kit.sdk.domain.usecase.PurchaseProductUseCase

class AdKitPurchaseHelper private constructor(
    private val init: InitBillingUseCase,
    private val purchase: PurchaseProductUseCase,
    private val billingRepository: BillingRepository
) {

    fun initBilling(
        removeAdsIds: List<String>,
        featureIds: List<String>
    ) = init(removeAdsIds = removeAdsIds, featureIds = featureIds)


    val oneTimePurchaseState = init.ucState


    fun purchaseProduct(
        activity: Activity?,
        productId: String,
        onUserDismissedPaywall: (() -> Unit)? = null
    ) =
        purchase(activity, productId, onUserDismissedPaywall)

    fun getBillingPrice(
        productId: String,
    ): String {
        return init.getBillingPrice(productId)
    }

    companion object {
        @Volatile
        private var instance: AdKitPurchaseHelper? = null

        internal fun getInstance(
            context: Application,
        ): AdKitPurchaseHelper {
            val billingRepo = BillingRepositoryImpl.getInstance(
                context.applicationContext,
            )

            return instance ?: synchronized(this) {
                instance ?: AdKitPurchaseHelper(
                    init = InitBillingUseCase.getInstance(billingRepo),
                    purchase = PurchaseProductUseCase.getInstance(billingRepo),
                    billingRepository = billingRepo
                ).also { instance = it }
            }
        }
    }
}