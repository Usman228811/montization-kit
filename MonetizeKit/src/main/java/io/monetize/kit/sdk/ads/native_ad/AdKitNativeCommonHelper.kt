package io.monetize.kit.sdk.ads.native_ad

import io.monetize.kit.sdk.ads.interstitial.InterControllerConfig
import java.util.concurrent.atomic.AtomicInteger

class AdKitNativeCommonHelper {

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