package com.test.compose.adslibrary.xml.splash

import android.app.Activity
import android.content.Context
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.monetize.kit.sdk.ads.interstitial.InterAdsConfigs
import io.monetize.kit.sdk.ads.interstitial.InterstitialControllerListener
import io.monetize.kit.sdk.core.utils.in_app_update.UpdateState
import io.monetize.kit.sdk.core.utils.init.AdKit
import io.monetize.kit.sdk.core.utils.init.AdKit.consentManager
import io.monetize.kit.sdk.core.utils.init.AdKit.internetController
import io.monetize.kit.sdk.core.utils.init.AdKit.splashAdController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class State(
    val moveNext: Boolean = false,
    val showSplashAd: Boolean = false,
)


class SplashXmlViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        @Suppress("UNCHECKED_CAST")
        return SplashXmlViewModel(
        ) as T
    }
}


class SplashXmlViewModel : ViewModel() {


    private var _state = MutableStateFlow(State())
    val state = _state.asStateFlow()


    init {

        viewModelScope.launch {

            consentManager.googleConsent.collectLatest {
                runSplash()
            }
        }


    }

    fun checkConsent(activity: Activity) {
        if (!AdKit.adKitPref.isAppPurchased && internetController.isConnected) {
            consentManager.gatherConsent(activity)
            if (consentManager.canRequestAds) {
                runSplash()
            }
        } else {
            runSplash()
        }
    }

    fun showSplashAd(activity: Activity) {
        if (state.value.moveNext.not()) {
            splashAdController.initSplashAdmob(
                activity = activity,
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
                                moveNext = true
                            )
                        }
                    }

                }
            )
        }


    }

    fun runSplash() {
        if (state.value.showSplashAd.not()) {
            _state.update {
                it.copy(
                    showSplashAd = true
                )
            }
        }

    }


    fun checkUpdate(
        context: Context,
        launcher: ActivityResultLauncher<IntentSenderRequest>
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
                    //continue

                }

                UpdateState.Idle -> {

                }
            }

        }

        AdKit.inAppUpdateManager.checkUpdate(context)

    }

    fun resumeAd(activity: Activity) {
        splashAdController.resumeAd(activity, true)
    }

    fun pauseAd() {
        splashAdController.pauseAd()
    }

}