package io.monetize.kit.sdk.core.utils.init

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp
import io.monetize.kit.sdk.ads.interstitial.AdSdkInterHelper
import io.monetize.kit.sdk.ads.interstitial.InterControllerConfig
import io.monetize.kit.sdk.ads.native_ad.AdsCustomLayoutHelper
import io.monetize.kit.sdk.ads.open.AdSdkOpenAdManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AdSdkInitializer(
    private val context: Context,
    private val adSdkInterHelper: AdSdkInterHelper,
    private val adSdkOpenAdManager: AdSdkOpenAdManager,
    private val customLayoutHelper: AdsCustomLayoutHelper
) {

    fun initMobileAds(adMobAppId: String, onInit: () -> Unit) {
        try {
            val applicationInfo = context.packageManager.getApplicationInfo(
                context.packageName,
                PackageManager.GET_META_DATA
            )
            applicationInfo.metaData?.putString(
                "com.google.android.gms.ads.APPLICATION_ID",
                adMobAppId
            )
        } catch (e: PackageManager.NameNotFoundException) {
            Log.i("APPLICATION_ID", "ApplicationID not found")
            e.printStackTrace()
        }


        try {
            FirebaseApp.initializeApp(context)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        CoroutineScope(Dispatchers.IO).launch {
            try {
                MobileAds.initialize(context) {
                    Log.d("AdKit_Logs", "initMobileAds: app-id =: $adMobAppId")
                }
            } catch (_: Exception) {
            } catch (_: NoClassDefFoundError) {
            }
        }
        onInit()
    }

    fun setNativeCustomLayouts(
        bigNativeLayout: Int?,
        smallNativeLayout: Int?,
        splitNativeLayout: Int?,
    ) {
        customLayoutHelper.apply {
            setBigNative(bigNativeLayout)
            setSmallNative(smallNativeLayout)
            setSplitNative(splitNativeLayout)
        }
    }

    fun init(
        interControllerConfig: InterControllerConfig,
    ) {
        adSdkInterHelper.setAdIds(
            splashId = interControllerConfig.splashId,
            appInterIds = interControllerConfig.appInterIds,
            interControllerConfig = interControllerConfig
        )
        adSdkOpenAdManager.setOpenAdConfigs(
            adId = interControllerConfig.openAdId,
            isAdEnable = interControllerConfig.openAdEnable,
            isLoadingEnable = interControllerConfig.openAdLoadingEnable
        )
        adSdkOpenAdManager.initOpenAd()
    }
}