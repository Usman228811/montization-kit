package io.monetize.kit.sdk.core.utils.adtype

enum class AdType(val type: Int) {
    LARGE_NATIVE(0),
    JAZZ_LEFT_BOTTOM_CTA(1),
    SMALL_BOTTOM_BUTTON(2),
    BANNER(3),
}

data class BannerControllerConfig(
    val key: String = "",
    val adId: String = "",
    val isAdEnable: Boolean = false,
    val collapsableConfig: CollapsableConfig? = null,

)

data class CollapsableConfig(
    val isBottom:Boolean = true
)

data class NativeControllerConfig(
    val key: String = "",
    val adId: String = "",
    val isAdEnable: Boolean = false,
    val adType: Long = 2L,
    val loadNewAd:Boolean = false
)