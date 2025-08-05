package io.monetize.kit.sdk.ads.rewarded

interface RewardedControllerListener {
    fun onRewardDismissed(isRewarded: Boolean)
    fun onAdLoaded() {}
    fun onAdShow() {}
}