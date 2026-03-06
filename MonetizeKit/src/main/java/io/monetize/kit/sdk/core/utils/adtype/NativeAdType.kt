package io.monetize.kit.sdk.core.utils.adtype

enum class NativeAdType {
    LARGE_NATIVE,
    SMALL_NATIVE_MEDIA_VIEW,
    SMALL_NATIVE,
    SMALL_NATIVE_MINI,
    FULL_NATIVE,
}

enum class BannerAdType {
    ADAPTIVE_BANNER,
    LARGE_BANNER,
    MEDIUM_RECTANGLE_BANNER,
    BOTTOM_COLLAPSIBLE_BANNER,
    TOP_COLLAPSIBLE_BANNER,
}

data class BannerControllerConfig(
    val placementKey: String,
    val adIdKey: String
)

data class CollapsableConfig(
    val isBottom: Boolean = true
)

data class NativeControllerConfig(
    val placementKey: String,
    val adIdKey: String,
    val consumeAnyAd: Boolean = false,
    val loadNextAd: Boolean = true
)