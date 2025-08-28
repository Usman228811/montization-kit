package io.monetize.kit.sdk.ads.interstitial

interface InterstitialControllerListener {
    fun onAdClosed(isInterShowed: Boolean = false)
    fun onAdLoaded() {}
    fun onAdShow() {}
}