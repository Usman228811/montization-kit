package io.monetize.kit.sdk.ads.interstitial

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
        val list = normalizedMap[placement] ?: return null
        if (list.isEmpty()) return null

        val currentIndex = currentIndexMap[placement] ?: 0
        val selectedId = list[currentIndex]

        // Update index for next call
        currentIndexMap[placement] = (currentIndex + 1) % list.size

        return selectedId
    }
}