package io.monetize.kit.sdk.core.utils.remoteconfig

class RemoteConfigBuilder private constructor() {
    val configMap = mutableMapOf<String, Any>()


    companion object {
        @Volatile
        private var instance: RemoteConfigBuilder? = null


        internal fun getInstance(
        ): RemoteConfigBuilder {
            return instance ?: synchronized(this) {
                instance ?: RemoteConfigBuilder(
                ).also { instance = it }
            }
        }
    }

    fun banner(placementKey: String, block: BannerConfig.() -> Unit) {
        BannerConfig(placementKey, configMap).apply(block)
    }

    class BannerConfig(
        private val placementKey: String,
        private val configMap: MutableMap<String, Any>
    ) {
        fun enable(value: Boolean) {
            configMap["${placementKey}_isAdEnable"] = value
        }
        fun bannerType(value: Long) {
            configMap["${placementKey}_bannerType"] = value
        }

    }

    fun native(placementKey: String, block: NativeConfig.() -> Unit) {
        NativeConfig(placementKey, configMap).apply(block)
    }

    fun fullScreen(placementKey: String, block: InterstitialConfig.() -> Unit) {
        InterstitialConfig(placementKey, configMap).apply(block)
    }

    class NativeConfig(
        private val placementKey: String,
        private val configMap: MutableMap<String, Any>
    ) {
        fun enable(value: Boolean = true) {
            configMap["${placementKey}_isAdEnable"] = value
        }

        fun ctaColor(color: String = "") {
            configMap["${placementKey}_ctaColor"] = color
        }

        fun bgColor(color: String = "") {
            configMap["${placementKey}_bgColor"] = color
        }

        fun adType(type: Int) {
            configMap["${placementKey}_adType"] = type
        }
        fun refreshTime(type: Int) {
            configMap["${placementKey}_refreshTime"] = type
        }
    }

    class InterstitialConfig(
        private val placementKey: String,
        private val configMap: MutableMap<String, Any>
    ) {
        fun enable(value: Boolean) {
            configMap["${placementKey}_isAdEnable"] = value
        }
        fun instantInter(value: Boolean = false) {
            configMap["${placementKey}_isInterInstant"] = value
        }
        fun instantReward(value: Boolean) {
            configMap["${placementKey}_isRewardInstant"] = value
        }
    }

    fun overAllNativeColor(ctaColor:String = "", bgColor:String = ""){
        configMap["overAllNativeCtaColor"] = ctaColor
        configMap["overAllNativeBgColor"] = bgColor
    }


    fun bool(key: String, value: Boolean) {
        configMap[key] = value
    }

    fun long(key: String, value: Long) {
        configMap[key] = value
    }

    fun string(key: String, value: String) {
        configMap[key] = value
    }

    fun getDefaultLong(key: String, default: Long = 0L): Long =
        (configMap[key] as? Number)?.toLong() ?: default

    fun getDefaultString(key: String, default: String = ""): String =
        configMap[key] as? String ?: default

    fun getDefaultBoolean(key: String, default: Boolean = false): Boolean =
        configMap[key] as? Boolean ?: default

}
