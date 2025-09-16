package io.monetize.kit.sdk.ads.native_ad

import io.monetize.kit.sdk.core.utils.adtype.AdType

class AdsCustomLayoutHelper private constructor() {

    private var overAllCtaColor = ""
    private var overAllBgColor = ""

    fun setOverAllBgColor(color: String) { overAllBgColor = color }
    fun getOverAllBgColor(): String = overAllBgColor

    fun setOverAllCtaColor(color: String) { overAllCtaColor = color }
    fun getOverAllCtaColor(): String = overAllCtaColor

    private val layouts = mutableMapOf<AdType, Int?>()
    private val shimmers = mutableMapOf<AdType, Int?>()

    fun setNativeCustomLayouts(
        largeNativeLayout: Int? = null,
        smallNativeLayout: Int? = null,
        smallNativeMiniLayout: Int? = null,
        smallNativeMediaViewLayout: Int? = null,
        largeNativeShimmer: Int? = null,
        smallNativeShimmer: Int? = null,
        smallNativeMiniShimmer: Int? = null,
        smallNativeMediaViewShimmer: Int? = null,
        bannerShimmer: Int? = null,
    ) {
        layouts[AdType.LARGE_NATIVE] = largeNativeLayout
        layouts[AdType.SMALL_NATIVE] = smallNativeLayout
        layouts[AdType.SMALL_NATIVE_MINI] = smallNativeMiniLayout
        layouts[AdType.SMALL_NATIVE_MEDIA_VIEW] = smallNativeMediaViewLayout

        shimmers[AdType.LARGE_NATIVE] = largeNativeShimmer
        shimmers[AdType.SMALL_NATIVE] = smallNativeShimmer
        shimmers[AdType.SMALL_NATIVE_MINI] = smallNativeMiniShimmer
        shimmers[AdType.SMALL_NATIVE_MEDIA_VIEW] = smallNativeMediaViewShimmer
        shimmers[AdType.BANNER] = bannerShimmer
    }

    fun getLayout(type: AdType): Int? = layouts[type]
    fun getShimmer(type: AdType): Int? = shimmers[type]

    companion object {
        @Volatile private var instance: AdsCustomLayoutHelper? = null
        fun getInstance(): AdsCustomLayoutHelper =
            instance ?: synchronized(this) {
                instance ?: AdsCustomLayoutHelper().also { instance = it }
            }
    }
}
