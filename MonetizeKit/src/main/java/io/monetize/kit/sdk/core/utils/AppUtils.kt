package io.monetize.kit.sdk.core.utils

import android.content.Context
import android.widget.Toast

fun Context?.showToast(msg: String) {
    this?.let {
        if (msg.isNotBlank()) {
            Toast.makeText(it, msg, Toast.LENGTH_SHORT).show()
        }
    }
}