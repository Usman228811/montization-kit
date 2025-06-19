package io.monetize.kit.sdk.ads.banner

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import io.monetize.kit.sdk.ads.native_ad.AdControllerListener
import io.monetize.kit.sdk.core.utils.AdSdkInternetController
import io.monetize.kit.sdk.core.utils.AdSdkPref
import io.monetize.kit.sdk.core.utils.consent.AdSdkConsentManager

val singleBannerList = ArrayList<BannerSingleAdControllerModel>()


data class BannerSingleAdControllerModel(
    val controller: AdKitBannerController? = null,
    val key: String = ""
)

class AdKitBannerController(
    private val prefs: AdSdkPref,
    private val internetController: AdSdkInternetController,
    private val consentManager: AdSdkConsentManager
) {
    private var key: String = ""
    private var bannerAdId: String = ""
    private var canRequestBannerAd = true
    private var adView: AdView? = null
    private var adControllerListener: AdControllerListener? = null
    private lateinit var bannerSize: AdSize



    fun setAdControllerListener(listener: AdControllerListener?) {
        adControllerListener?.resetRequesting()
        adControllerListener = listener
    }


    fun loadNewBannerAd(
        context: Activity, enable: Boolean
    ) {
        setAdControllerListener(null)
        loadBannerAd(context, enable)
    }


    private fun loadBannerAd(
        context: Activity, enable: Boolean
    ) {
        try {
//            val adId = 0
//            if (adId != -1) {
            if (enable && !prefs.isAppPurchased && internetController.isConnected && consentManager.canRequestAds) {
                if (adView == null) {
                    if (!canRequestBannerAd) {
                        return
                    }
                    canRequestBannerAd = false
//                        if (BuildConfig.DEBUG) {
//                            Constants.showToast(context, "banner ad calling")
//                        }

                    if (!::bannerSize.isInitialized) {
                        bannerSize = getAdSize(context)
                    }
                    val bannerAd = AdView(context).apply {
                        this.adUnitId = bannerAdId
                        this.setAdSize(bannerSize)
                        this.loadAd(AdRequest.Builder().build())
                    }
                    bannerAd.adListener = object : AdListener() {
                        override fun onAdLoaded() {
                            super.onAdLoaded()
                            canRequestBannerAd = true
//                                if (BuildConfig.DEBUG) {
//                                    Toast.makeText(context, "banner ad loaded", Toast.LENGTH_SHORT)
//                                        .show()
//                                }
                            adView = bannerAd
                            adControllerListener?.onAdLoaded()
                        }

                        override fun onAdFailedToLoad(p0: LoadAdError) {
                            super.onAdFailedToLoad(p0)
                            canRequestBannerAd = true
                            adView = null
//                                if (BuildConfig.DEBUG) {
//
//                                    Toast.makeText(
//                                        context,
//                                        "banner load failed ==> code " + p0.code,
//                                        Toast.LENGTH_SHORT
//                                    ).show()
//                                }
                            adControllerListener?.onAdFailed()
                        }
                    }
                }
            } else {
                adControllerListener?.onAdFailed()
            }
//            } else {
//                adControllerListener?.onAdFailed()
//            }
        } catch (ignored: Exception) {
        }
    }

    fun populateBannerAd(
        context: Activity, key: String, enable: Boolean,
        bannerAdId:String,
        adFrame: LinearLayout, loadNewAd: Boolean = false,
        populateCallback: (Any) -> Unit
    ) {
        try {
            this.key = key
            this.bannerAdId = bannerAdId
            if (enable && !prefs.isAppPurchased && adView != null) {
                adView?.let {
                    try {
                        adFrame.visibility = View.VISIBLE
                        try {
                            it.parent?.let { parent ->
                                (parent as ViewGroup).removeAllViews()
                            }
                            adFrame.removeAllViews()
                        } catch (_: Exception) {
                        }
                        adFrame.addView(it)
                        populateCallback.invoke(it)
                        adView = null
                        if (loadNewAd) {
                            loadBannerAd(context, enable)
                        }
                    } catch (_: Exception) {
                    }
                }
            } else {
                loadBannerAd(context, enable)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}