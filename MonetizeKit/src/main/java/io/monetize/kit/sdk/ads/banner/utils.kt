package io.monetize.kit.sdk.ads.banner

import android.app.Activity
import android.os.Build
import com.google.android.gms.ads.AdSize


fun getAdSize(activity: Activity, bannerType: Long): AdSize {

    return when (bannerType) {
        0L, 3L, 4L -> getAdaptiveSize(activity)
        1L -> AdSize.LARGE_BANNER
        2L -> AdSize.MEDIUM_RECTANGLE
        else -> getAdaptiveSize(activity)
    }
}

fun getAdaptiveSize(activity: Activity): AdSize {
    val displayMetrics = activity.resources.displayMetrics
    val adWidthPixels = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        activity.windowManager.currentWindowMetrics.bounds.width()
    } else {
        displayMetrics.widthPixels
    }
    val adWidth = (adWidthPixels / displayMetrics.density).toInt()
    return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth)
}