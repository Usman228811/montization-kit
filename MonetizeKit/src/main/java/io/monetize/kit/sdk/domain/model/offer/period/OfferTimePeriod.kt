package io.monetize.kit.sdk.domain.model.offer.period
import io.monetize.kit.sdk.domain.model.offer.period.OfferPeriod

sealed interface OfferTimePeriod {

    data object Lifetime : OfferTimePeriod
    data class Timed(val period: OfferPeriod)
}