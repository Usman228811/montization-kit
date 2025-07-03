package io.monetize.kit.sdk.core.utils.purchase

import android.app.Activity
import android.content.Context
import io.monetize.kit.sdk.core.utils.AdKitInternetController
import io.monetize.kit.sdk.core.utils.AdKitPref
import io.monetize.kit.sdk.data.impl.BillingRepositoryImpl
import io.monetize.kit.sdk.domain.repo.BillingRepository
import io.monetize.kit.sdk.domain.usecase.InitBillingUseCase
import io.monetize.kit.sdk.domain.usecase.PurchaseProductUseCase

class AdKitPurchaseHelper private constructor(
    private val init: InitBillingUseCase,
    private val purchase: PurchaseProductUseCase,
    private val billingRepository: BillingRepository
) {

    val productPriceFlow = billingRepository.productPriceFlow()
    val appPurchased = billingRepository.appPurchased()

    fun initBilling(productId: String) = init(productId)

    fun purchaseProduct(activity: Activity?) = purchase(activity)

    companion object {
        @Volatile
        private var instance: AdKitPurchaseHelper? = null

        fun getInstance(
            context: Context,
        ): AdKitPurchaseHelper {
            val billingRepo = BillingRepositoryImpl.getInstance(
                context,
                AdKitInternetController.getInstance(context),
                AdKitPref.getInstance(context)
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

//class AdKitPurchaseHelper(
//    private val init: InitBillingUseCase,
//    private val purchase: PurchaseProductUseCase,
//    private val billingRepository: BillingRepository
//) {
//
//    val productPriceFlow = billingRepository.productPriceFlow()
//    val appPurchased = billingRepository.appPurchased()
//
//    fun initBilling(productId: String) = init(productId)
//
//    fun purchaseProduct(activity: Activity?) = purchase(activity)
//}