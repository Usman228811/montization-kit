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
import io.monetize.kit.sdk.core.utils.IS_INTERSTITIAL_Ad_SHOWING
import io.monetize.kit.sdk.core.utils.appflyer.postAdImpression
import io.monetize.kit.sdk.core.utils.appflyer.revenueListener
import io.monetize.kit.sdk.core.utils.firebaseBoolean
import io.monetize.kit.sdk.core.utils.firebaseLong
import io.monetize.kit.sdk.core.utils.init.AdKit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


data class InterAdSingleModel(
    val key: String = "",
    val controller: InterstitialController? = null,
)

val singleInterList = ArrayList<InterAdSingleModel>()

//data class InterAdsConfigs(
//    val openAdEnable: Boolean,
//    val openAdInstant: Boolean = false,
//    val splashTime: Long,
//    val instantInterTime: Long = 8L,
//    val instantOpenAdTime: Long = 8L,
//    val interLoadingEnable: Boolean = false,
//    val openAdLoadingEnable: Boolean = false,
//)

class InterstitialController private constructor(
) {
    companion object {
        fun getInstance(
        ): InterstitialController {
            return InterstitialController()
        }
    }


    private var placementKey: String = ""
    private var adIdKey: String = ""
    private var handlerAd = Handler(Looper.getMainLooper())
    private var canRequestAd = true
    private var admobInterAd: InterstitialAd? = null
    private var mInterstitialControllerListener: InterstitialControllerListener? = null
    private var adLoadingDialog: AdLoadingDialog? = null

    private val handlerAdDelay: Handler = Handler(Looper.getMainLooper())
    private var isHandlerAdDelayRunning = false
    private val runnableHandlerAdDelay = Runnable {
        if (mInterstitialControllerListener != null && isHandlerAdDelayRunning) {
            try {
                adLoadingDialog?.dismissAlertDialog()
            } catch (_: Exception) {
            }
            isHandlerAdDelayRunning = false
            mInterstitialControllerListener?.onAdClosed(
                reason = "$placementKey called onAdClosed because: instant ad time is completed"

            )
        }
    }


    private fun startDelayHandler() {
        var instantTime = firebaseLong("INTER_INSTANT_TIME", 8L)
        if (instantTime == 0L) {
            instantTime = 8L
        }
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
            if (admobInterAd != null && !AdKit.interHelper.getAppInPause() && !IS_INTERSTITIAL_Ad_SHOWING) {
                mInterstitialControllerListener?.onAdShow()
                if (admobInterAd != null) {
                    setAdmobFullScreen(activity, key)
                    admobInterAd?.show(activity)
                }
                if (key != "") {
                    setInterCount(key, 0)
                }
            } else {
                mInterstitialControllerListener?.onAdClosed(
                    reason = "$placementKey called onAdClosed because: App is minimized | Other Ad is showing"
                )
            }
        } catch (exception: Exception) {
            mInterstitialControllerListener?.onAdClosed(
                reason = "$placementKey called onAdClosed because: Inter Exception"

            )
        }
    }

    fun loadAndShowWithCounter(
        context: Activity,
        placementKey: String,
        adIdKey: String,
        enable: Boolean,
        listener: InterstitialControllerListener, key: String, counter: Long,
    ) {

        this.placementKey = placementKey
        this.adIdKey = adIdKey
        mInterstitialControllerListener = listener
        val savedCount = getInterCount(key)
        if (AdKit.adKitPref.isAppPurchased || !enable || AdKit.interHelper.getAppInPause() || IS_INTERSTITIAL_Ad_SHOWING) {
            listener.onAdClosed(
                reason = "$placementKey called onAdClosed because: App is minimized | Ad is disabled | Other Ad is showing | App is Purchased"

            )
        } else if (savedCount == -1 || savedCount >= counter) {
            if (admobInterAd != null) {
                checkProgressShowAd(context, key)
            } else {
                loadAndShow(
                    context,
                    this@InterstitialController.placementKey, adIdKey, true, key, listener
                )
            }
        } else if ((savedCount + 1).toLong() >= counter) {
            listener.onAdClosed(
                reason = "$placementKey called onAdClosed because: counter is not completed"

            )
            setInterCount(key, savedCount + 1)
        } else {
            listener.onAdClosed(
                reason = "$placementKey called onAdClosed because: counter is not completed"
            )
            setInterCount(key, savedCount + 1)
        }
    }

    fun showWithoutCounter(
        context: Activity,
        placementKey: String,
        adIdKey: String,
        enable: Boolean,
        listener: InterstitialControllerListener
    ) {
        mInterstitialControllerListener = listener
        this.placementKey = placementKey
        this.adIdKey = adIdKey
        if (AdKit.adKitPref.isAppPurchased || !enable || AdKit.interHelper.getAppInPause() || IS_INTERSTITIAL_Ad_SHOWING) {
            listener.onAdClosed(
                reason = "$placementKey called onAdClosed because: App is minimized | Ad is disabled | Other Ad is showing | App is Purchased"
            )
        } else {
            if (admobInterAd != null) {
                checkProgressShowAd(context)
            } else {
                listener.onAdClosed(
                    reason = "$placementKey called onAdClosed because: Ad is null"
                )
                loadInter(context)
            }
        }
    }

    fun showWithCounter(
        context: Activity,
        placementKey: String,
        adIdKey: String,
        enable: Boolean,
        listener: InterstitialControllerListener, key: String, counter: Long,
    ) {
        mInterstitialControllerListener = listener
        this.placementKey = placementKey
        this.adIdKey = adIdKey
        val savedCount = getInterCount(key)
        if (AdKit.adKitPref.isAppPurchased || !enable || AdKit.interHelper.getAppInPause() || IS_INTERSTITIAL_Ad_SHOWING) {
            listener.onAdClosed(
                reason = "$placementKey called onAdClosed because: App is minimized | Ad is disabled | Other Ad is showing | App is Purchased"
            )
        } else if (savedCount == -1 || savedCount >= counter) {
            if (admobInterAd != null) {
                checkProgressShowAd(context, key)
            } else {

                if ((savedCount + 2).toLong() >= counter) {
                    loadInter(context)
                }

                if (savedCount == -1) {
                    setInterCount(key, 1)
                }
                listener.onAdClosed(
                    reason = "$placementKey called onAdClosed because: counter is complete but no ad is available"
                )

            }
        } else if ((savedCount + 2).toLong() >= counter) {
            listener.onAdClosed(
                reason = "$placementKey called onAdClosed because: counter is not completed"
            )
            loadInter(context)
            setInterCount(key, savedCount + 1)
        } else {
            listener.onAdClosed(
                reason = "$placementKey called onAdClosed because: counter is not completed"
            )
            setInterCount(key, savedCount + 1)
        }
    }

    private fun initAdMobCounter(context: Context, key: String, counter: Long) {
        val canLoad = AdKit.internetController.isConnected && !AdKit.adKitPref.isAppPurchased
        if (AdKit.consentManager.canRequestAds && canLoad) {
            val savedCount = getInterCount(key)
            if (savedCount == -1 || savedCount >= counter) {
                loadInter(context)
            }
        }
    }

    fun preLoadInter(
        context: Context,
        placementKey: String,
        adIdKey: String,
        enable: Boolean, counterKey: String = "", counter: Long = -1
    ) {
        if (!enable) {
            return
        }
        this.placementKey = placementKey
        this.adIdKey = adIdKey
        if (admobInterAd != null) {
            return
        }
        val isForCounter = counterKey.isNotEmpty() && counter.toInt() != -1
        if (isForCounter) {
            initAdMobCounter(context, counterKey, counter)
        } else {
            val canLoad = AdKit.internetController.isConnected && !AdKit.adKitPref.isAppPurchased
            if (AdKit.consentManager.canRequestAds && canLoad) {
                loadInter(context)
            }
        }
    }

    private fun loadInter(context: Context) {
        try {
            val canGo = AdKit.internetController.isConnected && AdKit.consentManager.canRequestAds
            if (!AdKit.adKitPref.isAppPurchased && !hasAd && canGo) {
                if (!canRequestAd) {
                    return
                }
                canRequestAd = false

                val id = AdKit.interIdManager.getNextInterId(adIdKey) ?: ""
                InterstitialAd.load(
                    context, id,
                    AdRequest.Builder().build(),
                    object : InterstitialAdLoadCallback() {
                        override fun onAdLoaded(interstitialAd: InterstitialAd) {
                            super.onAdLoaded(interstitialAd)
                            admobInterAd = interstitialAd
                            admobInterAd?.revenueListener(id)

                            canRequestAd = true
                        }

                        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                            super.onAdFailedToLoad(loadAdError)
                            admobInterAd = null
                            canRequestAd = true
                        }
                    })
            }
        } catch (ignored: Exception) {
            canRequestAd = true
        }
    }


    fun loadAndShow(
        context: Activity,
        placementKey: String,
        adIdKey: String,
        enable: Boolean = true,
        key: String = "",
        listener: InterstitialControllerListener,
    ) {

        this.placementKey = placementKey
        this.adIdKey = adIdKey
        mInterstitialControllerListener = listener
        try {
            if (!AdKit.adKitPref.isAppPurchased && AdKit.internetController.isConnected && enable && AdKit.consentManager.canRequestAds) {
                if (!canRequestAd) {
                    mInterstitialControllerListener?.onAdClosed(
                        reason = "$placementKey called onAdClosed because: Other ad is being request"
                    )
                    return
                }
                if (admobInterAd != null) {
                    checkProgressShowAd(context, key)
                } else {
                    canRequestAd = false
                    dismissLoadingDialog()
                    adLoadingDialog = AdLoadingDialog(context)
                    adLoadingDialog?.showAlertDialog()
                    startDelayHandler()
                    val id = AdKit.interIdManager.getNextInterId(adIdKey) ?: ""
                    InterstitialAd.load(
                        context, id,
                        AdRequest.Builder().build(),
                        object : InterstitialAdLoadCallback() {
                            override fun onAdLoaded(p0: InterstitialAd) {
                                super.onAdLoaded(p0)
                                admobInterAd = p0
                                admobInterAd?.revenueListener(
                                    id
                                )

                                canRequestAd = true
                                if (isHandlerAdDelayRunning) {
                                    dismissLoadingDialog()
                                    removeCallBacksDelay()
                                    showAdmobAd(context, key)
                                }
                            }

                            override fun onAdFailedToLoad(p0: LoadAdError) {
                                canRequestAd = true
                                handlerRemoveCallback(
                                    reason = "$placementKey ad failed to load code: ${p0.code} message: ${p0.message}"
                                )
                            }
                        })
                }

            } else {
                mInterstitialControllerListener?.onAdClosed(
                    reason = "$placementKey called onAdClosed because: App is minimized | Ad is disabled | Other Ad is showing | App is Purchased"
                )
            }
        } catch (e: Exception) {
            canRequestAd = true
            handlerRemoveCallback("$placementKey called onAdClosed because: Inter Exception")
        } catch (e: OutOfMemoryError) {
            canRequestAd = true
            handlerRemoveCallback("$placementKey called onAdClosed because: Inter Exception")
        }
    }

    private fun handlerRemoveCallback(reason: String) {
        if (isHandlerAdDelayRunning) {
            dismissLoadingDialog()
            removeCallBacksDelay()
            mInterstitialControllerListener?.onAdClosed(
                reason = reason
            )
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
        if (firebaseBoolean("INTER_LOADING_ENABLE", false)) {
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


    private fun setAdmobFullScreen(activity: Activity, key: String) {
        admobInterAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                dismissLoadingDialog()
                mInterstitialControllerListener?.onAdClosed(
                    isInterShowed = true,
                    reason = "$placementKey called onAdClosed because: ad is showed successfully"
                )
                super.onAdDismissedFullScreenContent()
                IS_INTERSTITIAL_Ad_SHOWING = false
                admobInterAd = null
                if (key.isEmpty() && !firebaseBoolean(
                        "${placementKey}_isInterInstant",
                        false
                    )
                ) {
                    loadInter(activity)
                }
            }

            override fun onAdImpression() {
                super.onAdImpression()
                postAdImpression("InterstitialAd")
            }

            override fun onAdShowedFullScreenContent() {
                super.onAdShowedFullScreenContent()
                IS_INTERSTITIAL_Ad_SHOWING = true
                admobInterAd = null
            }

            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                dismissLoadingDialog()
                mInterstitialControllerListener?.onAdClosed(
                    reason = "$placementKey called onAdClosed because: onAdFailedToShowFullScreenContent code: ${p0.code} message: ${p0.message}"
                )
                super.onAdFailedToShowFullScreenContent(p0)
                admobInterAd = null
                IS_INTERSTITIAL_Ad_SHOWING = false
            }

        }
    }

    private fun getInterCount(key: String, defValue: Int = 0): Int {
        return AdKit.adKitPref.getInterInt(key, defValue)
    }

    private fun setInterCount(key: String = "APP_INTER_COUNTER", count: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            AdKit.adKitPref.putInterInt(key, count)
        }
    }


    private val hasAd: Boolean
        get() {
            return admobInterAd != null
        }

}