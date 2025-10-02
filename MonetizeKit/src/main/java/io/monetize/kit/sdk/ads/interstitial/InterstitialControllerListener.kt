package io.monetize.kit.sdk.ads.interstitial

interface InterstitialControllerListener {
    fun onAdClosed(isInterShowed: Boolean = false, reason: String)
    fun onAdLoaded(reason: String) {}
    fun onAdShow() {}
}