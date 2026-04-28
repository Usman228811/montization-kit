package io.monetize.kit.sdk.ads.banner

import android.app.Activity
import android.os.Build
import android.view.WindowMetrics
import com.google.android.gms.ads.AdSize
import io.monetize.kit.sdk.core.utils.adtype.BannerAdType


fun getAdSize(activity: Activity, bannerType: String): AdSize {

    return when (bannerType) {
        BannerAdType.ADAPTIVE_BANNER.name,
        BannerAdType.BOTTOM_COLLAPSIBLE_BANNER.name,
        BannerAdType.TOP_COLLAPSIBLE_BANNER.name -> getAdaptiveSize(activity)

        BannerAdType.LARGE_ANCHORED_ADAPTIVE_BANNER.name -> getLargeAnchored(activity)
        BannerAdType.LARGE_BANNER.name -> AdSize.LARGE_BANNER
        BannerAdType.MEDIUM_RECTANGLE_BANNER.name -> AdSize.MEDIUM_RECTANGLE
        else -> getAdaptiveSize(activity)
    }
}

private fun adWidth(activity: Activity): Int {
    val displayMetrics = activity.resources.displayMetrics
    val adWidthPixels =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics: WindowMetrics = activity.windowManager.currentWindowMetrics
            windowMetrics.bounds.width()
        } else {
            displayMetrics.widthPixels
        }
    val density = displayMetrics.density
    return (adWidthPixels / density).toInt()
}

fun getAdaptiveSize(activity: Activity): AdSize {
    return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth(activity))
}

fun getLargeAnchored(activity: Activity): AdSize {
    return AdSize.getLargeAnchoredAdaptiveBannerAdSize(activity, adWidth(activity))
}