package io.monetize.kit.sdk.core.utils.init

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.google.android.libraries.ads.mobile.sdk.MobileAds
import com.google.android.libraries.ads.mobile.sdk.initialization.InitializationConfig
import io.monetize.kit.sdk.core.utils.REMOTE_DISABLE_ALL_ADS_KEY
import io.monetize.kit.sdk.core.utils.firebaseBoolean
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

    fun initMobileAds(context: Context, adMobAppId: String, onInit: () -> Unit) {

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
        Log.d("AdKit_Logs", "initMobileAds start")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                MobileAds.initialize(
                    context,
                    InitializationConfig.Builder(adMobAppId).build()
                ) {
                    Log.d("AdKit_Logs", "initMobileAds: successfully")
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

    // Ads are off when disabled in code or by the "DISABLE_ALL_ADS" Remote Config kill switch
    internal fun getDisableAds() = disableAds || firebaseBoolean(REMOTE_DISABLE_ALL_ADS_KEY, false)


    internal fun initAdsConfigs(

    ) {
        openAdManager.setOpenAdConfigs()
        openAdManager.initOpenAd()
    }


}