package io.monetize.kit.sdk.ads.interstitial

import java.util.concurrent.atomic.AtomicInteger

class AdKitInterCommonHelper private constructor() {

    companion object {
        @Volatile
        private var instance: AdKitInterCommonHelper? = null


        internal fun getInstance(
        ): AdKitInterCommonHelper {
            return instance ?: synchronized(this) {
                instance ?: AdKitInterCommonHelper().also { instance = it }
            }
        }
    }

    private val adIdIndex = AtomicInteger(0)
    private var idsList: List<String> = emptyList()

    fun setInterCommonAdIds(ids: List<String>?) {
        ids?.let {
            idsList = it
            adIdIndex.set(0)
        }
    }
//
    fun getInterCommonAdId(): String {
        if (idsList.isEmpty()) throw IllegalStateException("Ad IDs not set. Call setAdIds() first.")
        val index = adIdIndex.getAndUpdate { (it + 1) % idsList.size }
        return idsList[index]
    }
}