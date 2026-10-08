package io.monetize.kit.sdk.core.utils

import io.monetize.kit.sdk.core.utils.init.AdKit
import org.json.JSONArray


fun firebaseBoolean(key: String, default: Boolean) =
    AdKit.firebaseHelper.getBoolean(key, default)


fun firebaseLong(key: String, default: Long) =
    AdKit.firebaseHelper.getLong(key, default)


fun firebaseString(key: String, default: String) =
    AdKit.firebaseHelper.getString(key, default)

internal const val REMOTE_AD_ID_SUFFIX = "_adId"
internal const val REMOTE_OPEN_AD_ID_KEY = "OPEN_AD_ID"

/**
 * Ad unit IDs for [placement] from the remote config key "{placement}_adId".
 * The value can be a single ID, a comma-separated list, or a JSON array.
 * Returns an empty list when the key isn't set, so callers fall back to the IDs passed to AdKit.init.
 */
internal fun remoteAdIds(placement: String): List<String> {
    val raw = firebaseString("$placement$REMOTE_AD_ID_SUFFIX", "").trim()
    if (raw.isEmpty()) return emptyList()
    val ids = if (raw.startsWith("[")) {
        try {
            val array = JSONArray(raw)
            List(array.length()) { array.optString(it) }
        } catch (_: Exception) {
            emptyList()
        }
    } else {
        raw.split(",")
    }
    return ids.map { it.trim() }.filter { it.isNotEmpty() }
}
