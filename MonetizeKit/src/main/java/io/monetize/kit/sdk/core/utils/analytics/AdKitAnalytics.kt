package io.monetize.kit.sdk.core.utils.analytics

import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import io.monetize.kit.sdk.BuildConfig

class AdKitAnalytics private constructor() {

    companion object {
        @Volatile
        private var instance: AdKitAnalytics? = null


        fun getInstance(
        ): AdKitAnalytics {
            return instance ?: synchronized(this) {
                instance ?: AdKitAnalytics().also { instance = it }
            }
        }
    }


    private val firebaseAnalytics: FirebaseAnalytics by lazy {
        Firebase.analytics
    }

    fun postAnalytics(message: String) {
        try {
            Log.d("AdKit_Logs", "${BuildConfig.DEBUG}")
            if (!BuildConfig.DEBUG) {
                var event = message
                if (message.contains(" ")) {
                    event = event.replace(" ", "_")
                }
                firebaseAnalytics.logEvent(event.trim(), Bundle())
            }
        } catch (_: Exception) {
        } catch (_: OutOfMemoryError) {
        }
    }
}