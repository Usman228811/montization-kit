package io.monetize.kit.sdk.ads.interstitial

import android.app.Activity
import android.content.Context
import io.monetize.kit.sdk.ads.open.AdKitOpenAdManager
import io.monetize.kit.sdk.core.utils.init.AdKit

class AdKitInterHelper private constructor(
    private val interstitialController: InterstitialController,
) {


    companion object {
        @Volatile
        private var instance: AdKitInterHelper? = null


        internal fun getInstance(
            context: Context,
        ): AdKitInterHelper {
            return instance ?: synchronized(this) {
                instance ?: AdKitInterHelper(
                    InterstitialController.getInstance(),
                ).also { instance = it }
            }
        }
    }


    fun setAdIds(
        splashId: String,
        appInterIds: List<String>,
        interControllerConfig: InterControllerConfig
    ) {
        AdKit.splashAdController.setSplashId(splashId, interControllerConfig)
        interstitialController.setAdIds(appInterIds, interControllerConfig)
    }


    fun setAppInPause(isPause: Boolean) {
        AdKit.splashAdController.setAppInPause(isPause)
        interstitialController.setAppInPause(isPause)
        AdKit.openAdManager.setAppInPause(isPause)
    }


    fun showInterAd(
        activity: Activity,
        enable: Boolean,
        interInstant: Boolean = false,
        listener: InterstitialControllerListener, key: String = "", counter: Long = -1L,
    ) {
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
            if (interInstant.not()) {

                if (counter != -1L) {
                    interstitialController.showWithCounter(
                        context = activity,
                        enable = enable,
                        listener = listener,
                        key = key,
                        counter = counter
                    )

                } else {
                    interstitialController.showWithoutCounter(
                        context = activity,
                        enable = enable,
                        listener = listener,
                    )
                }
            } else {
                if (counter != -1L) {
                    interstitialController.loadAndShowWithCounter(
                        context = activity,
                        enable = enable,
                        listener = listener,
                        key = key,
                        counter = counter
                    )
                } else {
                    interstitialController.loadAndShow(
                        context = activity,
                        enable = true,
                        listener = listener,
                    )
                }
            }
        }
    }
}