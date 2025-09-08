package io.monetize.kit.sdk.ads.native_ad

class AdsCustomLayoutHelper private constructor() {

    private var overAllCtaColor = ""
    private var overAllBgColor = ""

    fun setOverAllBgColor(color: String) {

    }
    fun getOverAllBgColor(): String {
        return overAllBgColor
    }

    fun setOverAllCtaColor(color: String) {
        overAllCtaColor = color
    }

    fun getOverAllCtaColor(): String {
        return overAllCtaColor
    }

    companion object {
        @Volatile
        private var instance: AdsCustomLayoutHelper? = null


        internal fun getInstance(
        ): AdsCustomLayoutHelper {
            return instance ?: synchronized(this) {
                instance ?: AdsCustomLayoutHelper(
                ).also { instance = it }
            }
        }
    }


    private var bigNative: Int? = null
    private var smallNative: Int? = null
    private var smallNativeBanner: Int? = null
    private var splitNative: Int? = null
    private var splitNativeShimmer: Int? = null
    private var bigNativeShimmer: Int? = null
    private var smallNativeShimmer: Int? = null

    private var smallNativeBannerShimmer: Int? = null
    private var bannerShimmer: Int? = null


    fun setNativeCustomLayouts(
        bigNativeLayout: Int? = null,
        smallNativeLayout: Int? = null,
        smallNativeBannerLayout: Int? = null,
        splitNativeLayout: Int? = null,
        bigNativeShimmer: Int? = null,
        smallNativeShimmer: Int? = null,
        smallNativeBannerShimmer: Int? = null,
        splitNativeShimmer: Int? = null,
        bannerShimmer: Int? = null,
    ) {
        setBigNative(
            bigNative = bigNativeLayout,
            bigNativeShimmer = bigNativeShimmer
        )
        setSmallNativeBanner(
            smallNativeBanner = smallNativeBannerLayout,
            smallNativeBannerShimmer = smallNativeBannerShimmer
        )
        setSmallNative(
            smallNative = smallNativeLayout,
            smallNativeShimmer = smallNativeShimmer
        )
        setSplitNative(
            splitNative = splitNativeLayout,
            splitNativeShimmer = splitNativeShimmer
        )
        this.bannerShimmer = bannerShimmer
    }

    private fun setBigNative(bigNative: Int?, bigNativeShimmer: Int?) {
        this.bigNative = bigNative
        this.bigNativeShimmer = bigNativeShimmer
    }
    private fun setSmallNativeBanner(smallNativeBanner: Int?, smallNativeBannerShimmer: Int?) {
        this.smallNativeBanner = smallNativeBanner
        this.smallNativeBannerShimmer = smallNativeBannerShimmer
    }

    fun getBigNative(): Int? {
        return bigNative
    }

    fun getBigNativeShimmer(): Int? {
        return bigNativeShimmer
    }

    private fun setSmallNative(smallNative: Int?, smallNativeShimmer: Int?) {
        this.smallNative = smallNative
        this.smallNativeShimmer = smallNativeShimmer
    }


    fun getSmallNative(): Int? {
        return smallNative
    }

    fun getSmallNativeBanner(): Int? {
        return smallNativeBanner
    }

    fun getSmallNativeShimmer(): Int? {
        return smallNativeShimmer
    }
    fun getSmallNativeBannerShimmer(): Int? {
        return smallNativeBannerShimmer
    }
    fun getBannerShimmer(): Int? {
        return bannerShimmer
    }


    private fun setSplitNative(splitNative: Int?, splitNativeShimmer: Int?) {
        this.splitNative = splitNative
        this.splitNativeShimmer = splitNativeShimmer
    }

    fun getSplitNative(): Int? {
        return splitNative
    }

    fun getSplitNativeShimmer(): Int? {
        return splitNativeShimmer
    }

}