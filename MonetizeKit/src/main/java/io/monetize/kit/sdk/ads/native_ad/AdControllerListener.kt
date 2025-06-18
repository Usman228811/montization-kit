package io.monetize.kit.sdk.ads.native_ad

interface AdControllerListener {
    fun onAdLoaded()
    fun onAdFailed()
    fun resetRequesting()
}
