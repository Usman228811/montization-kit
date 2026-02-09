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

    fun getLayout(type: NativeAdType): Int? = layouts[type]
    fun getShimmer(type: NativeAdType): Int? = shimmers[type]

    companion object {
        @Volatile private var instance: AdsCustomLayoutHelper? = null
        fun getInstance(): AdsCustomLayoutHelper =
            instance ?: synchronized(this) {
                instance ?: AdsCustomLayoutHelper().also { instance = it }
            }
    }
}
