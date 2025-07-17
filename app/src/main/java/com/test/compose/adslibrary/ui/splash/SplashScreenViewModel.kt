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
import com.test.compose.adslibrary.BuildConfig
import io.monetize.kit.sdk.ads.interstitial.InterAdsConfigs
import io.monetize.kit.sdk.ads.interstitial.InterstitialControllerListener
import io.monetize.kit.sdk.core.utils.in_app_update.UpdateState
import io.monetize.kit.sdk.core.utils.init.AdKit
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
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

//        val AdKit.splashAdController: AdKit.splashAdController =
//            AdKit.splashAdController.getInstance(context)
//        val adKitFirebaseRemoteConfigHelper: AdKitFirebaseRemoteConfigHelper =
//            AdKitFirebaseRemoteConfigHelper.getInstance()
//        val AdKit.inAppUpdateManager: AdKit.inAppUpdateManager =
//            AdKit.inAppUpdateManager.getInstance(context)
//        val AdKit.consentManager: AdKit.consentManager = AdKit.consentManager.getInstance(context)
//        val pref: AdKitPref = AdKitPref.getInstance(context)
//        val AdKit.internetController: AdKit.internetController =
//            AdKit.internetController.getInstance(context)
//        val  AdKit.purchaseHelper:  AdKit.purchaseHelper =  AdKit.purchaseHelper.getInstance(context)

        return SplashScreenViewModel() as T
    }
}

class SplashScreenViewModel(
) : ViewModel() {

    private var _state = MutableStateFlow(SplashScreenState())
    val state = _state.asStateFlow()

    private var isInterAdShowed = false
    private var isInterAdCalled = false
    private var animator: ValueAnimator? = null

    init {


        viewModelScope.apply {

            launch {
                AdKit.consentManager.googleConsent.collectLatest {
                    fetchFirebase()
                }
            }
        }


        AdKit.purchaseHelper.initBilling("one_time_purchase_id")



        viewModelScope.apply {

            launch {

                 AdKit.purchaseHelper.appPurchased.collectLatest { isPurchased ->

                }
            }

            launch {

                 AdKit.purchaseHelper.productPriceFlow.collectLatest {


                }
            }
        }




        AdKit.splashAdController.resetSplash()
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
                if (!AdKit.adKitPref.isAppPurchased && AdKit.internetController.isConnected) {
                    AdKit.consentManager.gatherConsent(activity)
                    if (AdKit.consentManager.canRequestAds) {
                        runSplash()
                    }
                } else {
                    runSplash()
                }
            }
        }
    }

    fun fetchFirebase(){
        AdKit.firebaseHelper.apply {
            viewModelScope.launch {
                configFetched.collectLatest {
                    try {
                        runSplash()
                    } catch (e: Exception) {
                        runSplash()
                    }
                }
            }

            fetchRemoteValues(BuildConfig.DEBUG) {
                bool("inter_btn_plant_isAdEnable", true)
                bool("inter_btn_plant_isInterInstant", true)
                bool("home_native_isAdEnable", true)
                bool("home_banner_isAdEnable", true)
                bool("home_banner_isCollapsible", true)
                bool("subscription_native_isAdEnable", true)
                long("home_native_adType", 1L)
                long("subscription_native_adType", 1L)
                long("SPLASH_TIME", 16)
            }

        }
    }


    fun checkUpdate(
        context: Context,
        launcher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>
    ) {

        AdKit.inAppUpdateManager.setUpdateStateCallback { updateState ->
            when (updateState) {
                UpdateState.Available -> {

                    AdKit.inAppUpdateManager.startUpdateFlow(launcher)
                }

                UpdateState.Downloaded -> {
                    /* show restart dialog
                     or
                    adSdkInAppUpdateManager.updateComplete()*/

                }

                UpdateState.Failed -> {
                   initConsent(context as Activity)

                }

                UpdateState.Idle -> {

                }
            }

        }

        AdKit.inAppUpdateManager.checkUpdate(context)

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
            AdKit.splashAdController.pauseAd()
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
        AdKit.splashAdController.initSplashAdmob(
            mContext,
            placementKey = "splash_inter",
            interAdsConfigs = InterAdsConfigs(
                openAdEnable = true,
                interLoadingEnable = true,
                openAdLoadingEnable = true,
            ),
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
        if (!isInterAdShowed && isInterAdCalled) {
            AdKit.splashAdController.resumeAd(activity, true)
        }
    }

    override fun onCleared() {
        super.onCleared()
        animator?.cancel()
    }
}