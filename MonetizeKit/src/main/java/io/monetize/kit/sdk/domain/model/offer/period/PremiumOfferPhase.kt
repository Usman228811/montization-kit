package io.monetize.kit.sdk.domain.model.offer.period
import io.monetize.kit.sdk.domain.model.offer.price.OfferPrice

data class PremiumOfferPhase(
    val price: OfferPrice,
    val period: OfferPeriod
)