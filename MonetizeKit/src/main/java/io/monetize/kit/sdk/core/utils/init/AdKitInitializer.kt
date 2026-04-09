package io.monetize.kit.sdk.core.utils.init

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.google.android.gms.ads.MobileAds
import io.monetize.kit.sdk.core.utils.init.AdKit.openAdManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class AdKitInitializer private constructor(
) {

    private var disableAds = false

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

    fun initMobileAds(context: Context, onInit: () -> Unit) {

//        try {
//            val applicationInfo = context.packageManager.getApplicationInfo(
//                context.packageName,
//                PackageManager.GET_META_DATA
//            )
//            applicationInfo.metaData?.putString(
//                "com.google.android.gms.ads.APPLICATION_ID",
//                adMobAppId
//            )
//        } catch (e: PackageManager.NameNotFoundException) {
//            Log.i("APPLICATION_ID", "ApplicationID not found")
//            e.printStackTrace()
//        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                MobileAds.initialize(context) {
                    Log.d("AdKit_Logs", "initMobileAds Successfully")
                }
            } catch (_: Exception) {
            } catch (_: NoClassDefFoundError) {
            }
        }
        onInit()
    }

    fun disableAds(disableAds: Boolean) {
        this.disableAds = disableAds
    }

    internal fun getDisableAds() = disableAds


    internal fun initAdsConfigs(

    ) {
        openAdManager.setOpenAdConfigs()
        openAdManager.initOpenAd()
    }


}