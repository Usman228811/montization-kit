package io.monetize.kit.sdk.core.utils.appflyer

import com.google.android.libraries.ads.mobile.sdk.common.AdValue
import io.monetize.kit.sdk.core.utils.init.AdKit


fun postAdImpression(type: String) {
    //Log.d("usmaaaaaaaan", "ad_impression: $type")
}

fun revenueListener(adId: String, adValue: AdValue, adType: String) {

//    setOnPaidEventListener { adValue ->
//        val extras = bannerAd.getResponseInfo().responseExtras
//        val extraMap: MutableMap<String, Any> = mutableMapOf()

//        extras?.getString("mediation_group_name")
//            ?.let { value -> extraMap["mediation_group_name"] = value }
//        extras?.getString("mediation_ab_test_name")
//            ?.let { value -> extraMap["mediation_ab_test_name"] = value }
//        extras?.getString("mediation_ab_test_variant")
//            ?.let { value -> extraMap["mediation_ab_test_variant"] = value }

        AdKit.appsFlyer.logAdmobRevenue(
            adValue = adValue,
//            extras = extraMap,
            adUnitId = adId,
            adType = adType,
//            adapterResponseInfo = bannerAd.getResponseInfo().loadedAdSourceResponseInfo
        )

//    }
}

//fun rewardedAdRevenueListener(adId: String, adValue: AdValue) {
//    setOnPaidEventListener { adValue ->
//        val extras = rewardedAd.getResponseInfo().responseExtras
//        val extraMap: MutableMap<String, Any> = mutableMapOf()
//
//        extras.getString("mediation_group_name")
//            ?.let { value -> extraMap["mediation_group_name"] = value }
//        extras.getString("mediation_ab_test_name")
//            ?.let { value -> extraMap["mediation_ab_test_name"] = value }
//        extras.getString("mediation_ab_test_variant")
//            ?.let { value -> extraMap["mediation_ab_test_variant"] = value }
//
//        AdKit.appsFlyer.logAdmobRevenue(
//            adValue = adValue,
//            extras = extraMap,
//            adUnitId = adId,
//            adType = "REWARDED",
//            adapterResponseInfo = rewardedAd.getResponseInfo().loadedAdSourceResponseInfo
//        )
//    }
//}

//fun InterstitialAd.revenueListener(adId: String) {
//    setOnPaidEventListener { adValue ->
//        val extras = responseInfo.responseExtras
//        val extraMap: MutableMap<String, Any> = mutableMapOf()
//
//        extras.getString("mediation_group_name")
//            ?.let { value -> extraMap["mediation_group_name"] = value }
//        extras.getString("mediation_ab_test_name")
//            ?.let { value -> extraMap["mediation_ab_test_name"] = value }
//        extras.getString("mediation_ab_test_variant")
//            ?.let { value -> extraMap["mediation_ab_test_variant"] = value }
//
//        AdKit.appsFlyer.logAdmobRevenue(
//            adValue = adValue,
//            extras = extraMap,
//            adUnitId = adId,
//            adType = "INTERSTITIAL",
//            adapterResponseInfo = responseInfo.loadedAdapterResponseInfo
//        )
//    }
//}

//fun AppOpenAd.revenueListener(adId: String) {
//    setOnPaidEventListener { adValue ->
//        val extras = responseInfo.responseExtras
//        val extraMap: MutableMap<String, Any> = mutableMapOf()
//
//        extras.getString("mediation_group_name")
//            ?.let { value -> extraMap["mediation_group_name"] = value }
//        extras.getString("mediation_ab_test_name")
//            ?.let { value -> extraMap["mediation_ab_test_name"] = value }
//        extras.getString("mediation_ab_test_variant")
//            ?.let { value -> extraMap["mediation_ab_test_variant"] = value }
//
//        AdKit.appsFlyer.logAdmobRevenue(
//            adValue = adValue,
//            extras = extraMap,
//            adUnitId = adId,
//            adType = "APP_OPEN",
//            adapterResponseInfo = responseInfo.loadedAdapterResponseInfo
//        )
//    }
//}

//fun NativeAd.revenueListener(adUnitId: String) {

//    setOnPaidEventListener { adValue ->
//        val extras = responseInfo?.responseExtras
//        val extraMap: MutableMap<String, Any> = mutableMapOf()
//
//        Log.d("usman", "revenueListener: $adUnitId")
//
//        extras?.getString("mediation_group_name")
//            ?.let { value -> extraMap["mediation_group_name"] = value }
//        extras?.getString("mediation_ab_test_name")
//            ?.let { value -> extraMap["mediation_ab_test_name"] = value }
//        extras?.getString("mediation_ab_test_variant")
//            ?.let { value -> extraMap["mediation_ab_test_variant"] = value }
//
//        AdKit.appsFlyer.logAdmobRevenue(
//            adValue = adValue,
//            extras = extraMap,
//            adUnitId = adUnitId,
//            adType = "NATIVE",
//            adapterResponseInfo = responseInfo?.loadedAdapterResponseInfo
//        )
//    }
//}