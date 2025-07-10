package io.monetize.kit.sdk.ads.native_ad

import java.util.concurrent.atomic.AtomicInteger

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

    fun getNextInterId(placement: String): String? {
        val list = normalizedMap[placement] ?: return null
        if (list.isEmpty()) return null

        val currentIndex = currentIndexMap[placement] ?: 0
        val selectedId = list[currentIndex]

        // Update index for next call
        currentIndexMap[placement] = (currentIndex + 1) % list.size

        return selectedId
    }
}