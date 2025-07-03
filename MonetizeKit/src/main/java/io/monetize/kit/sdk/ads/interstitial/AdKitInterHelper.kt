package io.monetize.kit.sdk.ads.interstitial

import android.app.Activity
import android.content.Context
import io.monetize.kit.sdk.ads.open.AdKitOpenAdManager

class AdKitInterHelper private constructor(
    private val splashController: AdKitSplashAdController,
    private val interstitialController: InterstitialController,
    private val adKitOpenAdManager: AdKitOpenAdManager
) {


    companion object {
        @Volatile
        private var instance: AdKitInterHelper? = null


        fun getInstance(
            context: Context,
        ): AdKitInterHelper {
            return instance ?: synchronized(this) {
                instance ?: AdKitInterHelper(
                    AdKitSplashAdController.getInstance(context),
                    InterstitialController.getInstance(context),
                    AdKitOpenAdManager.getInstance(context),
                ).also { instance = it }
            }
        }
    }


    fun setAdIds(
        splashId: String,
        appInterIds: List<String>,
        interControllerConfig: InterControllerConfig
    ) {
        splashController.setSplashId(splashId, interControllerConfig)
        interstitialController.setAdIds(appInterIds, interControllerConfig)
    }


    fun setAppInPause(isPause: Boolean) {
        splashController.setAppInPause(isPause)
        interstitialController.setAppInPause(isPause)
        adKitOpenAdManager.setAppInPause(isPause)
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

        if (splashController.hasAd()) {
            splashController.showInterstitial(
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