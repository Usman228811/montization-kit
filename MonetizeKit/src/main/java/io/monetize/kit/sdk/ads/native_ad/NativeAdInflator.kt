package io.monetize.kit.sdk.ads.native_ad

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import io.monetize.kit.sdk.R
import io.monetize.kit.sdk.ads.native_ad.custom.SdkNativeAdView
import io.monetize.kit.sdk.core.utils.adtype.AdType
import io.monetize.kit.sdk.core.utils.adtype.NativeControllerConfig
import io.monetize.kit.sdk.core.utils.init.AdKit
import io.monetize.kit.sdk.core.utils.shimmer_effect.ShimmerFrameLayout

private fun getFirstNonNull(vararg values: Int?): Int {
    return values.firstOrNull { it != null }
        ?: throw IllegalArgumentException("All layout values are null")
}

private val defaultLayouts = mapOf(
    AdType.LARGE_NATIVE to R.layout.large_native_layout,
    AdType.SMALL_NATIVE_MEDIA_VIEW to R.layout.small_native_media_view_layout,
    AdType.SMALL_NATIVE to R.layout.small_native_layout,
    AdType.SMALL_NATIVE_MINI to R.layout.small_native_mini_layout,
    AdType.BANNER to R.layout.banner_layout
)


fun addShimmerLayout(
    context: Context,
    adFrame: LinearLayout, adType: AdType,
    customLayoutHelper: AdsCustomLayoutHelper? = null
) {
    val shimmerLayoutId = getFirstNonNull(
        customLayoutHelper?.getShimmer(adType),
        customLayoutHelper?.getLayout(adType),
        defaultLayouts[adType]
    )

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
    nativeControllerConfig: NativeControllerConfig,
    adsCustomLayoutHelper: AdsCustomLayoutHelper,
    adType: AdType,
    context: Context,
    adFrame: LinearLayout,
    ad: NativeAd,
) {

    try {
        val (layoutId, isCustom) = adsCustomLayoutHelper.getLayout(adType)?.let { it to true }
            ?: (defaultLayouts[adType] to false)
        layoutId?.let {

            val adView = LayoutInflater.from(context).inflate(layoutId, adFrame, false)
            adView.parent?.let { parent ->
                (parent as ViewGroup).removeAllViews()
            }

            if (isCustom) {
                val sdkLayout = adView.findViewById<SdkNativeAdView>(R.id.ad_view)
                populateNativeAd(
                    nativeControllerConfig = nativeControllerConfig,
                    nativeAd = ad,
                    adView = sdkLayout.nativeAdView,
                    isCustom = true,
                    customLayout = sdkLayout,
                    adType = adType
                )
            } else {
                val defaultAdView = adView.findViewById<NativeAdView>(R.id.ad_view)
                populateNativeAd(
                    nativeControllerConfig = nativeControllerConfig,
                    nativeAd = ad,
                    adView = defaultAdView,
                    adType = adType
                )
            }

            adFrame.apply {
                visibility = View.VISIBLE
                removeAllViews()
                addView(adView)
            }
        }


    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun populateNativeAd(
    nativeControllerConfig: NativeControllerConfig,
    nativeAd: NativeAd,
    adView: NativeAdView,
    isCustom: Boolean = false,
    customLayout: SdkNativeAdView? = null,
    adType: AdType,
) {
    try {
        val colorHex = listOf(
            AdKit.firebaseHelper.getString(
                "${nativeControllerConfig.placementKey}_bgColor",
                ""
            ),
            AdKit.firebaseHelper.getString(
                "overAllNativeBgColor",
                ""
            ),
        ).firstOrNull { it.isNotEmpty() && it.startsWith("#") }

        colorHex?.toColorInt()?.let { colorInt ->
            adView.setBackgroundColor(colorInt)
        }
    } catch (_: Exception) {

    }

    when (adType) {
        AdType.LARGE_NATIVE, AdType.SMALL_NATIVE_MEDIA_VIEW -> {
            val mediaView: MediaView? = if (isCustom) {
                customLayout?.mediaView?.setupMediaView() as? MediaView
            } else {
                adView.findViewById(R.id.ad_media)
            }

            mediaView?.let {
                adView.mediaView = it
                it.setOnHierarchyChangeListener(object : ViewGroup.OnHierarchyChangeListener {
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

        else -> {

        }
    }


    // 🔹 Common views
    adView.headlineView = adView.findViewById(R.id.ad_headline)
    adView.bodyView = adView.findViewById(R.id.ad_body)
    val button = adView.findViewById<AppCompatButton>(R.id.ad_call_to_action)
    adView.callToActionView = button

    // Only for large layouts (withMediaView)
    if (adType != AdType.SMALL_NATIVE_MEDIA_VIEW) {
        adView.iconView = adView.findViewById(R.id.ad_app_icon)
    }

    // Headline
    (adView.headlineView as? TextView)?.apply {
        text = nativeAd.headline ?: ""
        setTextColor(Color.BLACK)
    }

    // Body
    (adView.bodyView as? TextView)?.apply {
        text = nativeAd.body ?: ""
        visibility = if (nativeAd.body == null) View.GONE else View.VISIBLE
        setTextColor(Color.BLACK)
    }

    (adView.callToActionView as? AppCompatButton)?.apply {
        text = nativeAd.callToAction ?: ""
        visibility = if (nativeAd.callToAction == null) View.GONE else View.VISIBLE

        try {
            val colorHex = listOf(
                AdKit.firebaseHelper.getString(
                    "${nativeControllerConfig.placementKey}_ctaColor",
                    ""
                ),
                AdKit.firebaseHelper.getString(
                    "overAllNativeCtaColor",
                    ""
                ),
            ).firstOrNull { it.isNotEmpty() && it.startsWith("#") }

            colorHex?.toColorInt()?.let { colorInt ->
                background?.let {
                    ViewCompat.setBackgroundTintList(this, ColorStateList.valueOf(colorInt))
                } ?: run {
                    setBackgroundColor(colorInt)
                }
            }
        } catch (_: Exception) {

        }
    }

    if (adType != AdType.SMALL_NATIVE_MEDIA_VIEW) {
        (adView.iconView as? ImageView)?.apply {
            visibility = if (nativeAd.icon == null) View.GONE else View.VISIBLE
            setImageDrawable(nativeAd.icon?.drawable)
        }
    }

    // 🔹 Bind native ad
    adView.setNativeAd(nativeAd)
}
