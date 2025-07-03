package io.monetize.kit.sdk.core.utils

import android.content.Context
import android.content.Context.MODE_PRIVATE
import androidx.core.content.edit

class AdKitPref private constructor(context: Context) {

    private val pref = context.getSharedPreferences(
        "MonetizeKitPref", Context.MODE_PRIVATE
    )

    var isAppPurchased: Boolean
        get() = pref.getBoolean("isAppPurchased", false)
        set(value) = pref.edit { putBoolean("isAppPurchased", value) }

    fun getInterInt(key: String, defValue: Int): Int {
        return pref.getInt(key, defValue)
    }

    fun putInterInt(key: String, value: Int) {
        pref.edit { putInt(key, value) }
    }

    companion object {
        @Volatile
        private var instance: AdKitPref? = null

        fun getInstance(context: Context): AdKitPref {
            return instance ?: synchronized(this) {
                instance ?: AdKitPref(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}


class AdKitPref2(context: Context) {
    private val pref = context.getSharedPreferences(
        "MonetizeKitPref", MODE_PRIVATE
    )

    var isAppPurchased: Boolean
        get() = pref.getBoolean("isAppPurchased", false)
        set(value) = pref.edit { putBoolean("isAppPurchased", value) }

    fun getInterInt(key: String, defValue: Int): Int {
        return pref.getInt(key, defValue)
    }

    fun putInterInt(key: String, value: Int) {
        pref.edit { putInt(key, value) }
    }

}