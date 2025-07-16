package io.monetize.kit.sdk.core.utils.adtype

enum class AdType(val type: Int) {
    LARGE_NATIVE(0),
    JAZZ_LEFT_BOTTOM_CTA(1),
    SMALL_BOTTOM_BUTTON(2),
    BANNER(3),
}

data class BannerControllerConfig constructor(
    val placementKey: String,
    val adIdKey: String,

)

data class CollapsableConfig(
    val isBottom: Boolean = true
)

data class NativeControllerConfig(
    val placementKey: String,
    val adIdKey: String,
    val adType:Int
)