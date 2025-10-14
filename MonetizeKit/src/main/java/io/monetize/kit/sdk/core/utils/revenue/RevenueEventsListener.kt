package io.monetize.kit.sdk.core.utils.revenue

import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.AdapterResponseInfo


interface RevenueEventsListener {

    fun onAdmobRevenue(
        adValue: AdValue,
        extras: Map<String, Any> = emptyMap(),
        country: String? = null,
        adUnitId: String? = null,
        adType: String? = null,
        placement: String? = null,
        adapterResponseInfo: AdapterResponseInfo? = null
    )

}