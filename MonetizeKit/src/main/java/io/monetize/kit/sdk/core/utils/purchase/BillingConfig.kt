package io.monetize.kit.sdk.core.utils.purchase

class BillingConfig {

    internal val lifetimeRemoveAds = mutableListOf<String>()
    internal val lifetimeFeatures = mutableListOf<String>()
    internal val subscriptionRemoveAds = mutableListOf<String>()
    internal val subscriptionFeatures = mutableListOf<String>()

    fun lifetime(block: LifetimeScope.() -> Unit) {
        val scope = LifetimeScope()
        scope.block()

        lifetimeRemoveAds.addAll(scope.removeAdsIds)
        lifetimeFeatures.addAll(scope.featureIds)
    }

    fun subscription(block: SubscriptionScope.() -> Unit) {
        val scope = SubscriptionScope()
        scope.block()

        subscriptionRemoveAds.addAll(scope.removeAdsIds)
        subscriptionFeatures.addAll(scope.featureIds)
    }
}

class LifetimeScope {
    internal val removeAdsIds = mutableListOf<String>()
    internal val featureIds = mutableListOf<String>()

    fun removeAds(productId: String) {
        removeAdsIds.add(productId)
    }

    fun feature(productId: String) {
        featureIds.add(productId)
    }
}

class SubscriptionScope {
    internal val removeAdsIds = mutableListOf<String>()
    internal val featureIds = mutableListOf<String>()

    fun removeAds(productId: String) {
        removeAdsIds.add(productId)
    }

    fun feature(productId: String) {
        featureIds.add(productId)
    }
}