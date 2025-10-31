package io.monetize.kit.sdk.ads.native_ad

interface AdControllerListener {
    fun onAdLoaded()
    fun onAdFailed(reason: String)
    fun resetRequesting()
}
interface NativeRefreshListener {
    fun refreshNativeAd()
}
