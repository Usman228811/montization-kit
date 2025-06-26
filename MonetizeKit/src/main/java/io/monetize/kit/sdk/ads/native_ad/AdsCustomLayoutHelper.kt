package io.monetize.kit.sdk.ads.native_ad

class AdsCustomLayoutHelper {


    private var bigNative: Int? = null
    private var smallNative: Int? = null
    private var splitNative: Int? = null

    fun setBigNative(bigNative: Int?) {
        this.bigNative = bigNative
    }

    fun getBigNative() :Int?{
        return bigNative
    }

    fun setSmallNative(smallNative: Int?) {
        this.smallNative = smallNative
    }


    fun getSmallNative() :Int?{
        return smallNative
    }



    fun setSplitNative(splitNative: Int?) {
        this.splitNative = splitNative
    }
    fun getSplitNative() :Int?{
        return splitNative
    }

}