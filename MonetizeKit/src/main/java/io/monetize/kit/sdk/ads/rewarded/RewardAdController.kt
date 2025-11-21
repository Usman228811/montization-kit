package io.monetize.kit.sdk.ads.rewarded

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import io.monetize.kit.sdk.ads.open.AdLoadingDialog
import io.monetize.kit.sdk.core.utils.IS_INTERSTITIAL_Ad_SHOWING
import io.monetize.kit.sdk.core.utils.appflyer.revenueListener
import io.monetize.kit.sdk.core.utils.firebaseBoolean
import io.monetize.kit.sdk.core.utils.firebaseLong
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


    private var placementKey: String = ""
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
            mInterstitialControllerListener?.onRewardDismissed(
                false,
                "$placementKey called onRewardDismissed because: INTER_INSTANT_TIME is completed"
            )
        }
    }


    private fun startDelayHandler() {
        var instantTime = firebaseLong("INTER_INSTANT_TIME", 8L)
        if (instantTime == 0L) {
            instantTime = 8
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
                mInterstitialControllerListener?.onRewardDismissed(
                    false,
                    "$placementKey called onRewardDismissed because: App is minimized | Other Ad is showing"
                )
            }
        } catch (exception: Exception) {
            mInterstitialControllerListener?.onRewardDismissed(
                false,
                "$placementKey called onRewardDismissed because: Reward Ad Exception"
            )

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
        this.placementKey = placementKey
        this.adIdKey = adIdKey
        mInterstitialControllerListener = listener
        val savedCount = getInterCount(key)
        if (AdKit.adKitPref.isAppPurchased || !enable || AdKit.interHelper.getAppInPause() || IS_INTERSTITIAL_Ad_SHOWING) {
            listener.onRewardDismissed(
                false,
                reason = "$placementKey called onRewardDismissed because: App is minimized | Ad is disabled | Other Ad is showing | App is Purchased"
            )
        } else if (savedCount == -1 || savedCount >= counter) {
            if (rewardAd != null) {
                checkProgressShowAd(context, key)
            } else {
                loadAndShow(
                    context,
                    this@RewardAdController.placementKey, adIdKey, true, key, listener
                )
            }
        } else if ((savedCount + 1).toLong() >= counter) {
            listener.onRewardDismissed(
                false,
                reason = "$placementKey called onRewardDismissed because: counter is not completed"
            )
            setInterCount(key, savedCount + 1)
        } else {
            listener.onRewardDismissed(
                false,
                reason = "$placementKey called onRewardDismissed because: counter is not completed"
            )
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
        this.placementKey = placementKey
        this.adIdKey = adIdKey
        if (AdKit.adKitPref.isAppPurchased || !enable || AdKit.interHelper.getAppInPause() || IS_INTERSTITIAL_Ad_SHOWING) {
            listener.onRewardDismissed(
                false,
                reason = "$placementKey called onRewardDismissed because: App is minimized | Ad is disabled | Other Ad is showing | App is Purchased"
            )
        } else {
            if (rewardAd != null) {
                checkProgressShowAd(context)
            } else {
                listener.onRewardDismissed(
                    false,
                    reason = "$placementKey called onRewardDismissed because: Ad is null"
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
        listener: RewardedControllerListener, key: String, counter: Long,
    ) {
        isUserEarnReward = false
        mInterstitialControllerListener = listener
        this.placementKey = placementKey
        this.adIdKey = adIdKey
        val savedCount = getInterCount(key)
        if (AdKit.adKitPref.isAppPurchased || !enable || AdKit.interHelper.getAppInPause() || IS_INTERSTITIAL_Ad_SHOWING) {
            listener.onRewardDismissed(
                false,
                reason = "$placementKey called onRewardDismissed because: App is minimized | Ad is disabled | Other Ad is showing | App is Purchased"
            )
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
                listener.onRewardDismissed(
                    false,
                    reason = "$placementKey called onRewardDismissed because: counter is complete but no ad is available"
                )

            }
        } else if ((savedCount + 2).toLong() >= counter) {
            listener.onRewardDismissed(
                false,
                reason = "$placementKey called onRewardDismissed because: counter is not completed"
            )
            loadInter(context)
            setInterCount(key, savedCount + 1)
        } else {
            listener.onRewardDismissed(
                false,
                reason = "$placementKey called onRewardDismissed because: counter is not completed"
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
                            rewardAd?.revenueListener(AdKit.rewardAdIdManager.getNextRewardId(adIdKey) ?: "",)

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
        this.placementKey = placementKey
        this.adIdKey = adIdKey
        mInterstitialControllerListener = listener
        try {
            if (!AdKit.adKitPref.isAppPurchased && AdKit.internetController.isConnected && enable && AdKit.consentManager.canRequestAds) {
                if (!canRequestAd) {
                    mInterstitialControllerListener?.onRewardDismissed(
                        false,
                        reason = "$placementKey called onRewardDismissed because: Other ad is being request"
                    )
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
                                handlerRemoveCallback(
                                    reason = "ad failed to load code: ${adError.code} message: ${adError.message}"

                                )
                            }
                        },
                    )
                }


            } else {
                mInterstitialControllerListener?.onRewardDismissed(
                    false,
                    reason = "$placementKey called onRewardDismissed because:  internet connection | consent manager | app purchased | ad is disable in remote config"

                )
            }
        } catch (e: Exception) {
            canRequestAd = true
            handlerRemoveCallback("Reward Ad Exception")
        } catch (e: OutOfMemoryError) {
            canRequestAd = true
            handlerRemoveCallback("Reward Ad Exception")
        }
    }

    private fun handlerRemoveCallback(reason: String) {
        if (isHandlerAdDelayRunning) {
            dismissLoadingDialog()
            removeCallBacksDelay()
            mInterstitialControllerListener?.onRewardDismissed(
                false,
                "$placementKey called onRewardDismissed because: $reason"
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
                    mInterstitialControllerListener?.onRewardDismissed(
                        isUserEarnReward,
                        reason = "$placementKey called onRewardDismissed because: ad is showed successfully"
                    )
                    super.onAdDismissedFullScreenContent()
                    IS_INTERSTITIAL_Ad_SHOWING = false
                    rewardAd = null
                    if (key.isEmpty() && !firebaseBoolean(
                            "${placementKey}_isRewardInstant",
                            false
                        )
                    ) {
                        loadInter(activity)
                    }
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    dismissLoadingDialog()
                    mInterstitialControllerListener?.onRewardDismissed(
                        false,
                        reason = "$placementKey called onRewardDismissed because: onAdFailedToShowFullScreenContent code: ${adError.code} message: ${adError.message}"
                    )
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