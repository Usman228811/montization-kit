package io.monetize.kit.sdk.ads.rewarded

interface RewardedControllerListener {
    fun onRewardDismissed(isRewarded: Boolean, reason: String)
    fun onAdLoaded() {}
    fun onAdShow() {}
}