package io.monetize.kit.sdk.core.utils.appflyer

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.appsflyer.AFAdRevenueData
import com.appsflyer.AdRevenueScheme
import com.appsflyer.AppsFlyerLib
import com.appsflyer.MediationNetwork
import com.appsflyer.attribution.AppsFlyerRequestListener
import com.google.android.libraries.ads.mobile.sdk.common.AdValue
import io.monetize.kit.sdk.core.utils.init.AdKit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppsFlyer {

    private val TAG = "AppsFlyerTAG"
    private var sdkKey = ""

    private var isInitialized = false
    private var isDebug = true

    companion object {
        @Volatile
        private var instance: AppsFlyer? = null


        internal fun getInstance(
        ): AppsFlyer {
            return instance ?: synchronized(this) {
                instance ?: AppsFlyer().also { instance = it }
            }
        }
    }

    fun initAppFlyer(context: Context, sdkKey: String, isDebug: Boolean) {
        this.sdkKey = sdkKey
        this.isDebug = isDebug
        Log.d(TAG, "init: $sdkKey")
        if (sdkKey.isNotEmpty()) {
            if (!isInitialized) {
                CoroutineScope(Dispatchers.IO).launch {

                    AppsFlyerLib.getInstance().init(sdkKey, null, context)
                    AppsFlyerLib.getInstance()
                        .start(context, "", object : AppsFlyerRequestListener {
                            override fun onSuccess() {
                                Log.d(TAG, "onSuccess: ")
                                isInitialized = true
                            }

                            override fun onError(p0: Int, p1: String) {
                                Log.e(TAG, "onError: $p0 $p1")
                                isInitialized = false
                            }
                        })
                    if (isDebug) {
                        AppsFlyerLib.getInstance().setDebugLog(true)
                    }
                }
            }
        }
    }


    fun logAdmobRevenue(
        adValue: AdValue,
//        extras: Map<String, Any> = emptyMap(),
        country: String? = null,
        adUnitId: String? = null,
        adType: String? = null,
        placement: String? = null,
//        adapterResponseInfo: AdSourceResponseInfo? = null
    ) {

        logFirebaseAdRevenue(
            adValue = adValue,
            adUnitId = adUnitId ?: "-",
            adFormat = adType ?: "-",
            adSource = "Admob"
        )
        if (sdkKey.isNotEmpty() && isInitialized) {
            val mediationNetwork = MediationNetwork.GOOGLE_ADMOB
            val currencyIso4217Code = adValue.currencyCode
            val revenue = adValue.valueMicros / 1000000.0
            val adRevenueData = AFAdRevenueData(
                monetizationNetwork = "admob",
                mediationNetwork = mediationNetwork,
                currencyIso4217Code = currencyIso4217Code,
                revenue = revenue
            )

            val additionalParameters: MutableMap<String, Any> = HashMap()
            country?.let { additionalParameters[AdRevenueScheme.COUNTRY] = it }
            adUnitId?.let { additionalParameters[AdRevenueScheme.AD_UNIT] = it }
            adType?.let { additionalParameters[AdRevenueScheme.AD_TYPE] = it }
            placement?.let { additionalParameters[AdRevenueScheme.PLACEMENT] = it }

//            adapterResponseInfo?.let {
//                val adSourceName = it.name
//                val adSourceId = it.id
//                val adSourceInstanceName = it.instanceName
//                val adSourceInstanceId = it.instanceId
//                additionalParameters["adSourceName"] = adSourceName
//                additionalParameters["adSourceId"] = adSourceId
//                additionalParameters["adSourceInstanceName"] = adSourceInstanceName
//                additionalParameters["adSourceInstanceId"] = adSourceInstanceId
//
//            }

//            extras.forEach {
//                additionalParameters[it.key] = it.value
//            }
            logAdRevenue(adRevenueData, additionalParameters)
        }
    }


    fun logFirebaseAdRevenue(
        adValue: AdValue?,
        adUnitId: String,
        adFormat: String,
        adSource: String
    ) {
        adValue?.let {
            val revenue = adValue.valueMicros / 1_000_000.0 // micros -> standard unit
            val bundle = Bundle().apply {
                putString("ad_platform", adSource)
                putString("currency", adValue.currencyCode)
                putDouble("value", revenue)
                putString("ad_source", adValue.precisionType.toString())
                putString("ad_unit_id", adUnitId)
                putString("ad_format", adFormat)
//            putString("ad_placement", placementName)
            }

            AdKit.analytics.postRevenue("admob_revenue", bundle)

            Log.d(
                "AdMobRevenue",
                "ValueMicros=${adValue.valueMicros}, Currency=${adValue.currencyCode}, adSource=${adSource}, adFormat=${adFormat}"
            )
        }
    }


    private fun logAdRevenue(
        afAdRevenueData: AFAdRevenueData,
        map: Map<String, Any>
    ) {
        if (!isDebug && isInitialized) {
            Log.d(TAG, "logRevenue: logging $afAdRevenueData -> $map")
            AppsFlyerLib.getInstance().logAdRevenue(afAdRevenueData, map)
        }
    }
}