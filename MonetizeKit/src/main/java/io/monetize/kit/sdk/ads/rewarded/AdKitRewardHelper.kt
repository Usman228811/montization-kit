package io.monetize.kit.sdk.ads.rewarded

import android.app.Activity
import io.monetize.kit.sdk.core.utils.firebaseBoolean
import io.monetize.kit.sdk.core.utils.init.AdKit

class AdKitRewardHelper private constructor(
) {

    private var isAdEnable = false
    private var isRewardInstant = true


    companion object {
        @Volatile
        private var instance: AdKitRewardHelper? = null


        internal fun getInstance(
        ): AdKitRewardHelper {
            return instance ?: synchronized(this) {
                instance ?: AdKitRewardHelper().also { instance = it }
            }
        }
    }


    fun preLoadRewardAd(
        activity: Activity,
        placementKey: String,
        adIdKey: String,
        prefKey: String = "", counter: Long = -1L,
    ) {
        if (AdKit.initializer.getDisableAds()) {
            return
        }
        this.isRewardInstant =
            firebaseBoolean("${placementKey}_isRewardInstant", true)
        this.isAdEnable = firebaseBoolean("${placementKey}_isAdEnable", false)
        if (isAdEnable.not()) {
            return
        }

        var interstitialController: RewardAdController? = null
        var index = singleRewardAdList.indexOfFirst { it.key == adIdKey }
        if (index == -1) {
            singleRewardAdList.apply {
                add(
                    RewardAdSingleModel(
                        adIdKey,
                        RewardAdController.getInstance()
                    )
                )
            }
            index = singleRewardAdList.indexOfFirst { it.key == adIdKey }
        }
        if (index != -1) {
            interstitialController = singleRewardAdList[index].controller
        }
        if (isRewardInstant.not()) {
            interstitialController?.preLoadInter(
                activity,
                placementKey,
                adIdKey,
                isAdEnable,
                prefKey,
                counter
            )
        }
    }

    fun showRewardAd(
        activity: Activity,
        placementKey: String,
        adIdKey: String,
        listener: RewardedControllerListener, prefKey: String = "", counter: Long = -1L,
    ) {
        if (AdKit.initializer.getDisableAds()) {
            listener.onRewardDismissed(false, "ads are disabled in app class")
            return
        }
        this.isRewardInstant =
            firebaseBoolean("${placementKey}_isRewardInstant", true)
        this.isAdEnable = firebaseBoolean("${placementKey}_isAdEnable", false)
        if (!isAdEnable) {
            listener.onRewardDismissed(
                false,
                "$placementKey ad is disable or not added in remote config"
            )
            return
        }


        val adListener = object : RewardedControllerListener {
            override fun onRewardDismissed(isRewarded: Boolean, reason: String) {
                activity.runOnUiThread { listener.onRewardDismissed(isRewarded, reason) }
            }

            override fun onAdLoaded() {
                super.onAdLoaded()
                activity.runOnUiThread { listener.onAdLoaded() }

            }

            override fun onAdShow() {
                super.onAdShow()
                activity.runOnUiThread { listener.onAdShow() }

            }


        }

        var interstitialController: RewardAdController? = null
        var index = singleRewardAdList.indexOfFirst { it.key == adIdKey }
        if (index == -1) {
            singleRewardAdList.apply {
                add(
                    RewardAdSingleModel(
                        adIdKey,
                        RewardAdController.Companion.getInstance()
                    )
                )
            }
            index = singleRewardAdList.indexOfFirst { it.key == adIdKey }
        }
        if (index != -1) {
            interstitialController = singleRewardAdList[index].controller
        }


        if (isRewardInstant.not()) {

            if (counter != -1L) {
                interstitialController?.showWithCounter(
                    context = activity,
                    placementKey = placementKey,
                    adIdKey = adIdKey,
                    enable = isAdEnable,
                    listener = adListener,
                    key = prefKey,
                    counter = counter
                )

            } else {
                interstitialController?.showWithoutCounter(
                    context = activity,
                    placementKey = placementKey,
                    adIdKey = adIdKey,
                    enable = isAdEnable,
                    listener = adListener,
                )
            }
        } else {
            if (counter != -1L) {
                interstitialController?.loadAndShowWithCounter(
                    context = activity,
                    placementKey = placementKey,
                    adIdKey = adIdKey,
                    enable = isAdEnable,
                    listener = adListener,
                    key = prefKey,
                    counter = counter
                )
            } else {
                interstitialController?.loadAndShow(
                    context = activity,
                    placementKey = placementKey,
                    adIdKey = adIdKey,
                    enable = true,
                    listener = adListener,
                )
            }
        }
    }
}