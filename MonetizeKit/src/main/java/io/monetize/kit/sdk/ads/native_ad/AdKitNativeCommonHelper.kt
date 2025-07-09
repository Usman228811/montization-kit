package io.monetize.kit.sdk.ads.native_ad

import android.content.Context
import java.util.concurrent.atomic.AtomicInteger

class AdKitNativeCommonHelper private constructor(){

    companion object {
        @Volatile
        private var instance: AdKitNativeCommonHelper? = null


        internal  fun getInstance(
        ): AdKitNativeCommonHelper {
            return instance ?: synchronized(this) {
                instance ?: AdKitNativeCommonHelper().also { instance = it }
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