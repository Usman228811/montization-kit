package io.monetize.kit.sdk.ads.interstitial

interface InterstitialControllerListener {
    fun onAdClosed()
    fun onAdLoaded() {}
    fun onAdShow() {}
}