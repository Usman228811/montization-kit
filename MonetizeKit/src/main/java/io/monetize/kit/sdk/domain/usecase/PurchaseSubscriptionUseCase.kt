package io.monetize.kit.sdk.domain.usecase

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.ProductDetails
import com.revenuecat.purchases.Package
import io.monetize.kit.sdk.data.impl.PlaySubscriptionRepositoryImpl
import io.monetize.kit.sdk.data.impl.RCSubscriptionRepositoryImpl
import io.monetize.kit.sdk.domain.repo.SubscriptionRepository


class PurchaseSubscriptionUseCase private constructor(
    private val repositoryPlay: SubscriptionRepository,
    private val repositoryRc: SubscriptionRepository,
) {

    companion object {
        @Volatile
        private var instance: PurchaseSubscriptionUseCase? = null


        fun getInstance(
            context: Context
        ): PurchaseSubscriptionUseCase {
            val repoPlay = PlaySubscriptionRepositoryImpl.getInstance(context)
            val repoRC = RCSubscriptionRepositoryImpl.getInstance(context)
            return instance ?: synchronized(this) {
                instance ?: PurchaseSubscriptionUseCase(
                    repoPlay, repoRC
                ).also { instance = it }
            }
        }
    }


    fun purchasePlayProduct(
        activity: Activity,
        product: ProductDetails,
        onUserDismissedPaywall: (() -> Unit)? = null
    ) {
        repositoryPlay.purchaseProduct(activity, product, onUserDismissedPaywall)
    }

    fun purchaseRcProduct(
        activity: Activity,
        product: Package,
        onUserDismissedPaywall: (() -> Unit)? = null
    ) {
        repositoryRc.purchaseProduct(activity, product, onUserDismissedPaywall)
    }

    fun changeSubscriptionPlan(activity: Activity, product: ProductDetails) {
        repositoryPlay.changeSubscriptionPlan(activity, product)
    }

    fun viewUrl(activity: Activity, url: String) {
        repositoryPlay.viewUrl(activity, url)
    }

}