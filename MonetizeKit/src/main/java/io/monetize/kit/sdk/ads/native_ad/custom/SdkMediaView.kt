package io.monetize.kit.sdk.ads.native_ad.custom

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.google.android.gms.ads.nativead.MediaView


class SdkMediaView : FrameLayout {

    private var currentMediaView: View? = null

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    private fun init() {
        // Nothing to inflate yet — mediaView is added later dynamically

    }

    fun setupMediaView(): View? {
        val view = MediaView(context)
        removeAllViews()
        addView(view, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        currentMediaView = view
        return view
    }

    fun getAdMobMediaView(): MediaView? {
        return currentMediaView as? MediaView
    }
}
