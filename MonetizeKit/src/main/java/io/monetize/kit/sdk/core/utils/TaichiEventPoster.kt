package io.monetize.kit.sdk.core.utils

import android.os.Bundle
import android.util.Log
import com.google.android.libraries.ads.mobile.sdk.common.AdValue
import com.google.firebase.analytics.FirebaseAnalytics
import io.monetize.kit.sdk.core.utils.init.AdKit
import java.util.Locale

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

    fun postRevenue(adValue: AdValue, currentRevenue: Double) {
        val currency = adValue.currencyCode.ifBlank { "USD" }.uppercase(Locale.US)
        if (currency == "USD" && currentRevenue > 0.0) {


            val totalRevenue = pref.adsRevenueCount + currentRevenue
            val revenueThreshold =
                firebaseDouble("ads_revenue_threshold", 10.0) / 1000

            val totalImpressions = pref.adsImpressionCount
            val impressionThreshold =
                firebaseLong("ads_impression_threshold", 10)

            val shouldPostRevenue =
                totalRevenue >= revenueThreshold &&
                        totalImpressions <= impressionThreshold

            if (shouldPostRevenue) {
                Log.d("RevenueTracking", "Revenue event posted")

                AdKit.analytics.postTaichiRevenueCount(Bundle().apply {
                    putDouble(FirebaseAnalytics.Param.VALUE, totalRevenue)
                    putString(FirebaseAnalytics.Param.CURRENCY, currency)
                })

                pref.adsRevenueCount = 0.0f
                pref.adsImpressionCount = 0

            } else if (totalImpressions > impressionThreshold) {
                // Threshold not reached within the impression window: start a new window,
                // otherwise the impression count stays above the limit and the event never fires again
                Log.d("RevenueTracking", "Revenue event not posted, impression window reset")

                pref.adsRevenueCount = 0.0f
                pref.adsImpressionCount = 0
            } else {
                Log.d("RevenueTracking", "Revenue event not posted")

                pref.adsRevenueCount = totalRevenue.toFloat()
            }
        }
    }
}