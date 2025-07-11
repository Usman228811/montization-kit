package io.monetize.kit.sdk.ads.interstitial

import android.app.Activity
import io.monetize.kit.sdk.core.utils.init.AdKit

class AdKitInterHelper private constructor(
) {

    private var isAppInPause = false
    private var isInterInstant = false
    private var interAdsConfigs: InterAdsConfigs? = null


    fun getInterAdsConfigs(): InterAdsConfigs? {
        return interAdsConfigs
    }

    fun getInterInstant(): Boolean {
        return isInterInstant

    }

    companion object {
        @Volatile
        private var instance: AdKitInterHelper? = null


        internal fun getInstance(
        ): AdKitInterHelper {
            return instance ?: synchronized(this) {
                instance ?: AdKitInterHelper().also { instance = it }
            }
        }
    }


    fun setInterAdsConfigs(
        interAdsConfigs: InterAdsConfigs,
    ) {
        this.interAdsConfigs = interAdsConfigs
    }

    fun getAppInPause() = isAppInPause


    fun setAppInPause(isPause: Boolean) {
        this.isAppInPause = isPause
        AdKit.splashAdController.setAppInPause(isPause)
        AdKit.openAdManager.setAppInPause(isPause)
    }


    fun showInterAd(
        activity: Activity,
        placementKey: String,
        enable: Boolean,
        interInstant: Boolean = false,
        listener: InterstitialControllerListener, prefKey: String = "", counter: Long = -1L,
    ) {
        this.isInterInstant = interInstant
        if (!enable) {
            listener.onAdClosed()
            return
        }

        if (AdKit.splashAdController.hasAd()) {
            AdKit.splashAdController.showInterstitial(
                activity, true, object : InterstitialControllerListener {
                    override fun onAdClosed() {
                        listener.onAdClosed()
                    }
                })
        } else {


            var interstitialController: InterstitialController? = null
            var index = singleInterList.indexOfFirst { it.key == placementKey }
            if (index == -1) {
                singleInterList.apply {
                    add(
                        InterAdSingleModel(
                            placementKey,
                            InterstitialController.getInstance()
                        )
                    )
                }
                index = singleInterList.indexOfFirst { it.key == placementKey }
            }
            if (index != -1) {
                interstitialController = singleInterList[index].controller
            }


            if (interInstant.not()) {

                if (counter != -1L) {
                    interstitialController?.showWithCounter(
                        context = activity,
                        placementKey = placementKey,
                        enable = enable,
                        listener = listener,
                        key = prefKey,
                        counter = counter
                    )

                } else {
                    interstitialController?.showWithoutCounter(
                        context = activity,
                        placementKey = placementKey,
                        enable = enable,
                        listener = listener,
                    )
                }
            } else {
                if (counter != -1L) {
                    interstitialController?.loadAndShowWithCounter(
                        context = activity,
                        placementKey = placementKey,
                        enable = enable,
                        listener = listener,
                        key = prefKey,
                        counter = counter
                    )
                } else {
                    interstitialController?.loadAndShow(
                        context = activity,
                        placementKey = placementKey,
                        enable = true,
                        listener = listener,
                    )
                }
            }
        }
    }
}