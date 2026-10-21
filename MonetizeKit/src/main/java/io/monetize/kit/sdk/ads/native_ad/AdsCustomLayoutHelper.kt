package io.monetize.kit.sdk.ads.native_ad

import io.monetize.kit.sdk.core.utils.adtype.NativeAdType

class AdsCustomLayoutHelper private constructor() {


    private val layouts = mutableMapOf<NativeAdType, Int?>()
    private val shimmers = mutableMapOf<NativeAdType, Int?>()

    fun setNativeCustomLayouts(
        largeNativeLayout: Int? = null,
        smallNativeLayout: Int? = null,
        smallNativeMiniLayout: Int? = null,
        smallNativeMediaViewLayout: Int? = null,
        fullScreenNativeLayout: Int? = null,
        largeNativeShimmer: Int? = null,
        smallNativeShimmer: Int? = null,
        smallNativeMiniShimmer: Int? = null,
        smallNativeMediaViewShimmer: Int? = null,
        fullScreenNativeShimmer: Int? = null,
    ) {
        layouts[NativeAdType.LARGE_NATIVE] = largeNativeLayout
        layouts[NativeAdType.SMALL_NATIVE] = smallNativeLayout
        layouts[NativeAdType.SMALL_NATIVE_MINI] = smallNativeMiniLayout
        layouts[NativeAdType.SMALL_NATIVE_MEDIA_VIEW] = smallNativeMediaViewLayout
        layouts[NativeAdType.FULL_NATIVE] = fullScreenNativeLayout

        shimmers[NativeAdType.LARGE_NATIVE] = largeNativeShimmer
        shimmers[NativeAdType.SMALL_NATIVE] = smallNativeShimmer
        shimmers[NativeAdType.SMALL_NATIVE_MINI] = smallNativeMiniShimmer
        shimmers[NativeAdType.SMALL_NATIVE_MEDIA_VIEW] = smallNativeMediaViewShimmer
        shimmers[NativeAdType.FULL_NATIVE] = fullScreenNativeShimmer
    }

    // Layouts for single placement keys, used before the per-type layouts above
    private val placementLayouts = mutableMapOf<String, Map<NativeAdType, Int>>()
    private val placementShimmers = mutableMapOf<String, Map<NativeAdType, Int>>()

    /**
     * Custom layouts for one placement key, so placements of the same NativeAdType can look
     * different (for example two SMALL_NATIVE placements with different designs).
     *
     * Set a layout for each type the placement may get from "{placementKey}_nativeAdType" in
     * Remote Config; for a type without one here, the layout from setNativeCustomLayouts is used,
     * and after that the library default. Call it again for the same key to replace its layouts.
     */
    fun setPlacementCustomLayouts(
        placementKey: String,
        largeNativeLayout: Int? = null,
        smallNativeLayout: Int? = null,
        smallNativeMiniLayout: Int? = null,
        smallNativeMediaViewLayout: Int? = null,
        fullScreenNativeLayout: Int? = null,
        largeNativeShimmer: Int? = null,
        smallNativeShimmer: Int? = null,
        smallNativeMiniShimmer: Int? = null,
        smallNativeMediaViewShimmer: Int? = null,
        fullScreenNativeShimmer: Int? = null,
    ) {
        placementLayouts[placementKey] = nonNullTypes(
            largeNativeLayout, smallNativeLayout, smallNativeMiniLayout,
            smallNativeMediaViewLayout, fullScreenNativeLayout
        )
        placementShimmers[placementKey] = nonNullTypes(
            largeNativeShimmer, smallNativeShimmer, smallNativeMiniShimmer,
            smallNativeMediaViewShimmer, fullScreenNativeShimmer
        )
    }

    // Placement layout first, then the layout for the type
    fun getLayout(type: NativeAdType, placementKey: String? = null): Int? =
        placementKey?.let { placementLayouts[it]?.get(type) } ?: layouts[type]

    // A placement's own shimmer, or its own layout, so the shimmer matches that placement's design
    fun getShimmer(type: NativeAdType, placementKey: String? = null): Int? =
        placementKey?.let { placementShimmers[it]?.get(type) ?: placementLayouts[it]?.get(type) }
            ?: shimmers[type]

    private fun nonNullTypes(
        large: Int?, small: Int?, smallMini: Int?, smallMediaView: Int?, full: Int?,
    ): Map<NativeAdType, Int> = buildMap {
        large?.let { put(NativeAdType.LARGE_NATIVE, it) }
        small?.let { put(NativeAdType.SMALL_NATIVE, it) }
        smallMini?.let { put(NativeAdType.SMALL_NATIVE_MINI, it) }
        smallMediaView?.let { put(NativeAdType.SMALL_NATIVE_MEDIA_VIEW, it) }
        full?.let { put(NativeAdType.FULL_NATIVE, it) }
    }

    companion object {
        @Volatile private var instance: AdsCustomLayoutHelper? = null
        fun getInstance(): AdsCustomLayoutHelper =
            instance ?: synchronized(this) {
                instance ?: AdsCustomLayoutHelper().also { instance = it }
            }
    }
}
