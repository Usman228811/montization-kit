package io.monetize.kit.sdk.ads.rewarded

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import io.monetize.kit.sdk.ads.open.AdLoadingDialog
import io.monetize.kit.sdk.core.utils.IS_INTERSTITIAL_Ad_SHOWING
import io.monetize.kit.sdk.core.utils.init.AdKit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


data class RewardAdSingleModel(
    val key: String = "",
    val controller: RewardAdController? = null,
)

val singleRewardAdList = ArrayList<RewardAdSingleModel>()

class RewardAdController private constructor(
) {
    companion object {
        fun getInstance(
        ): RewardAdController {
            return RewardAdController()
        }
    }


    private var btnKey: String = ""
    private var adIdKey: String = ""
    private var handlerAd = Handler(Looper.getMainLooper())
    private var canRequestAd = true
    private var isUserEarnReward = false
    private var rewardAd: RewardedAd? = null
    private var mInterstitialControllerListener: RewardedControllerListener? = null
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
            mInterstitialControllerListener?.onRewardDismissed(false)
        }
    }


    private fun startDelayHandler() {
        val instantTime = AdKit.interHelper.getInterAdsConfigs()?.instantInterTime ?: 8L
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

    private fun showRewardAd(activity: Activity, key: String) {
        try {
            if (rewardAd != null && !AdKit.interHelper.getAppInPause() && !IS_INTERSTITIAL_Ad_SHOWING) {
                mInterstitialControllerListener?.onAdShow()
                if (rewardAd != null) {
                    setAdmobFullScreen(activity, key)
                    rewardAd?.show(activity) { rewardItem ->
                        isUserEarnReward = true
                    }
                }
                if (key != "") {
                    setInterCount(key, 0)
                }
            } else {
                mInterstitialControllerListener?.onRewardDismissed(false)
            }
        } catch (exception: Exception) {
            mInterstitialControllerListener?.onRewardDismissed(false)
        }
    }

    fun loadAndShowWithCounter(
        context: Activity,
        placementKey: String,
        adIdKey: String,
        enable: Boolean,
        listener: RewardedControllerListener, key: String, counter: Long,
    ) {

        isUserEarnReward = false
        this.btnKey = placementKey
        this.adIdKey = adIdKey
        mInterstitialControllerListener = listener
        val savedCount = getInterCount(key)
        if (AdKit.adKitPref.isAppPurchased || !enable || AdKit.interHelper.getAppInPause() || IS_INTERSTITIAL_Ad_SHOWING) {
            listener.onRewardDismissed(false)
        } else if (savedCount == -1 || savedCount >= counter) {
            if (rewardAd != null) {
                checkProgressShowAd(context, key)
            } else {
                loadAndShow(context, btnKey, adIdKey, true, key, listener)
            }
        } else if ((savedCount + 1).toLong() >= counter) {
            listener.onRewardDismissed(false)
            setInterCount(key, savedCount + 1)
        } else {
            listener.onRewardDismissed(false)
            setInterCount(key, savedCount + 1)
        }
    }

    fun showWithoutCounter(
        context: Activity,
        placementKey: String,
        adIdKey: String,
        enable: Boolean,
        listener: RewardedControllerListener
    ) {
        isUserEarnReward = false
        mInterstitialControllerListener = listener
        this.btnKey = placementKey
        this.adIdKey = adIdKey
        if (AdKit.adKitPref.isAppPurchased || !enable || AdKit.interHelper.getAppInPause() || IS_INTERSTITIAL_Ad_SHOWING) {
            listener.onRewardDismissed(false)
        } else {
            if (rewardAd != null) {
                checkProgressShowAd(context)
            } else {
                listener.onRewardDismissed(false)
                loadInter(context)
            }
        }
    }

    fun showWithCounter(
        context: Activity,
        placementKey: String,
        adIdKey: String,
        enable: Boolean,
        listener: RewardedControllerListener, key: String, counter: Long,
    ) {
        isUserEarnReward = false
        mInterstitialControllerListener = listener
        this.btnKey = placementKey
        this.adIdKey = adIdKey
        val savedCount = getInterCount(key)
        if (AdKit.adKitPref.isAppPurchased || !enable || AdKit.interHelper.getAppInPause() || IS_INTERSTITIAL_Ad_SHOWING) {
            listener.onRewardDismissed(false)
        } else if (savedCount == -1 || savedCount >= counter) {
            if (rewardAd != null) {
                checkProgressShowAd(context, key)
            } else {

                if ((savedCount + 2).toLong() >= counter) {
                    loadInter(context)
                }

                if (savedCount == -1) {
                    setInterCount(key, 1)
                }
                listener.onRewardDismissed(false)

            }
        } else if ((savedCount + 2).toLong() >= counter) {
            listener.onRewardDismissed(false)
            loadInter(context)
            setInterCount(key, savedCount + 1)
        } else {
            listener.onRewardDismissed(false)
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
        this.btnKey = placementKey
        this.adIdKey = adIdKey
        if (rewardAd != null) {
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

                RewardedAd.load(
                    context,
                    AdKit.rewardAdIdManager.getNextRewardId(adIdKey) ?: "",
                    AdRequest.Builder().build(),
                    object : RewardedAdLoadCallback() {
                        override fun onAdLoaded(ad: RewardedAd) {
                            rewardAd = ad
                            canRequestAd = true
                        }

                        override fun onAdFailedToLoad(adError: LoadAdError) {
                            rewardAd = null
                            canRequestAd = true
                        }
                    },
                )
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
        listener: RewardedControllerListener,
    ) {

        isUserEarnReward = false
        this.btnKey = placementKey
        this.adIdKey = adIdKey
        mInterstitialControllerListener = listener
        try {
            if (!AdKit.adKitPref.isAppPurchased && AdKit.internetController.isConnected && enable && AdKit.consentManager.canRequestAds) {
                if (!canRequestAd) {
                    mInterstitialControllerListener?.onRewardDismissed(false)
                    return
                }
                if (rewardAd != null) {
                    checkProgressShowAd(context, key)
                } else {
                    canRequestAd = false
                    dismissLoadingDialog()
                    adLoadingDialog = AdLoadingDialog(context)
                    adLoadingDialog?.showAlertDialog()
                    startDelayHandler()
                    Log.d("ioioioi", "onAdFailedToLoad: ${AdKit.rewardAdIdManager.getNextRewardId(adIdKey) ?: ""}")

                    RewardedAd.load(
                        context,
                        AdKit.rewardAdIdManager.getNextRewardId(adIdKey) ?: "",
                        AdRequest.Builder().build(),
                        object : RewardedAdLoadCallback() {
                            override fun onAdLoaded(ad: RewardedAd) {

                                rewardAd = ad
                                canRequestAd = true
                                if (isHandlerAdDelayRunning) {
                                    dismissLoadingDialog()
                                    removeCallBacksDelay()
                                    showRewardAd(context, key)
                                }
                            }

                            override fun onAdFailedToLoad(adError: LoadAdError) {
                                canRequestAd = true
                                Log.d("ioioioi", "onAdFailedToLoad: $adError")
                                handlerRemoveCallback(context)
                            }
                        },
                    )
                }


            } else {
                mInterstitialControllerListener?.onRewardDismissed(false)
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
            mInterstitialControllerListener?.onRewardDismissed(false)
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
        if (AdKit.interHelper.getInterAdsConfigs()?.interLoadingEnable != false) {
            try {
                mInterstitialControllerListener?.onAdShow()
                val adLoadingDialog = AdLoadingDialog(activity)
                adLoadingDialog.showAlertDialog()
                handlerAd.postDelayed({
                    showRewardAd(activity, key)
                    adLoadingDialog.dismissAlertDialog()
                }, 1000)
            } catch (e: Exception) {
                showRewardAd(activity, key)
            }
        } else {
            showRewardAd(activity, key)
        }
    }


    private fun setAdmobFullScreen(activity: Activity, key: String) {

        rewardAd?.fullScreenContentCallback =
            object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    dismissLoadingDialog()
                    mInterstitialControllerListener?.onRewardDismissed(isUserEarnReward)
                    super.onAdDismissedFullScreenContent()
                    IS_INTERSTITIAL_Ad_SHOWING = false
                    rewardAd = null
                    if (key.isEmpty() && AdKit.interHelper.getInterInstant().not()) {
                        loadInter(activity)
                    }
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    dismissLoadingDialog()
                    mInterstitialControllerListener?.onRewardDismissed(false)
                    super.onAdFailedToShowFullScreenContent(adError)
                    rewardAd = null
                    IS_INTERSTITIAL_Ad_SHOWING = false
                }

                override fun onAdShowedFullScreenContent() {
                    IS_INTERSTITIAL_Ad_SHOWING = true
                    rewardAd = null
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
            return rewardAd != null
        }
}