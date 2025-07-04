package com.test.compose.adslibrary.xml.splash

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.monetize.kit.sdk.ads.interstitial.AdKitSplashAdController
import io.monetize.kit.sdk.ads.interstitial.InterstitialControllerListener
import io.monetize.kit.sdk.core.utils.AdKitInternetController
import io.monetize.kit.sdk.core.utils.AdKitPref
import io.monetize.kit.sdk.core.utils.consent.AdKitConsentManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class State(
    val moveNext: Boolean = false,
    val showSplashAd: Boolean = false,
)


class SplashXmlViewModelFactory(val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        @Suppress("UNCHECKED_CAST")
        return SplashXmlViewModel(
            consentManager = AdKitConsentManager.getInstance(context),
            pref = AdKitPref.getInstance(context),
            internetController = AdKitInternetController.getInstance(context),
            splashAdController = AdKitSplashAdController.getInstance(context)
        ) as T
    }
}


class SplashXmlViewModel(
    private val consentManager: AdKitConsentManager,
    private val pref: AdKitPref,
    private val internetController: AdKitInternetController,
    private val splashAdController: AdKitSplashAdController
) : ViewModel() {

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
        if (!pref.isAppPurchased && internetController.isConnected) {
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
                activity,
                true,
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

    fun resumeAd(activity: Activity) {
        splashAdController.resumeAd(activity, true)
    }

    fun pauseAd() {
        splashAdController.pauseAd()
    }

}