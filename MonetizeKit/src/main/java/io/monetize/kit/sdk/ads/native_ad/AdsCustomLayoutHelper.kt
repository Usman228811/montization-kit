package io.monetize.kit.sdk.ads.native_ad

class AdsCustomLayoutHelper {


    private var bigNative: Int? = null
    private var smallNative: Int? = null
    private var splitNative: Int? = null
    private var splitNativeShimmer: Int? = null
    private var bigNativeShimmer: Int? = null
    private var smallNativeShimmer: Int? = null

    fun setBigNative(bigNative: Int?, bigNativeShimmer: Int?) {
        this.bigNative = bigNative
        this.bigNativeShimmer = bigNativeShimmer
    }

    fun getBigNative(): Int? {
        return bigNative
    }

    fun getBigNativeShimmer(): Int? {
        return bigNativeShimmer
    }

    fun setSmallNative(smallNative: Int?, smallNativeShimmer: Int?) {
        this.smallNative = smallNative
        this.smallNativeShimmer = smallNativeShimmer
    }


    fun getSmallNative(): Int? {
        return smallNative
    }

    fun getSmallNativeShimmer(): Int? {
        return smallNativeShimmer
    }


    fun setSplitNative(splitNative: Int?, splitNativeShimmer: Int?) {
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