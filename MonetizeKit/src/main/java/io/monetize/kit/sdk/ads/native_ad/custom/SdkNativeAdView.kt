package io.monetize.kit.sdk.ads.native_ad.custom


import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.ads.nativead.NativeAdView
import io.monetize.kit.sdk.R


class SdkNativeAdView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    lateinit var nativeAdView: NativeAdView
        private set

    var mediaView: SdkMediaView? = null
    var headlineView: TextView? = null
    var bodyView: TextView? = null
    var iconView: ImageView? = null
    var callToActionView: View? = null

    override fun onFinishInflate() {
        super.onFinishInflate()

        // Step 1: Create NativeAdView
        nativeAdView = NativeAdView(context)
        nativeAdView.layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )

        // Step 2: Move children into it
        val children = mutableListOf<View>()
        for (i in 0 until childCount) {
            children.add(getChildAt(i))
        }
        removeAllViews()
        for (child in children) {
            nativeAdView.addView(child)
        }
        addView(nativeAdView)

        // Step 3: Assign views
        mediaView = nativeAdView.findViewById(R.id.ad_media)
        headlineView = nativeAdView.findViewById(R.id.ad_headline)
        bodyView = nativeAdView.findViewById(R.id.ad_body)
        iconView = nativeAdView.findViewById(R.id.ad_app_icon)
        callToActionView = nativeAdView.findViewById(R.id.ad_call_to_action)

        // Step 4: Attach to NativeAdView
        nativeAdView.mediaView = mediaView?.getAdMobMediaView()
        nativeAdView.headlineView = headlineView
        nativeAdView.bodyView = bodyView
        nativeAdView.iconView = iconView
        nativeAdView.callToActionView = callToActionView
    }
}