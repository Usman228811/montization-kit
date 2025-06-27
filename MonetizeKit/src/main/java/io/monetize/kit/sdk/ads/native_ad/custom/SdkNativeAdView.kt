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


//class SdkNativeAdView @JvmOverloads constructor(
//    context: Context,
//    attrs: AttributeSet? = null
//) : FrameLayout(context, attrs) {
//
//    private val adMobNativeAdView: NativeAdView = NativeAdView(context)
//
//    init {
//        // Let child views (user-defined in XML) be moved into adMobNativeAdView
//        post {
//            // Move all children from this custom view into the real NativeAdView
//            val tempViews = mutableListOf<View>()
//            for (i in 0 until childCount) {
//                val child = getChildAt(i)
//                tempViews.add(child)
//            }
//            removeAllViews()
//            for (child in tempViews) {
//                adMobNativeAdView.addView(child)
//            }
//
//            // Add NativeAdView to this FrameLayout
//            addView(
//                adMobNativeAdView, LayoutParams(
//                    LayoutParams.MATCH_PARENT,
//                    LayoutParams.WRAP_CONTENT
//                )
//            )
//        }
//    }
//
//    fun getAdMobNativeAdView(): NativeAdView = adMobNativeAdView
//}

//
//class SdkNativeAdView @JvmOverloads constructor(
//    context: Context,
//    attrs: AttributeSet? = null
//) : FrameLayout(context, attrs) {
//
//    private val realAdView: NativeAdView = NativeAdView(context)
//
//    init {
//        realAdView.layoutParams = LayoutParams(
//            LayoutParams.MATCH_PARENT,
//            LayoutParams.WRAP_CONTENT
//        )
//        addView(realAdView)
//    }
//
//    /**
//     * Returns the internal NativeAdView to allow binding views like headline, mediaView, etc.
//     */
//    fun getAdMobNativeAdView(): NativeAdView = realAdView
//
//    /**
//     * Optional: Forward setNativeAd to the real view
//     */
//    fun setNativeAd(ad: NativeAd) {
//        realAdView.setNativeAd(ad)
//    }
//
//    /**
//     * Optional: Helper to add ad content view (e.g. inflated layout)
//     */
//    fun setAdContentView(content: View) {
//        realAdView.removeAllViews()
//        realAdView.addView(content)
//    }
//}