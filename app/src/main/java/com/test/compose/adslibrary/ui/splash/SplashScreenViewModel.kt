package com.test.compose.adslibrary.ui.splash

import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.monetize.kit.sdk.ads.interstitial.AdKitSplashAdController
import io.monetize.kit.sdk.ads.interstitial.InterstitialControllerListener
import io.monetize.kit.sdk.core.utils.AdKitInternetController
import io.monetize.kit.sdk.core.utils.AdKitPref
import io.monetize.kit.sdk.core.utils.consent.AdKitConsentManager
import io.monetize.kit.sdk.core.utils.in_app_update.AdKitInAppUpdateManager
import io.monetize.kit.sdk.core.utils.in_app_update.UpdateState
import io.monetize.kit.sdk.core.utils.purchase.AdKitPurchaseHelper
import io.monetize.kit.sdk.core.utils.remoteconfig.AdKitFirebaseRemoteConfigHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class SplashScreenState(
    val runSplash: Boolean = false,
    val isAppResumed: Boolean = false,
    val moveToMain: Boolean = false,
    val isConsentManager: Boolean = false,
    val progress: Int = 0,
    val updateState: UpdateState = UpdateState.Idle,
)

class SplashScreenViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        val adKitSplashAdController: AdKitSplashAdController =
            AdKitSplashAdController.getInstance(context)
        val adKitFirebaseRemoteConfigHelper: AdKitFirebaseRemoteConfigHelper =
            AdKitFirebaseRemoteConfigHelper.getInstance()
        val adKitInAppUpdateManager: AdKitInAppUpdateManager =
            AdKitInAppUpdateManager.getInstance(context)
        val adKitConsentManager: AdKitConsentManager = AdKitConsentManager.getInstance(context)
        val pref: AdKitPref = AdKitPref.getInstance(context)
        val adKitInternetController: AdKitInternetController =
            AdKitInternetController.getInstance(context)
        val adKitPurchaseHelper: AdKitPurchaseHelper = AdKitPurchaseHelper.getInstance(context)

        return SplashScreenViewModel(
            adKitSplashAdController,
            adKitFirebaseRemoteConfigHelper,
            adKitInAppUpdateManager,
            adKitConsentManager,
            pref,
            adKitInternetController,
            adKitPurchaseHelper,

            ) as T
    }
}

class SplashScreenViewModel(
    private val adKitSplashAdController: AdKitSplashAdController,
    private val adKitFirebaseRemoteConfigHelper: AdKitFirebaseRemoteConfigHelper,
    private val adKitInAppUpdateManager: AdKitInAppUpdateManager,
    private val adKitConsentManager: AdKitConsentManager,
    private val pref: AdKitPref,
    private val adKitInternetController: AdKitInternetController,
    private val adKitPurchaseHelper: AdKitPurchaseHelper,
) : ViewModel() {

    private var _state = MutableStateFlow(SplashScreenState())
    val state = _state.asStateFlow()

    private var isInterAdShowed = false
    private var isInterAdCalled = false
    private var animator: ValueAnimator? = null

    init {


        viewModelScope.apply {

            launch {
                adKitConsentManager.googleConsent.collectLatest {
                    runSplash()
                }
            }
        }


        adKitPurchaseHelper.initBilling("one_time_purchase_id")



        viewModelScope.apply {

            launch {

                adKitPurchaseHelper.appPurchased.collectLatest { isPurchased ->

                }
            }

            launch {

                adKitPurchaseHelper.productPriceFlow.collectLatest {


                }
            }
        }

//        firebaseRemoteConfigHelper.apply {
//            viewModelScope.launch {
//                configFetched.collectLatest {
//                    try {
//                        val SPLASH_TIME = getLong("SPLASH_TIME", 16L)
//                        val HOME_NATIVE_ENABLE = getBoolean("HOME_NATIVE_ENABLE", true)
//                        val IS_AI = getString("IS_AI", "YES")
//
//                        runSplash()
//                    } catch (e: Exception) {
//                        runSplash()
//                    }
//                }
//            }
//
//            fetchRemoteValues(
//                BuildConfig.DEBUG,
//                mapOf(
//                    "SPLASH_TIME" to 16,
//                    "HOME_NATIVE_ENABLE" to true,
//                )
//            )
//
//        }


        adKitSplashAdController.resetSplash()
        startProgressAnimation()

    }

    fun initConsent(activity: Activity) {
        viewModelScope.launch {
            if (state.value.isConsentManager.not()) {
                _state.update {
                    it.copy(
                        isConsentManager = true
                    )
                }
                if (!pref.isAppPurchased && adKitInternetController.isConnected) {
                    adKitConsentManager.gatherConsent(activity)
                    if (adKitConsentManager.canRequestAds) {
                        runSplash()
                    }
                } else {
                    runSplash()
                }
            }
        }
    }


    fun checkUpdate(launcher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>) {

        adKitInAppUpdateManager.setUpdateStateCallback { updateState ->
            when (updateState) {
                UpdateState.Available -> {

                    adKitInAppUpdateManager.startUpdateFlow(launcher)
                }

                UpdateState.Downloaded -> {
                    /* show restart dialog
                     or
                    adSdkInAppUpdateManager.updateComplete()*/

                }

                UpdateState.Failed -> {
                    //continue

                }

                UpdateState.Idle -> {

                }
            }

        }

        adKitInAppUpdateManager.checkUpdate()

    }

//    override fun onCleared() {
//        super.onCleared()
//        inAppUpdateManager.unRegisterLister()
//        animator?.cancel()
//    }


    private fun onResume() {
        if (state.value.runSplash) {
            animator?.resume()
        }
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isAppResumed = true
                )
            }
        }
    }

    private fun onPause() {
        if (state.value.runSplash) {
            animator?.pause()
        }
        if (!isInterAdShowed && isInterAdCalled) {
            adKitSplashAdController.pauseAd()
        }
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isAppResumed = false
                )
            }
        }
    }

    fun observeLifecycle(lifecycleOwner: LifecycleOwner) {
        val lifecycleObserver = LifecycleEventObserver { a, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> onResume()
                Lifecycle.Event.ON_PAUSE -> onPause()
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)

        // Ensure observer is removed when lifecycle is destroyed
        lifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
            }
        })
    }


    fun runSplash() {
        viewModelScope.launch {
            if (state.value.runSplash.not()) {
                _state.update {
                    it.copy(
                        runSplash = true
                    )
                }
            }
        }
    }


    private fun startProgressAnimation() {
        animator?.cancel()
        animator = ValueAnimator.ofInt(0, 100).apply {
            duration = 25_000L
            addUpdateListener { animation ->
                val value = animation.animatedValue as Int
                viewModelScope.launch {
                    _state.update {
                        it.copy(
                            progress = value
                        )
                    }
                }
            }
            start()
        }
    }

    fun showSplashAd(mContext: Activity) {

        animator?.cancel()
        adKitSplashAdController.initSplashAdmob(
            mContext,
            true,
            object : InterstitialControllerListener {
                override fun onAdClosed() {
                    _state.update {
                        it.copy(
                            progress = 100,
                            moveToMain = true,
                        )
                    }
                }

            }
        )


    }

    fun resumeSplashAd(activity: Activity) {
//        if (!isInterAdShowed && isInterAdCalled) {
//            splashAdController.resumeAd(activity, true)
//        }
    }

    override fun onCleared() {
        super.onCleared()
        animator?.cancel()
    }
}