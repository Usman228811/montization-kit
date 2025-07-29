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
import com.test.compose.adslibrary.ui.splash.events.SplashOneTimeEventEvents
import com.test.compose.adslibrary.ui.splash.events.SplashScreenEvents
import com.test.compose.adslibrary.ui.splash.state.SplashScreenState
import io.monetize.kit.sdk.ads.interstitial.InterAdsConfigs
import io.monetize.kit.sdk.ads.interstitial.InterstitialControllerListener
import io.monetize.kit.sdk.core.utils.in_app_update.UpdateState
import io.monetize.kit.sdk.core.utils.init.AdKit
import io.monetize.kit.sdk.core.utils.init.AdKit.inAppUpdateManager
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class SplashScreenViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SplashScreenViewModel() as T
    }
}

class SplashScreenViewModel : ViewModel() {

    private var _state = MutableStateFlow(SplashScreenState())
    val state = _state.asStateFlow()

    private val _oneTimeEvent = Channel<SplashOneTimeEventEvents>()
    val oneTimeEvent = _oneTimeEvent.receiveAsFlow()



    private var isInterAdShowed = false
    private var isInterAdCalled = false
    private var animator: ValueAnimator? = null

    init {


        viewModelScope.apply {

            launch {
                AdKit.consentManager.googleConsent.collectLatest {
                    onEvent(SplashScreenEvents.FireBaseFetch)
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
                        onEvent(SplashScreenEvents.FireBaseFetch)
                    }
                } else {
                    onEvent(SplashScreenEvents.FireBaseFetch)
                }
            }
        }
    }

    private fun fetchFirebase() {

        AdKit.firebaseHelper.apply {
            viewModelScope.launch {
                configFetched.collectLatest {
                    try {
                        onEvent(SplashScreenEvents.RunSplash)
                    } catch (e: Exception) {
                        onEvent(SplashScreenEvents.RunSplash)
                    }
                }
            }

            fetchRemoteValues(BuildConfig.DEBUG)

        }
    }

    fun onEvent(splashScreenEvents: SplashScreenEvents) {
        when (splashScreenEvents) {
            is SplashScreenEvents.AppResumed -> {
                resumeSplashAd(splashScreenEvents.mContext)
            }

            is SplashScreenEvents.CheckUpdate -> {
                checkUpdate(
                    context = splashScreenEvents.context,
                    launcher = splashScreenEvents.launcher
                )

            }

            is SplashScreenEvents.ShowSplashAd -> {
                showSplashAd(splashScreenEvents.mContext)

            }

            is SplashScreenEvents.CheckConsent -> {
                initConsent(splashScreenEvents.mContext)
            }

            SplashScreenEvents.FireBaseFetch -> {
                fetchFirebase()
            }

            SplashScreenEvents.RunSplash -> {
                runSplash()
            }
        }

    }

    fun sendOneTimeEvent(events: SplashOneTimeEventEvents){
        when(events){
            SplashOneTimeEventEvents.MoveToMain -> {
                viewModelScope.launch {
                    _oneTimeEvent.send(events)
                }
            }
        }
    }


    private fun checkUpdate(
        context: Context,
        launcher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>
    ) {

        inAppUpdateManager.setUpdateStateCallback { updateState ->
            when (updateState) {
                UpdateState.Available -> {

                    inAppUpdateManager.startUpdateFlow(launcher)
                }

                UpdateState.Downloaded -> {
                    /* show restart dialog
                     or
                    adSdkInAppUpdateManager.updateComplete()*/

                }

                UpdateState.Failed -> {
                    onEvent(SplashScreenEvents.CheckConsent(context as Activity))

                }

                UpdateState.Idle -> {

                }
            }

        }

        inAppUpdateManager.checkUpdate(context)

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


    private fun runSplash() {
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

    private fun showSplashAd(mContext: Activity) {

        animator?.cancel()
        AdKit.splashAdController.initSplashAdmob(
            mContext,
            placementKey = "splash_inter",
            adIdKey = "splash_inter",
            interAdsConfigs = InterAdsConfigs(
                openAdEnable = true,
                interLoadingEnable = true,
                openAdInstant = false,
                openAdLoadingEnable = true,
                splashTime = AdKit.firebaseHelper.getLong("splash_time", 16)
            ),
            object : InterstitialControllerListener {
                override fun onAdClosed() {
                    _state.update {
                        it.copy(
                            progress = 100,
                        )
                    }
                    sendOneTimeEvent(SplashOneTimeEventEvents.MoveToMain)
                }


            }
        )


    }

    private fun resumeSplashAd(activity: Activity) {
        if (!isInterAdShowed && isInterAdCalled) {
            AdKit.splashAdController.resumeAd(activity, true)
        }
    }

    override fun onCleared() {
        super.onCleared()
        animator?.cancel()
    }
}