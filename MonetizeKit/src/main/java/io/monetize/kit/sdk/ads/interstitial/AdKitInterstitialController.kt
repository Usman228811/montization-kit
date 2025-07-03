package io.monetize.kit.sdk.ads.interstitial

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import io.monetize.kit.sdk.ads.open.AdLoadingDialog
import io.monetize.kit.sdk.core.utils.AdKitInternetController
import io.monetize.kit.sdk.core.utils.AdKitPref
import io.monetize.kit.sdk.core.utils.IS_INTERSTITIAL_Ad_SHOWING
import io.monetize.kit.sdk.core.utils.consent.AdKitConsentManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger

data class InterControllerConfig(
    val openAdId: String,
    val splashId: String,
    val appInterIds: List<String>,
    val splashInterEnable: Boolean = false,
    val openAdEnable: Boolean = false,
    val splashTime: Long = 16L,
    val instantInterTime: Long = 8L,
    val interLoadingEnable: Boolean = false,
    val openAdLoadingEnable: Boolean = false,
)


class InterstitialController private constructor(
    private val internetController: AdKitInternetController,
    private val mMyPref: AdKitPref,
    private val mConsent: AdKitConsentManager,
) {


    companion object {
        @Volatile
        private var instance: InterstitialController? = null


        fun getInstance(
            context: Context,
        ): InterstitialController {
            return instance ?: synchronized(this) {
                instance ?: InterstitialController(
                    AdKitInternetController.getInstance(context),
                    AdKitPref.getInstance(context),
                    AdKitConsentManager.getInstance(context),
                ).also { instance = it }
            }
        }
    }


    private var interControllerConfig: InterControllerConfig? = null
    private var handlerAd = Handler(Looper.getMainLooper())
    private var canRequestAd = true
    private var admobInterAd: InterstitialAd? = null
    private var mInterstitialControllerListener: InterstitialControllerListener? = null
    private var adLoadingDialog: AdLoadingDialog? = null

    private val handlerAdDelay: Handler = Handler(Looper.getMainLooper())
    private var isAppPause = false
    private var isHandlerAdDelayRunning = false
    private val runnableHandlerAdDelay = Runnable {
        if (mInterstitialControllerListener != null && isHandlerAdDelayRunning) {
            try {
                adLoadingDialog?.dismissAlertDialog()
            } catch (_: Exception) {
            }
            isHandlerAdDelayRunning = false
            mInterstitialControllerListener?.onAdClosed()
        }
    }
    private val adIdIndex = AtomicInteger(0)
    private var idsList: List<String> = emptyList()


    fun setAppInPause(isAppPause: Boolean) {
        this.isAppPause = isAppPause
    }


    fun setAdIds(ids: List<String>, interControllerConfig: InterControllerConfig) {
        idsList = ids
        adIdIndex.set(0) // Optional: reset rotation when new list set
        this.interControllerConfig = interControllerConfig
    }

    fun getAdId(): String {
        if (idsList.isEmpty()) throw IllegalStateException("Ad IDs not set. Call setAdIds() first.")
        val index = adIdIndex.getAndUpdate { (it + 1) % idsList.size }
        return idsList[index]
    }


    private fun startDelayHandler() {
        val instantTime = interControllerConfig?.instantInterTime ?: 8L
        if (!isHandlerAdDelayRunning) {
            isHandlerAdDelayRunning = true
            handlerAdDelay.postDelayed(
                runnableHandlerAdDelay, instantTime * 1000
            )
        }
    }

    fun removeCallBacksDelay() {
        try {
            isHandlerAdDelayRunning = false
            handlerAdDelay.removeCallbacks(runnableHandlerAdDelay)
        } catch (ignored: Exception) {
        }
    }

    private fun showAdmobAd(activity: Activity, key: String) {
        try {
            if (admobInterAd != null && !isAppPause && !IS_INTERSTITIAL_Ad_SHOWING) {
                mInterstitialControllerListener?.onAdShow()
                if (admobInterAd != null) {
                    setAdmobFullScreen()
                    admobInterAd?.show(activity)
                }
                if (key != "") {
                    setInterCount(key, 0)
                }
            } else {
                mInterstitialControllerListener?.onAdClosed()
            }
        } catch (exception: Exception) {
            mInterstitialControllerListener?.onAdClosed()
        }
    }

    fun loadAndShowWithCounter(
        context: Activity, enable: Boolean,
        listener: InterstitialControllerListener, key: String, counter: Long,
    ) {
        mInterstitialControllerListener = listener
        val savedCount = getInterCount(key)
        if (mMyPref.isAppPurchased || !enable || isAppPause || IS_INTERSTITIAL_Ad_SHOWING) {
            listener.onAdClosed()
        } else if (savedCount == -1 || savedCount >= counter) {
            if (admobInterAd != null) {
                checkProgressShowAd(context, key)
            } else {
                loadAndShow(context, true, key, listener)
            }
        } else if ((savedCount + 1).toLong() >= counter) {
            listener.onAdClosed()
            setInterCount(key, savedCount + 1)
        } else {
            listener.onAdClosed()
            setInterCount(key, savedCount + 1)
        }
    }

    fun showWithoutCounter(
        context: Activity, enable: Boolean,
        listener: InterstitialControllerListener
    ) {
        mInterstitialControllerListener = listener
        if (mMyPref.isAppPurchased || !enable || isAppPause || IS_INTERSTITIAL_Ad_SHOWING) {
            listener.onAdClosed()
        } else {
            if (admobInterAd != null) {
                checkProgressShowAd(context)
            } else {
                preLoadInter(context)
            }
        }
    }

    fun showWithCounter(
        context: Activity, enable: Boolean,
        listener: InterstitialControllerListener, key: String, counter: Long,
    ) {
        mInterstitialControllerListener = listener
        val savedCount = getInterCount(key)
        if (mMyPref.isAppPurchased || !enable || isAppPause || IS_INTERSTITIAL_Ad_SHOWING) {
            listener.onAdClosed()
        } else if (savedCount == -1 || savedCount >= counter) {
            if (admobInterAd != null) {
                checkProgressShowAd(context, key)
            } else {

                if ((savedCount + 2).toLong() >= counter) {
                    preLoadInter(context)
                }

                if (savedCount == -1) {
                    setInterCount(key, 1)
                }
                listener.onAdClosed()

            }
        } else if ((savedCount + 2).toLong() >= counter) {
            listener.onAdClosed()
            preLoadInter(context)
            setInterCount(key, savedCount + 1)
        } else {
            listener.onAdClosed()
            setInterCount(key, savedCount + 1)
        }
    }

    private fun initAdMobCounter(context: Context, key: String, counter: Long) {
        val canLoad = internetController.isConnected && !mMyPref.isAppPurchased
        if (mConsent.canRequestAds && canLoad) {
            val savedCount = getInterCount(key)
            if (savedCount == -1 || savedCount >= counter) {
                preLoadInter(context)
            }
        }
    }

    fun initAdMob(context: Context, enable: Boolean, counterKey: String = "", counter: Long = -1) {
        if (!enable) {
            return
        }
        val isForCounter = counterKey.isNotEmpty() && counter.toInt() != -1
        if (isForCounter) {
            initAdMobCounter(context, counterKey, counter)
        } else {
            val canLoad = internetController.isConnected && !mMyPref.isAppPurchased
            if (mConsent.canRequestAds && canLoad) {
                preLoadInter(context)
            }
        }
    }

    private fun preLoadInter(context: Context) {
        try {
            val canGo = internetController.isConnected && mConsent.canRequestAds
            if (!mMyPref.isAppPurchased && !hasAd && canGo) {
                if (!canRequestAd) {
                    return
                }
                canRequestAd = false
//                if (isDebug) {
//                    context.showToast("Inter calling")
//                }
//                "Common Inter Called".logIt()
                InterstitialAd.load(
                    context, getAdId(),
                    AdRequest.Builder().build(),
                    object : InterstitialAdLoadCallback() {
                        override fun onAdLoaded(interstitialAd: InterstitialAd) {
                            super.onAdLoaded(interstitialAd)
                            admobInterAd = interstitialAd
                            canRequestAd = true
//                            if (isDebug) {
//                                context.showToast("Inter loaded")
//                            }
//                            "Common Inter Loaded".logIt()
                        }

                        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                            super.onAdFailedToLoad(loadAdError)
                            admobInterAd = null
                            canRequestAd = true
//                            if (isDebug) {
//                                context.showToast("Inter failed: ${loadAdError.code}")
//                            }
//                            "Common Inter Failed: ${loadAdError.code} and message: ${loadAdError.message}".logIt()
                        }
                    })
            }
        } catch (ignored: Exception) {
            canRequestAd = true
        }
    }


    fun loadAndShow(
        context: Activity,
        enable: Boolean = true,
        key: String = "",
        listener: InterstitialControllerListener,
    ) {
        mInterstitialControllerListener = listener
        try {
            if (!mMyPref.isAppPurchased && internetController.isConnected && enable && mConsent.canRequestAds) {
                if (!canRequestAd) {
                    mInterstitialControllerListener?.onAdClosed()
                    return
                }
                canRequestAd = false
                dismissLoadingDialog()
                adLoadingDialog = AdLoadingDialog(context)
                adLoadingDialog?.showAlertDialog()
                startDelayHandler()
//                if (isDebug) {
////                    "Common Inter Called".logIt()
//                    context.showToast("Inter Ad Called")
//                }
                InterstitialAd.load(
                    context, getAdId(),
                    AdRequest.Builder().build(),
                    object : InterstitialAdLoadCallback() {
                        override fun onAdLoaded(p0: InterstitialAd) {
//                            if (isDebug) {
////                                "Common Inter Loaded".logIt()
//                                context.showToast("Inter Ad Loaded")
//                            }
                            super.onAdLoaded(p0)
                            admobInterAd = p0
                            canRequestAd = true
                            if (isHandlerAdDelayRunning) {
                                dismissLoadingDialog()
                                removeCallBacksDelay()
                                showAdmobAd(context, key)
                            }
                        }

                        override fun onAdFailedToLoad(p0: LoadAdError) {
//                            if (isDebug) {
////                                "Common Inter Ad Failed: ${p0.code}".logIt()
//                                context.showToast("Inter Ad Failed")
//                            }
                            canRequestAd = true
                            handlerRemoveCallback(context)
                        }
                    })
            } else {
                mInterstitialControllerListener?.onAdClosed()
            }
        } catch (e: Exception) {
            canRequestAd = true
            handlerRemoveCallback(context)
        } catch (e: OutOfMemoryError) {
            canRequestAd = true
            handlerRemoveCallback(context)
        }
    }

    private fun handlerRemoveCallback(context: Activity) {
        if (isHandlerAdDelayRunning) {
            dismissLoadingDialog()
            removeCallBacksDelay()
            mInterstitialControllerListener?.onAdClosed()
        }
    }


    private fun dismissLoadingDialog() {
        try {
            adLoadingDialog?.dismissAlertDialog()
        } catch (_: Exception) {
        }
    }

    private fun checkProgressShowAd(
        activity: Activity, key: String = "",
    ) {
        if (interControllerConfig?.interLoadingEnable != false) {
            try {
                mInterstitialControllerListener?.onAdShow()
                val adLoadingDialog = AdLoadingDialog(activity)
                adLoadingDialog.showAlertDialog()
                handlerAd.postDelayed({
                    showAdmobAd(activity, key)
                    adLoadingDialog.dismissAlertDialog()
                }, 1000)
            } catch (e: Exception) {
                showAdmobAd(activity, key)
            }
        } else {
            showAdmobAd(activity, key)
        }
    }


    private fun setAdmobFullScreen() {
        admobInterAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                dismissLoadingDialog()
                mInterstitialControllerListener?.onAdClosed()
                super.onAdDismissedFullScreenContent()
                IS_INTERSTITIAL_Ad_SHOWING = false
                admobInterAd = null
//                activity.userAnalytics("${fromScreen}_Inter_Close")
//                if (loadNewNextAd && key.isEmpty()) {
//                    preLoadInter(activity)
//                }
            }

            override fun onAdShowedFullScreenContent() {
                super.onAdShowedFullScreenContent()
                IS_INTERSTITIAL_Ad_SHOWING = true
                admobInterAd = null
//                activity.userAnalytics("${fromScreen}_Inter_Show")
            }

            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                dismissLoadingDialog()
                mInterstitialControllerListener?.onAdClosed()
                super.onAdFailedToShowFullScreenContent(p0)
//                activity.userAnalytics("${fromScreen}_Inter_Show_Failed")
                admobInterAd = null
                IS_INTERSTITIAL_Ad_SHOWING = false
//                if (loadNewNextAd && key.isEmpty()) {
//                    preLoadInter(activity)
//                }
            }

        }
    }

    private fun getInterCount(key: String, defValue: Int = 0): Int {
        return mMyPref.getInterInt(key, defValue)
    }

    private fun setInterCount(key: String = "APP_INTER_COUNTER", count: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            mMyPref.putInterInt(key, count)
        }
    }


    private val hasAd: Boolean
        get() {
            return admobInterAd != null
        }

}