package io.monetize.kit.sdk.ads.interstitial

import android.util.Log
import io.monetize.kit.sdk.core.utils.remoteAdIds
class InterIdManager private constructor() {

    companion object {
        @Volatile
        private var instance: InterIdManager? = null


        internal fun getInstance(
        ): InterIdManager {
            return instance ?: synchronized(this) {
                instance ?: InterIdManager().also { instance = it }
            }
        }
    }

    private val normalizedMap: MutableMap<String, List<String>> = mutableMapOf()

    private val currentIndexMap: MutableMap<String, Int> = mutableMapOf()

    fun setInterIds(map: Map<String, Any>) {
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

    // Call this to get the next interstitial ID for a placement
    fun getNextInterId(placement: String): String? {
        // Remote config "{placement}_adId" overrides the IDs passed to AdKit.init
        val list = remoteAdIds(placement).ifEmpty { normalizedMap[placement].orEmpty() }
        if (list.isEmpty()) return null

        // Modulo keeps the index valid if a remote config update shrinks the list
        val currentIndex = (currentIndexMap[placement] ?: 0) % list.size
        val selectedId = list[currentIndex]

        // Update index for next call
        currentIndexMap[placement] = (currentIndex + 1) % list.size

        Log.d("AdKit_Ad_Id", "${placement}_selectedId: $selectedId")
        return selectedId
    }
}