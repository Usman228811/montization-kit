package com.test.compose.adslibrary.xml.splash

import android.animation.ValueAnimator
import android.app.Activity
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.test.compose.adslibrary.BuildConfig
import com.test.compose.adslibrary.ui.settings.FEATURE_1
import com.test.compose.adslibrary.ui.settings.FEATURE_2
import com.test.compose.adslibrary.ui.settings.FEATURE_3
import com.test.compose.adslibrary.ui.settings.LIFE_TIME_ID
import com.test.compose.adslibrary.ui.settings.REMOVE_ADS_ID
import io.monetize.kit.sdk.ads.interstitial.InterstitialControllerListener
import io.monetize.kit.sdk.core.utils.firebaseLong
import io.monetize.kit.sdk.core.utils.in_app_update.UpdateState
import io.monetize.kit.sdk.core.utils.init.AdKit
import io.monetize.kit.sdk.core.utils.init.AdKit.adKitPref
import io.monetize.kit.sdk.core.utils.init.AdKit.consentManager
import io.monetize.kit.sdk.core.utils.init.AdKit.firebaseHelper
import io.monetize.kit.sdk.core.utils.init.AdKit.inAppUpdateManager
import io.monetize.kit.sdk.core.utils.init.AdKit.internetController
import io.monetize.kit.sdk.core.utils.init.AdKit.splashAdController
import io.monetize.kit.sdk.core.utils.purchase.BillingItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SplashScreenState(
    val isConsentManager: Boolean = false,
    val initializeSplash: Boolean = false,
    val fireBaseFetch: Boolean = false,
    val showRestartDialog: Boolean = false,
    val moveToMain: Boolean = false,
    val isPurchased: Boolean = false,
    val runSplash: Boolean = false,
    val progress: Int = 0
)


class SplashXmlViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        @Suppress("UNCHECKED_CAST")
        return SplashXmlViewModel(
        ) as T
    }
}


class SplashXmlViewModel : ViewModel() {
    private var _state = MutableStateFlow(SplashScreenState())
    val state = _state.asStateFlow()
    private var isInterAdShowed = false
    private var isInterAdCalled = false
    private var animator: ValueAnimator? = null


    init {
        AdKit.analytics.postAnalytics("Splash_launch")
        splashAdController.resetSplash()
        collections()
        startProgressAnimation()
//        purchaseHelper.initBilling(productId)
    }

    fun loadProducts(activity: Activity){
        AdKit.premiumHelper.initBilling(activity,
            items = listOf(
                BillingItem.Lifetime(LIFE_TIME_ID, BillingItem.Type.REMOVE_ADS),
                BillingItem.Subscription(REMOVE_ADS_ID, BillingItem.Type.REMOVE_ADS),
                BillingItem.Subscription(FEATURE_1, BillingItem.Type.FEATURE),
                BillingItem.Subscription(FEATURE_2, BillingItem.Type.FEATURE),
                BillingItem.Subscription(FEATURE_3, BillingItem.Type.FEATURE),
            )
        )
    }
    fun onResume(activity: Activity) {
        if (state.value.runSplash) {
            animator?.resume()
        }
        if (!isInterAdShowed && isInterAdCalled) {
            splashAdController.resumeAd(activity)
        }
    }

    fun onPause() {
        if (state.value.runSplash) {
            animator?.pause()
        }
        if (!isInterAdShowed && isInterAdCalled) {
            splashAdController.pauseAd()
        }
    }

    fun checkForUpdate(activity: Activity, launcher: ActivityResultLauncher<IntentSenderRequest>) {
        inAppUpdateManager.setUpdateStateCallback { updateState ->
            when (updateState) {
                UpdateState.Available -> inAppUpdateManager.startUpdateFlow(launcher)
                UpdateState.Downloaded -> inAppUpdateManager.updateComplete()
                UpdateState.Failed -> initConsent(activity)
                UpdateState.Idle -> {}
            }
        }
        inAppUpdateManager.checkUpdate(activity)
    }

    private fun collections() {
        viewModelScope.apply {
            launch {
                consentManager.googleConsent.collectLatest { initializeSplash() }
            }
            launch {
                firebaseHelper.apply {
                    configFetched.collectLatest {
                        try {
//                            assignRemoteValues(this)
                            runSplash()
                        } catch (e: Exception) {
                            runSplash()
                        }
                    }
                }
            }
            launch {
                AdKit.premiumHelper.premiumState.collectLatest { premiumState ->
                    Log.d("purchase_status", "premiumPurchases: ${premiumState.allPurchases}")
                    _state.update { it.copy(isPurchased = premiumState.isPremium) }
                }
            }
        }
    }

    fun initConsent(activity: Activity) {
        viewModelScope.launch {
            if (state.value.isConsentManager.not()) {
                _state.update { it.copy(isConsentManager = true) }
                if (!adKitPref.isAppPurchased && internetController.isConnected) {
                    consentManager.gatherConsent(activity)
                    if (consentManager.canRequestAds) {
                        initializeSplash()
                    }
                } else {
                    initializeSplash()
                }
            }
        }
    }

    private fun initializeSplash() {
        viewModelScope.launch {
            if (state.value.initializeSplash.not()) {
                _state.update { it.copy(initializeSplash = true) }
                fetchFirebase()
            }
        }
    }

    private fun fetchFirebase() {
        if (state.value.fireBaseFetch.not()) {
            _state.update { it.copy(fireBaseFetch = true) }
            firebaseHelper.fetchRemoteValues(isDebug = BuildConfig.DEBUG)
        }
    }

    private fun runSplash() {
        viewModelScope.launch {
            if (state.value.runSplash.not()) {
                _state.update { it.copy(runSplash = true) }
            }
        }
    }

    private fun startProgressAnimation() {
        animator?.cancel()
        animator = ValueAnimator.ofInt(0, 100).apply {
            duration = 25_000L
            addUpdateListener { animation ->
                val value = animation.animatedValue as? Int
                viewModelScope.launch {
                    _state.update { it.copy(progress = value ?: 50) }
                }
            }
            start()
        }
    }

    fun showSplashAd(mContext: Activity) {
        if (!isInterAdCalled) {
            isInterAdCalled = true

            splashAdController.initSplashInterstitial(
                placementKey = "splash_inter",
                adIdKey = "splash_inter",
                activity = mContext,
                splashTime = firebaseLong("splash_time", 16),
                listener = object : InterstitialControllerListener {
                    override fun onAdShow() {
                        super.onAdShow()
                        isInterAdShowed = true
                        animator?.cancel()
                        viewModelScope.launch {
                            _state.update { it.copy(progress = 100) }
                        }
                    }

                    override fun onAdClosed(isInterShowed: Boolean, reason: String) {
                        Log.d("dddddd", reason)
                        animator?.cancel()

                        _state.update {
                            it.copy(progress = 100, moveToMain = true)
                        }
                    }
                }
            )
        }
    }


    override fun onCleared() {
        super.onCleared()
        inAppUpdateManager.unRegisterLister()
        animator?.cancel()
    }
}
