package io.monetize.kit.sdk.domain.usecase

import io.monetize.kit.sdk.domain.model.OfferTexts
import io.monetize.kit.sdk.domain.model.OfferType
import io.monetize.kit.sdk.domain.model.PremiumOffer
import io.monetize.kit.sdk.domain.model.offer.period.Period
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

internal fun List<String>.distinctPurchases(): List<String> = distinct()

internal fun MutableStateFlow<OneTimePurchaseState>.updateOneTimeOffers(
    offers: List<PremiumOffer>
) {
    update { it.copy(offers = offers) }
}

internal fun MutableStateFlow<OneTimePurchaseState>.updateOneTimePurchases(
    purchases: List<String>
) {
    update { it.copy(purchasesList = purchases.distinctPurchases()) }
}

internal fun MutableStateFlow<SubscriptionState>.updateSubscriptionOffers(
    offers: List<PremiumOffer>
) {
    update { it.copy(offers = offers) }
}

internal fun MutableStateFlow<SubscriptionState>.updateSubscriptionPurchases(
    purchases: List<String>
) {
    update { it.copy(purchasesList = purchases.distinctPurchases()) }
}

internal fun buildSubscriptionOfferTexts(
    offers: List<PremiumOffer>,
    offerId: String
): OfferTexts {
    if (offers.isEmpty()) {
        return OfferTexts(OfferType.STRAIGHT, null, null, null, null)
    }

    val offer = offers.firstOrNull { it.id == offerId } as? PremiumOffer.Subscription
        ?: return OfferTexts(OfferType.STRAIGHT, null, null, null, null)

    val periodMap = mapOf(
        Period.DAY to "day",
        Period.WEEK to "week",
        Period.MONTH to "month",
        Period.YEAR to "year",
    )

    val type = when {
        offer.trialPhase != null -> OfferType.FREE_TRIAL
        offer.paidPhases.size > 1 -> OfferType.PAID_TRIAL
        else -> OfferType.STRAIGHT
    }

    val freeTrialText = offer.trialPhase?.let { trial ->
        "${trial.period.count}-${periodMap[trial.period.period]} FREE Trial"
    }

    val paidTrialText = if (type == OfferType.PAID_TRIAL) {
        val firstPhase = offer.paidPhases.first()
        "${firstPhase.price.formattedPrice} for ${firstPhase.period.count}-${periodMap[firstPhase.period.period]}"
    } else {
        null
    }

    val mainOfferText = offer.paidPhases.lastOrNull()?.price?.formattedPrice
    val period = offer.paidPhases.lastOrNull()?.let { last ->
        periodMap[last.period.period]
    }

    return OfferTexts(
        type = type,
        period = period,
        freeTrialText = freeTrialText,
        paidTrialText = paidTrialText,
        mainOfferText = mainOfferText
    )
}
