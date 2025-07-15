package io.monetize.kit.sdk.core.utils.analytics

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import io.monetize.kit.sdk.BuildConfig

class AdKitAnalytics private constructor(private val context: Context) {

    private var showToast = false

    fun showToast(show:Boolean){
        showToast = show
    }

    companion object {
        @Volatile
        private var instance: AdKitAnalytics? = null


        fun getInstance(
            context:Context
        ): AdKitAnalytics {
            return instance ?: synchronized(this) {
                instance ?: AdKitAnalytics(context).also { instance = it }
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
            if (showToast){
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        } catch (_: Exception) {
        } catch (_: OutOfMemoryError) {
        }
    }
}