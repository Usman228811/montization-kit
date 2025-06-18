package io.monetize.kit.sdk.ads.open

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.view.LayoutInflater
import androidx.core.graphics.drawable.toDrawable
import io.monetize.kit.sdk.R


class AdLoadingDialog(private val activity: Activity) {
    private val alertDialog: AlertDialog

    init {
        val builder = AlertDialog.Builder(activity)
        val view = LayoutInflater.from(activity).inflate(R.layout.ad_loading_dialog, null)
        builder.setView(view)

        alertDialog = builder.create()
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.setCancelable(false)
        alertDialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
    }

    fun setBlackColor() {
        try {
            alertDialog.window?.setBackgroundDrawable(Color.BLACK.toDrawable())
        } catch (_: Exception) {
        }
    }

    fun showAlertDialog() {
        try {
            if (!alertDialog.isShowing && !activity.isFinishing && !activity.isDestroyed) {
                alertDialog.show()
            }
        } catch (ignored: Exception) {
        }
    }

    fun dismissAlertDialog() {
        try {
            if (alertDialog.isShowing && !activity.isFinishing && !activity.isDestroyed) {
                alertDialog.dismiss()
            }
        } catch (ignored: Exception) {
        }
    }

}