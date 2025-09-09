package io.monetize.kit.sdk.core.utils.adtype

import androidx.annotation.Keep


@Keep
data class NativeAdConfig(
    val enabled: Boolean = true,
    val loadNewAd: Boolean = false,
    val ad_unit_id: String ="",
    val ad_type: Int,
    val cta_color: String,
    val bg_color: String
)

@Keep
data class BannerAdConfig(
    val enabled: Boolean,
    val ad_unit_id: String,
    val size: String
)

@Keep
data class AppOpenAdConfig(
    val enabled: Boolean,
    val ad_unit_id: String
)

@Keep
data class AdsConfig(
    val native_ads: Map<String, NativeAdConfig>,
    val banner_ads: Map<String, BannerAdConfig>,
    val app_open_ads: Map<String, AppOpenAdConfig>
)