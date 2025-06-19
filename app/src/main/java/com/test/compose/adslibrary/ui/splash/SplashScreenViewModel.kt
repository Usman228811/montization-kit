package com.test.compose.adslibrary.ui.splash

import android.animation.ValueAnimator
import android.app.Activity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.monetize.kit.sdk.ads.interstitial.AdSdkSplashAdController
import io.monetize.kit.sdk.ads.interstitial.InterstitialControllerListener
import io.monetize.kit.sdk.core.utils.in_app_update.AdSdkInAppUpdateManager
import io.monetize.kit.sdk.core.utils.in_app_update.UpdateState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class SplashScreenState(
    val runSplash: Boolean = false,
    val isAppResumed: Boolean = false,
    val moveToMain: Boolean = false,
    val progress: Int = 0,
    val updateState: UpdateState = UpdateState.Idle,
)

class SplashScreenViewModel(
    private val adSdkSplashAdController: AdSdkSplashAdController,
) : ViewModel() {

    private var _state = MutableStateFlow(SplashScreenState())
    val state = _state.asStateFlow()

    private var isInterAdShowed = false
    private var isInterAdCalled = false
    private var animator: ValueAnimator? = null

    init {
        adSdkSplashAdController.resetSplash()
        startProgressAnimation()

    }


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
            adSdkSplashAdController.pauseAd()
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
        adSdkSplashAdController.initSplashAdmob(
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