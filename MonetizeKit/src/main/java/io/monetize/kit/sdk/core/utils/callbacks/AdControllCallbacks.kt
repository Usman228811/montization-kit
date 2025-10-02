package io.monetize.kit.sdk.core.utils.callbacks

interface AdCallBack{
    fun onAdFailed(reason: String)
    fun onAdClick()
}