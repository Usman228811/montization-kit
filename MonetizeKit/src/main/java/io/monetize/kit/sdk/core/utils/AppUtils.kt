package io.monetize.kit.sdk.core.utils

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner

fun Context?.showToast(msg: String) {
    this?.let {
        if (msg.isNotBlank()) {
            Toast.makeText(it, msg, Toast.LENGTH_SHORT).show()
        }
    }
}

/**
 * Runs [block] on the main thread only while this activity is alive and at least STARTED.
 * If the activity is still stopped (ad activity on top) the block waits for onStart;
 * if the activity was destroyed (e.g. recreated while a full screen ad was showing)
 * the block is dropped, so app callbacks never navigate with a dead NavController.
 */
internal fun Activity.runWhenActive(block: () -> Unit) {
    runOnUiThread {
        if (isFinishing || isDestroyed) return@runOnUiThread
        val lifecycle = (this as? LifecycleOwner)?.lifecycle
        if (lifecycle == null || lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            block()
            return@runOnUiThread
        }
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                owner.lifecycle.removeObserver(this)
                block()
            }

            override fun onDestroy(owner: LifecycleOwner) {
                owner.lifecycle.removeObserver(this)
            }
        })
    }
}
