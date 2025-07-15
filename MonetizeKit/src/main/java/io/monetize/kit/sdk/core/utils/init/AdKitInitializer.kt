package io.monetize.kit.sdk.core.utils.init

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp
import io.monetize.kit.sdk.ads.interstitial.InterAdsConfigs
import io.monetize.kit.sdk.core.utils.init.AdKit.interHelper
import io.monetize.kit.sdk.core.utils.init.AdKit.nativeCustomLayoutHelper
import io.monetize.kit.sdk.core.utils.init.AdKit.openAdManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class AdKitInitializer private constructor(
) {

    companion object {
        @Volatile
        private var instance: AdKitInitializer? = null


        internal fun getInstance(
        ): AdKitInitializer {
            return instance ?: synchronized(this) {
                instance ?: AdKitInitializer(
                ).also { instance = it }
            }
        }
    }

    fun initMobileAds(context: Context, adMobAppId: String, onInit: () -> Unit) {

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
        nativeCustomLayoutHelper.apply {
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

   internal fun initAdsConfigs(
        interAdsConfigs: InterAdsConfigs,
    ) {
        interHelper.setInterAdsConfigs(
            interAdsConfigs = interAdsConfigs,
        )
        openAdManager.setOpenAdConfigs(
            isAdEnable = interAdsConfigs.openAdEnable,
            isLoadingEnable = interAdsConfigs.openAdLoadingEnable
        )
        openAdManager.initOpenAd()
    }


}