package io.monetize.kit.sdk.core.utils.init

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp
import io.monetize.kit.sdk.ads.interstitial.AdKitInterHelper
import io.monetize.kit.sdk.ads.interstitial.InterControllerConfig
import io.monetize.kit.sdk.ads.native_ad.AdKitNativeCommonHelper
import io.monetize.kit.sdk.ads.native_ad.AdsCustomLayoutHelper
import io.monetize.kit.sdk.ads.open.AdKitOpenAdManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AdKitInitializer(
    private val context: Context,
    private val adKitInterHelper: AdKitInterHelper,
    private val adKitOpenAdManager: AdKitOpenAdManager,
    private val customLayoutHelper: AdsCustomLayoutHelper,
    private val nativeCommonHelper: AdKitNativeCommonHelper,
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
        bigNativeLayout: Int? = null,
        smallNativeLayout: Int? = null,
        splitNativeLayout: Int? = null,
        bigNativeShimmer: Int? = null,
        smallNativeShimmer: Int? = null,
        splitNativeShimmer: Int? = null,
    ) {
        customLayoutHelper.apply {
            setBigNative(
                bigNative = bigNativeLayout,
                bigNativeShimmer = bigNativeShimmer
            )
            setSmallNative(
                smallNative = smallNativeLayout,
                smallNativeShimmer = smallNativeShimmer
            )
            setSplitNative(
                splitNative = splitNativeLayout,
                splitNativeShimmer = splitNativeShimmer
            )
        }
    }

    fun init(
        interControllerConfig: InterControllerConfig,
        nativeCommonIds: List<String>? = null
    ) {
        adKitInterHelper.setAdIds(
            splashId = interControllerConfig.splashId,
            appInterIds = interControllerConfig.appInterIds,
            interControllerConfig = interControllerConfig
        )
        adKitOpenAdManager.setOpenAdConfigs(
            adId = interControllerConfig.openAdId,
            isAdEnable = interControllerConfig.openAdEnable,
            isLoadingEnable = interControllerConfig.openAdLoadingEnable
        )
        nativeCommonHelper.setNativeAdIds(nativeCommonIds)
        adKitOpenAdManager.initOpenAd()


    }
}