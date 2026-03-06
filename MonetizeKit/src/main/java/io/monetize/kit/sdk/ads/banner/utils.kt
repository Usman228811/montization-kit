package io.monetize.kit.sdk.ads.banner

import android.app.Activity
import android.os.Build
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize
import io.monetize.kit.sdk.core.utils.adtype.BannerAdType


fun getAdSize(activity: Activity, bannerType: String): AdSize {

    return when (bannerType) {
        BannerAdType.ADAPTIVE_BANNER.name,
        BannerAdType.BOTTOM_COLLAPSIBLE_BANNER.name,
        BannerAdType.TOP_COLLAPSIBLE_BANNER.name -> getAdaptiveSize(activity)

        BannerAdType.LARGE_BANNER.name -> AdSize.LARGE_BANNER
        BannerAdType.MEDIUM_RECTANGLE_BANNER.name -> AdSize.MEDIUM_RECTANGLE
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