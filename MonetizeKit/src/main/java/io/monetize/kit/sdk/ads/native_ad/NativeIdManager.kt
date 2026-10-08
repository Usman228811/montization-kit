package io.monetize.kit.sdk.ads.native_ad

import android.util.Log
import io.monetize.kit.sdk.core.utils.remoteAdIds

class NativeIdManager private constructor() {


    companion object {
        @Volatile
        private var instance: NativeIdManager? = null


        internal fun getInstance(
        ): NativeIdManager {
            return instance ?: synchronized(this) {
                instance ?: NativeIdManager().also { instance = it }
            }
        }
    }

    private val normalizedMap: MutableMap<String, List<String>> = mutableMapOf()

    private val currentIndexMap: MutableMap<String, Int> = mutableMapOf()

    fun setNativeIds(map: Map<String, Any>) {
        normalizedMap.clear()
        currentIndexMap.clear()

        map.forEach { (placement, value) ->
            val list = when (value) {
                is String -> listOf(value)
                is List<*> -> value.filterIsInstance<String>()
                else -> emptyList()
            }
            normalizedMap[placement] = list
        }
    }

    fun getNextNativeId(placement: String): String? {
        // Remote config "{placement}_adId" overrides the IDs passed to AdKit.init
        val list = remoteAdIds(placement).ifEmpty { normalizedMap[placement].orEmpty() }
        if (list.isEmpty()) return null

        // Modulo keeps the index valid if a remote config update shrinks the list
        val currentIndex = (currentIndexMap[placement] ?: 0) % list.size
        val selectedId = list[currentIndex]

        // Update index for next call
        currentIndexMap[placement] = (currentIndex + 1) % list.size
        return selectedId
    }
}