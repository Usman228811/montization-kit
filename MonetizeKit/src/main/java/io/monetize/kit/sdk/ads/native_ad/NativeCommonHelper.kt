package io.monetize.kit.sdk.ads.native_ad

import java.util.concurrent.atomic.AtomicInteger

class NativeCommonHelper private constructor() {


    companion object {
        @Volatile
        private var instance: NativeCommonHelper? = null


        internal fun getInstance(
        ): NativeCommonHelper {
            return instance ?: synchronized(this) {
                instance ?: NativeCommonHelper().also { instance = it }
            }
        }
    }

    private val adIdIndex = AtomicInteger(0)
    private var idsList: List<String> = emptyList()

    fun setNativeAdIds(ids: List<String>?) {
        ids?.let {

            idsList = it
            adIdIndex.set(0)
        }
    }

    fun getNativeAdId(): String {
        if (idsList.isEmpty()) throw IllegalStateException("Ad IDs not set. Call setAdIds() first.")
        val index = adIdIndex.getAndUpdate { (it + 1) % idsList.size }
        return idsList[index]
    }
}