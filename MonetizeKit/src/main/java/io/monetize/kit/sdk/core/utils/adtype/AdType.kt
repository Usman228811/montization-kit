package io.monetize.kit.sdk.core.utils.adtype

enum class AdType(val type: Int) {
    LARGE_NATIVE(0),
    SMALL_NATIVE_MEDIA_VIEW(1),
    SMALL_NATIVE(2),
    SMALL_NATIVE_MINI(3),
    BANNER(4),
}

data class BannerControllerConfig(
    val placementKey: String,
    val adIdKey: String,

    )

data class CollapsableConfig(
    val isBottom: Boolean = true
)

data class NativeControllerConfig(
    val placementKey: String,
    val adIdKey: String,
    val adType: Int,
    val bgColor: String = "",
    val ctaColor: String = ""
)