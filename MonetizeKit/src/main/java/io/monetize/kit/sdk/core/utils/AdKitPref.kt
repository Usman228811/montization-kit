package io.monetize.kit.sdk.core.utils

import android.content.Context
import androidx.core.content.edit

class AdKitPref private constructor(context: Context) {

    private val pref = context.getSharedPreferences(
        "MonetizeKitPref", Context.MODE_PRIVATE
    )

    var isLifeTimePurchased: Boolean
        get() = pref.getBoolean("isLifeTimePurchased", false)
        set(value) = pref.edit { putBoolean("isLifeTimePurchased", value) }
    var isAppSubscribed: Boolean
        get() = pref.getBoolean("isAppSubscribed", false)
        set(value) = pref.edit { putBoolean("isAppSubscribed", value) }

    val isAppPurchased = isLifeTimePurchased || isAppSubscribed




    fun getInterInt(key: String, defValue: Int): Int {
        return pref.getInt(key, defValue)
    }

    fun putInterInt(key: String, value: Int) {
        pref.edit { putInt(key, value) }
    }

    companion object {
        @Volatile
        private var instance: AdKitPref? = null

       internal fun getInstance(context: Context): AdKitPref {
            return instance ?: synchronized(this) {
                instance ?: AdKitPref(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}
