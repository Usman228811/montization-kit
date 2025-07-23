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
    private var splitNative: Int? = null
    private var splitNativeShimmer: Int? = null
    private var bigNativeShimmer: Int? = null
    private var smallNativeShimmer: Int? = null


    fun setNativeCustomLayouts(
        bigNativeLayout: Int? = null,
        smallNativeLayout: Int? = null,
        splitNativeLayout: Int? = null,
        bigNativeShimmer: Int? = null,
        smallNativeShimmer: Int? = null,
        splitNativeShimmer: Int? = null,
    ) {
        setBigNative(
            bigNative = bigNativeLayout,
            bigNativeShimmer = bigNativeShimmer
        )
        setSmallNative(
            smallNative = smallNativeLayout,
            smallNativeShimmer = smallNativeShimmer
        )
        setSplitNative(
            splitNative = splitNativeLayout,
            splitNativeShimmer = splitNativeShimmer
        )
    }

    private fun setBigNative(bigNative: Int?, bigNativeShimmer: Int?) {
        this.bigNative = bigNative
        this.bigNativeShimmer = bigNativeShimmer
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

    fun getSmallNativeShimmer(): Int? {
        return smallNativeShimmer
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