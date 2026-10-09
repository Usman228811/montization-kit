package io.monetize.kit.sdk.data.impl

import com.android.billingclient.api.BillingClient.BillingResponseCode

// Acknowledge failures worth retrying. Google refunds purchases left unacknowledged
// for 3 days; if every retry fails, the next purchase history query acknowledges again.
internal val RETRYABLE_ACKNOWLEDGE_CODES = setOf(
    BillingResponseCode.SERVICE_DISCONNECTED,
    BillingResponseCode.SERVICE_UNAVAILABLE,
    BillingResponseCode.NETWORK_ERROR,
    BillingResponseCode.ERROR,
)

internal const val MAX_ACKNOWLEDGE_ATTEMPTS = 3

internal fun acknowledgeRetryDelayMillis(attempt: Int): Long = 2_000L * attempt
