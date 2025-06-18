package io.monetize.kit.sdk.ads.banner

import android.app.Activity
import android.os.Build
import android.view.WindowMetrics
import com.google.android.gms.ads.AdSize


fun getAdSize(activity: Activity): AdSize {
    val displayMetrics = activity.resources.displayMetrics
    val adWidthPixels =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics: WindowMetrics = activity.windowManager.currentWindowMetrics
            windowMetrics.bounds.width()
        } else {
            displayMetrics.widthPixels
        }
    val density = displayMetrics.density
    val adWidth = (adWidthPixels / density).toInt()
    return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth)
}