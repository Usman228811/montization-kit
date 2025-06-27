package io.monetize.kit.sdk.core.utils

import android.content.Context
import android.content.Context.MODE_PRIVATE
import androidx.core.content.edit

class AdKitPref(context: Context) {
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