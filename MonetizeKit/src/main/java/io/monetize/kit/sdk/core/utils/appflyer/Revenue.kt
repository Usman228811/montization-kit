package io.monetize.kit.sdk.core.utils.appflyer

import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.rewarded.RewardedAd
import io.monetize.kit.sdk.core.utils.init.AdKit


fun postAdImpression(type:String){
    //Log.d("usmaaaaaaaan", "ad_impression: $type")
}

fun AdView.revenueListener(adId: String){
    setOnPaidEventListener { adValue ->
        val extras = responseInfo?.responseExtras
        val extraMap: MutableMap<String, Any> = mutableMapOf()

        extras?.getString("mediation_group_name")
            ?.let { value -> extraMap["mediation_group_name"] = value }
        extras?.getString("mediation_ab_test_name")
            ?.let { value -> extraMap["mediation_ab_test_name"] = value }
        extras?.getString("mediation_ab_test_variant")
            ?.let { value -> extraMap["mediation_ab_test_variant"] = value }

        AdKit.appsFlyer.logAdmobRevenue(
            adValue = adValue,
            extras = extraMap,
            adUnitId = adId,
            adType = "Banner",
            adapterResponseInfo = responseInfo?.loadedAdapterResponseInfo
        )

    }
}

fun RewardedAd.revenueListener(adId: String){
    setOnPaidEventListener { adValue ->
        val extras = responseInfo.responseExtras
        val extraMap: MutableMap<String, Any> = mutableMapOf()

        extras.getString("mediation_group_name")
            ?.let { value -> extraMap["mediation_group_name"] = value }
        extras.getString("mediation_ab_test_name")
            ?.let { value -> extraMap["mediation_ab_test_name"] = value }
        extras.getString("mediation_ab_test_variant")
            ?.let { value -> extraMap["mediation_ab_test_variant"] = value }

        AdKit.appsFlyer.logAdmobRevenue(
            adValue = adValue,
            extras = extraMap,
            adUnitId = adId,
            adType = "REWARDED",
            adapterResponseInfo = responseInfo.loadedAdapterResponseInfo
        )
    }
}

fun InterstitialAd.revenueListener(adId: String){
    setOnPaidEventListener { adValue ->
        val extras = responseInfo.responseExtras
        val extraMap: MutableMap<String, Any> = mutableMapOf()

        extras.getString("mediation_group_name")
            ?.let { value -> extraMap["mediation_group_name"] = value }
        extras.getString("mediation_ab_test_name")
            ?.let { value -> extraMap["mediation_ab_test_name"] = value }
        extras.getString("mediation_ab_test_variant")
            ?.let { value -> extraMap["mediation_ab_test_variant"] = value }

        AdKit.appsFlyer.logAdmobRevenue(
            adValue = adValue,
            extras = extraMap,
            adUnitId = adId,
            adType = "INTERSTITIAL",
            adapterResponseInfo = responseInfo.loadedAdapterResponseInfo
        )
    }
}

fun AppOpenAd.revenueListener(adId: String){
    setOnPaidEventListener { adValue ->
        val extras = responseInfo.responseExtras
        val extraMap: MutableMap<String, Any> = mutableMapOf()

        extras.getString("mediation_group_name")
            ?.let { value -> extraMap["mediation_group_name"] = value }
        extras.getString("mediation_ab_test_name")
            ?.let { value -> extraMap["mediation_ab_test_name"] = value }
        extras.getString("mediation_ab_test_variant")
            ?.let { value -> extraMap["mediation_ab_test_variant"] = value }

        AdKit.appsFlyer.logAdmobRevenue(
            adValue = adValue,
            extras = extraMap,
            adUnitId = adId,
            adType = "APP_OPEN",
            adapterResponseInfo = responseInfo.loadedAdapterResponseInfo
        )
    }
}

fun NativeAd.revenueListener(adUnitId: String) {
    setOnPaidEventListener { adValue ->
        val extras = responseInfo?.responseExtras
        val extraMap: MutableMap<String, Any> = mutableMapOf()

        extras?.getString("mediation_group_name")
            ?.let { value -> extraMap["mediation_group_name"] = value }
        extras?.getString("mediation_ab_test_name")
            ?.let { value -> extraMap["mediation_ab_test_name"] = value }
        extras?.getString("mediation_ab_test_variant")
            ?.let { value -> extraMap["mediation_ab_test_variant"] = value }

        AdKit.appsFlyer.logAdmobRevenue(
            adValue = adValue,
            extras = extraMap,
            adUnitId = adUnitId,
            adType = "NATIVE",
            adapterResponseInfo = responseInfo?.loadedAdapterResponseInfo
        )
    }
}