package io.monetize.kit.sdk.core.utils.analytics

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent

class AdKitAnalytics private constructor(private val context: Context, private val isDebug: Boolean) {

    private var showToast = false

    fun showToast(show:Boolean){
        showToast = show
    }

    companion object {
        @Volatile
        private var instance: AdKitAnalytics? = null


        fun getInstance(
            context:Context,
            isDebug: Boolean,
        ): AdKitAnalytics {
            return instance ?: synchronized(this) {
                instance ?: AdKitAnalytics(context, isDebug = isDebug).also { instance = it }
            }
        }
    }


    private val firebaseAnalytics: FirebaseAnalytics by lazy {
        Firebase.analytics
    }

    fun postAnalytics(message: String) {
        try {
            Log.d("AdKit_Logs", "is debug ${isDebug}")
            if (!isDebug) {
                var event = message
                if (message.contains(" ")) {
                    event = event.replace(" ", "_")
                }
                firebaseAnalytics.logEvent(event.trim(), Bundle())
            }

        } catch (_: Exception) {
        } catch (_: OutOfMemoryError) {
        }
        finally {
            if (showToast){
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun postScreenName(screenName: String, className: String) {
        try {
            if (!isDebug) {
                Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
                    param(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
                    param(FirebaseAnalytics.Param.SCREEN_CLASS, className)
                }
            }
        } catch (_: Exception) {
        } catch (_: OutOfMemoryError) {
        }
    }
}