package io.monetize.kit.sdk.ads.native_ad

class NativeHelper private constructor() {

    private var ids: Map<String, List<String>>? = null


    companion object {
        @Volatile
        private var instance: NativeHelper? = null


        internal fun getInstance(
        ): NativeHelper {
            return instance ?: synchronized(this) {
                instance ?: NativeHelper().also { instance = it }
            }
        }
    }


    fun setNativeAdIds(map: Map<String, List<String>>) {
        ids = map
    }

    fun getAdIds(): Map<String, List<String>>? {
        return ids
    }

}