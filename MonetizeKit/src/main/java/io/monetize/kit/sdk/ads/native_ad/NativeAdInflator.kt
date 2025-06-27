package io.monetize.kit.sdk.ads.native_ad

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import io.monetize.kit.sdk.R
import io.monetize.kit.sdk.ads.native_ad.custom.SdkNativeAdView
import io.monetize.kit.sdk.core.utils.adtype.AdType
import io.monetize.kit.sdk.core.utils.shimmer_effect.ShimmerFrameLayout

private fun getFirstNonNull(vararg values: Int?): Int {
    return values.firstOrNull { it != null }
        ?: throw IllegalArgumentException("All layout values are null")
}

fun addShimmerLayout(
    context: Context,
    adFrame: LinearLayout, adType: AdType,
    customLayoutHelper: AdsCustomLayoutHelper? = null
) {
    val shimmerLayoutId = when (adType) {
        AdType.LARGE_NATIVE -> getFirstNonNull(
            customLayoutHelper?.getBigNativeShimmer(),
            customLayoutHelper?.getBigNative(),
            R.layout.large_native_layout
        )

        AdType.JAZZ_LEFT_BOTTOM_CTA -> getFirstNonNull(
            customLayoutHelper?.getSplitNativeShimmer(),
            customLayoutHelper?.getSplitNative(),
            R.layout.large_native_right_jazz
        )

        AdType.SMALL_BOTTOM_BUTTON -> getFirstNonNull(
            customLayoutHelper?.getSmallNativeShimmer(),
            customLayoutHelper?.getSmallNative(),
            R.layout.small_native_layout
        )

        AdType.BANNER -> R.layout.banner_layout
    }

    val shimmerContainer = LayoutInflater.from(context)
        .inflate(R.layout.shimmer_layout, adFrame, false) as ShimmerFrameLayout
    try {
        shimmerContainer.parent?.let { parent ->
            (parent as ViewGroup).removeAllViews()
        }
    } catch (_: Exception) {
    }
    adFrame.visibility = View.VISIBLE
    try {
        adFrame.removeAllViews()
    } catch (_: Exception) {
    }
    shimmerContainer.addView(
        LayoutInflater.from(context)
            .inflate(shimmerLayoutId, adFrame, false)
    )
    adFrame.addView(shimmerContainer)
}


fun addNativeAdView(
    adsCustomLayoutHelper: AdsCustomLayoutHelper,
    adType: AdType,
    context: Context,
    adFrame: LinearLayout,
    ad: NativeAd,
) {

    try {
        when (adType) {
            AdType.JAZZ_LEFT_BOTTOM_CTA -> {
                val isForCustom = adsCustomLayoutHelper.getSplitNative() != null
                val layoutId =
                    adsCustomLayoutHelper.getSplitNative() ?: R.layout.large_native_right_jazz
                val adView =
                    LayoutInflater.from(context).inflate(layoutId, adFrame, false)

                try {
                    adView.parent?.let { parent ->
                        (parent as ViewGroup).removeAllViews()
                    }
                } catch (_: Exception) {
                }
                if (isForCustom) {
                    val sdkLayout = adView.findViewById<SdkNativeAdView>(R.id.ad_view)
                    populateUnifiedNativeJazzAdViewUnified(
                        nativeAd = ad,
                        adView = sdkLayout.nativeAdView,
                        isCustom = true,
                        sdkLayout = sdkLayout
                    )
                } else {
                    val nativeAdView = adView.findViewById<NativeAdView>(R.id.ad_view)
                    populateUnifiedNativeJazzAdViewUnified(
                        nativeAd = ad,
                        adView = nativeAdView
                    )
                }

                adFrame.visibility = View.VISIBLE
                try {
                    adFrame.removeAllViews()
                } catch (_: Exception) {
                }
                adFrame.addView(adView)

            }


            else -> {
                val isForSmall = adType == AdType.SMALL_BOTTOM_BUTTON
                val isForCustom = if (isForSmall) {
                    adsCustomLayoutHelper.getSmallNative() != null
                } else {
                    adsCustomLayoutHelper.getBigNative() != null
                }

                val adView = LayoutInflater.from(context).inflate(
                    when {
                        isForSmall && isForCustom -> adsCustomLayoutHelper.getSmallNative()!!
                        !isForSmall && isForCustom -> adsCustomLayoutHelper.getBigNative()!!
                        isForSmall -> R.layout.small_native_layout
                        else -> R.layout.large_native_layout
                    },
                    adFrame,
                    false
                )
                try {
                    adView.parent?.let { parent ->
                        (parent as ViewGroup).removeAllViews()
                    }
                } catch (_: Exception) {
                }

                if (isForCustom) {
                    val sdkLayout = adView.findViewById<SdkNativeAdView>(R.id.ad_view)
                    populateUnifiedNativeAdViewUnified(
                        nativeAd = ad,
                        adView = sdkLayout.nativeAdView,
                        isCustom = true,
                        customLayout = sdkLayout,
                        isForSmall = isForSmall
                    )
                } else {
                    val defaultAdView = adView.findViewById<NativeAdView>(R.id.ad_view)
                    populateUnifiedNativeAdViewUnified(
                        nativeAd = ad,
                        adView = defaultAdView,
                        isForSmall = isForSmall
                    )
                }

                adFrame.visibility = View.VISIBLE
                try {
                    adFrame.removeAllViews()
                } catch (_: Exception) {
                }
                adFrame.addView(adView)
            }
        }

    } catch (e: Exception) {
        e.printStackTrace()

    }
}

fun populateUnifiedNativeAdViewUnified(
    nativeAd: NativeAd,
    adView: NativeAdView,
    isCustom: Boolean = false,
    customLayout: SdkNativeAdView? = null,
    isForSmall: Boolean
) {
    if (!isForSmall) {
        val mediaView = if (isCustom) {
            customLayout?.mediaView?.setupMediaView() as? MediaView
        } else {
            adView.findViewById(R.id.ad_media)
        }

        if (mediaView != null) {
            adView.mediaView = mediaView
            mediaView.setOnHierarchyChangeListener(object : ViewGroup.OnHierarchyChangeListener {
                override fun onChildViewAdded(parent: View?, child: View?) {
                    if (child is ImageView) {
                        child.adjustViewBounds = true
                        child.scaleType = ImageView.ScaleType.CENTER_CROP
                    }
                }

                override fun onChildViewRemoved(parent: View?, child: View?) {}
            })
        }
    }

    adView.headlineView = adView.findViewById(R.id.ad_headline)
    adView.bodyView = adView.findViewById(R.id.ad_body)
    adView.iconView = adView.findViewById(R.id.ad_app_icon)
    val button = adView.findViewById<AppCompatButton>(R.id.ad_call_to_action)
    adView.callToActionView = button

    (adView.headlineView as? TextView)?.apply {
        text = nativeAd.headline
        setTextColor(Color.BLACK)
    }

    (adView.bodyView as? TextView)?.apply {
        text = nativeAd.body ?: ""
        visibility = if (nativeAd.body == null) View.GONE else View.VISIBLE
        setTextColor(Color.BLACK)
    }

    (adView.callToActionView as? AppCompatButton)?.apply {
        text = nativeAd.callToAction ?: ""
        visibility = if (nativeAd.callToAction == null) View.GONE else View.VISIBLE
    }

    (adView.iconView as? ImageView)?.apply {
        visibility = if (nativeAd.icon == null) View.GONE else View.VISIBLE
        setImageDrawable(nativeAd.icon?.drawable)
    }

    adView.setNativeAd(nativeAd)
}


fun populateUnifiedNativeJazzAdViewUnified(
    nativeAd: NativeAd,
    adView: NativeAdView,
    isCustom: Boolean = false,
    sdkLayout: SdkNativeAdView? = null
) {
    val mediaView: MediaView? = if (isCustom) {
        sdkLayout?.mediaView?.setupMediaView() as? MediaView
    } else {
        adView.findViewById(R.id.ad_media)
    }

    if (mediaView != null) {
        adView.mediaView = mediaView
        mediaView.setOnHierarchyChangeListener(object : ViewGroup.OnHierarchyChangeListener {
            override fun onChildViewAdded(parent: View?, child: View?) {
                if (child is ImageView) {
                    child.adjustViewBounds = true
                }
            }

            override fun onChildViewRemoved(parent: View?, child: View?) {}
        })
    }

    adView.headlineView = adView.findViewById(R.id.ad_headline)
    adView.bodyView = adView.findViewById(R.id.ad_body)
    val button = adView.findViewById<AppCompatButton>(R.id.ad_call_to_action)
    adView.callToActionView = button

    (adView.bodyView as? TextView)?.apply {
        setTextColor(Color.BLACK)
        text = nativeAd.body ?: ""
        visibility = if (nativeAd.body == null) View.GONE else View.VISIBLE
    }

    (adView.headlineView as? TextView)?.apply {
        setTextColor(Color.BLACK)
        text = nativeAd.headline ?: ""
    }

    (adView.callToActionView as? AppCompatButton)?.apply {
        text = nativeAd.callToAction ?: ""
        visibility = if (nativeAd.callToAction == null) View.GONE else View.VISIBLE
    }

    adView.setNativeAd(nativeAd)
}
