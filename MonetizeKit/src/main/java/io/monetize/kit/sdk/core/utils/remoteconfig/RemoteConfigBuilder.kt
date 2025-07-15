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


    fun bool(key: String, value: Boolean) {
        configMap[key] = value
    }

    fun long(key: String, value: Long) {
        configMap[key] = value
    }

    fun string(key: String, value: String) {
        configMap[key] = value
    }

}
