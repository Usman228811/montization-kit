package io.monetize.kit.sdk.ads.rewarded

import android.util.Log
import io.monetize.kit.sdk.core.utils.remoteAdIds
class RewardAdIdManager private constructor() {

    companion object {
        @Volatile
        private var instance: RewardAdIdManager? = null


        internal fun getInstance(
        ): RewardAdIdManager {
            return instance ?: synchronized(this) {
                instance ?: RewardAdIdManager().also { instance = it }
            }
        }
    }

    private val normalizedMap: MutableMap<String, List<String>> = mutableMapOf()

    private val currentIndexMap: MutableMap<String, Int> = mutableMapOf()

    fun setRewardAdIds(map: Map<String, Any>) {
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
    fun getNextRewardId(placement: String): String? {
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