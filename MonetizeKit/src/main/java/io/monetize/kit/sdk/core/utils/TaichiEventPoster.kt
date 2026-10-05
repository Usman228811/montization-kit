package io.monetize.kit.sdk.core.utils

import android.util.Log
import io.monetize.kit.sdk.core.utils.init.AdKit

class TaichiEventPoster(val pref: AdKitPref) {

    companion object {
        @Volatile
        private var instance: TaichiEventPoster? = null

        internal fun getInstance(pref: AdKitPref): TaichiEventPoster {
            return instance ?: synchronized(this) {
                instance ?: TaichiEventPoster(pref).also {
                    instance = it
                }
            }
        }
    }

    fun saveAdsImpressionCount() {
        val oldCount = pref.adsImpressionCount
        pref.adsImpressionCount = oldCount + 1
    }

    fun postRevenue(revenue: Double) {
        val totalRevenue = pref.adsRevenueCount + revenue
        val revenueThreshold =
            firebaseLong("ads_revenue_threshold", 10).toDouble() / 1000.0

        val totalImpressions = pref.adsImpressionCount
        val impressionThreshold =
            firebaseLong("ads_impression_threshold", 10)

        val shouldPostRevenue =
            totalRevenue >= revenueThreshold &&
                    totalImpressions <= impressionThreshold

        if (shouldPostRevenue) {
            Log.d("RevenueTracking", "Revenue event posted")

            AdKit.analytics.postTaichiRevenueCount()

            pref.adsRevenueCount = 0.0f
            pref.adsImpressionCount = 0
        } else {
            Log.d("RevenueTracking", "Revenue event not posted")
        }
    }
}