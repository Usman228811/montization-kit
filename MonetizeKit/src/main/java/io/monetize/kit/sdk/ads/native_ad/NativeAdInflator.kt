package io.monetize.kit.sdk.ads.native_ad

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatButton
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import io.monetize.kit.sdk.R
import io.monetize.kit.sdk.core.utils.adtype.AdType
import io.monetize.kit.sdk.core.utils.shimmer_effect.ShimmerFrameLayout


fun addShimmerLayout(
    context: Context, adFrame: LinearLayout, adType: AdType
) {
    val nativeAdView = adFrame.findViewById<NativeAdView>(R.id.nativeAdView)
    val shimmerLayout =
        adFrame.findViewById<ShimmerFrameLayout>(R.id.shimmerContainer)
    if (nativeAdView == null && shimmerLayout == null) {
        val shimmerLayoutId = when(adType){
            AdType.LARGE_NATIVE -> R.layout.large_native_layout
            AdType.JAZZ_LEFT_BOTTOM_CTA -> R.layout.large_native_right_jazz
            AdType.SMALL_BOTTOM_BUTTON -> R.layout.small_native_layout
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
}


fun addNativeAdView(
    adType: AdType,
    context: Context,
    adFrame: LinearLayout,
    ad: NativeAd,
) {

    try {
        when (adType) {
            AdType.JAZZ_LEFT_BOTTOM_CTA -> {
                val adView =
                    LayoutInflater.from(context)
                        .inflate(R.layout.large_native_right_jazz, adFrame, false)

                try {
                    adView.parent?.let { parent ->
                        (parent as ViewGroup).removeAllViews()
                    }
                } catch (_: Exception) {
                }
                populateUnifiedNativeJazzAdView(
                    ad, adView.findViewById(R.id.nativeAdView)
                )
                adFrame.visibility = View.VISIBLE
                try {
                    adFrame.removeAllViews()
                } catch (_: Exception) {
                }
                adFrame.addView(adView)

            }


            else -> {
                val adView = if (adType == AdType.SMALL_BOTTOM_BUTTON) {
                    LayoutInflater.from(context)
                        .inflate(R.layout.small_native_layout, adFrame, false)
                } else {

                    LayoutInflater.from(context)
                        .inflate(R.layout.large_native_layout, adFrame, false)

                }
                try {
                    adView.parent?.let { parent ->
                        (parent as ViewGroup).removeAllViews()
                    }
                } catch (_: Exception) {
                }
                populateUnifiedNativeAdView(
                    ad, adView.findViewById(R.id.ad_view), adType == AdType.SMALL_BOTTOM_BUTTON
                )
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


fun populateUnifiedNativeAdView(
    nativeAd: NativeAd,
    adView: NativeAdView,
    isForSmall: Boolean,
) {
    if (!isForSmall) {
        val mediaView = adView.findViewById<MediaView>(R.id.ad_media)
        adView.mediaView = mediaView
        mediaView.setOnHierarchyChangeListener(object : ViewGroup.OnHierarchyChangeListener {
            override fun onChildViewAdded(parent: View?, child: View?) {
                try {
                    if (child !is ImageView) {
                        return
                    }
                    child.adjustViewBounds = true
                    child.scaleType = ImageView.ScaleType.CENTER_CROP
                } catch (_: Exception) {
                }
            }

            override fun onChildViewRemoved(parent: View?, child: View?) {
            }

        })

    }
    adView.headlineView = adView.findViewById(R.id.ad_headline)
    adView.bodyView = adView.findViewById(R.id.ad_body)
    val button = adView.findViewById<AppCompatButton>(R.id.ad_call_to_action)

    adView.callToActionView = button



    adView.iconView = adView.findViewById(R.id.ad_app_icon)
    adView.callToActionView = button


    (adView.headlineView as MaterialTextView).text = nativeAd.headline
    if (nativeAd.body == null) {
        adView.bodyView?.visibility = View.GONE
    } else {
        adView.bodyView?.visibility = View.VISIBLE
        (adView.bodyView as MaterialTextView).text = nativeAd.body
    }
    if (nativeAd.callToAction == null) {
        adView.callToActionView?.visibility = View.GONE
    } else {
        adView.callToActionView?.visibility = View.VISIBLE
        (adView.callToActionView as AppCompatButton).text = nativeAd.callToAction
    }
    if (nativeAd.icon == null) {
        adView.iconView?.visibility = View.GONE
    } else {
        (adView.iconView as ShapeableImageView).setImageDrawable(
            nativeAd.icon!!.drawable
        )
        adView.iconView?.visibility = View.VISIBLE
    }
    adView.setNativeAd(nativeAd)
}

fun populateUnifiedNativeJazzAdView(
    nativeAd: NativeAd, adView: NativeAdView
) {

    val mediaView = adView.findViewById<MediaView>(R.id.ad_media)
    adView.mediaView = mediaView
    mediaView.setOnHierarchyChangeListener(object : ViewGroup.OnHierarchyChangeListener {
        override fun onChildViewAdded(parent: View?, child: View?) {
            try {
                if (child !is ImageView) {
                    return
                }
                child.adjustViewBounds = true
            } catch (_: Exception) {
            }
        }

        override fun onChildViewRemoved(parent: View?, child: View?) {
        }

    })



    adView.headlineView = adView.findViewById(R.id.ad_headline)
    adView.bodyView = adView.findViewById(R.id.ad_body)
    val button = adView.findViewById<AppCompatButton>(R.id.ad_call_to_action)

    adView.callToActionView = button

    adView.callToActionView = button
    (adView.bodyView as MaterialTextView).setTextColor(Color.BLACK)
    (adView.headlineView as MaterialTextView).setTextColor(Color.BLACK)

    (adView.headlineView as MaterialTextView).text = nativeAd.headline
    if (nativeAd.body == null) {
        adView.bodyView?.visibility = View.GONE
    } else {
        adView.bodyView?.visibility = View.VISIBLE
        (adView.bodyView as MaterialTextView).text = nativeAd.body
    }
    if (nativeAd.callToAction == null) {
        adView.callToActionView?.visibility = View.GONE
    } else {
        adView.callToActionView?.visibility = View.VISIBLE
        (adView.callToActionView as AppCompatButton).text = nativeAd.callToAction
    }
    adView.setNativeAd(nativeAd)
}