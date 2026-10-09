package io.monetize.kit.sdk.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import io.monetize.kit.sdk.core.utils.callbacks.AdCallBack

/**
 * One callback instance for the whole life of the ad view that always forwards to the
 * latest [adCallBack], so the ad is set up once and still reports to the current caller.
 */
@Composable
internal fun rememberAdCallBack(adCallBack: AdCallBack?): AdCallBack {
    val currentAdCallBack by rememberUpdatedState(adCallBack)
    return remember {
        object : AdCallBack {
            override fun onAdFailed(reason: String) {
                currentAdCallBack?.onAdFailed(reason)
            }

            override fun onAdShow() {
                currentAdCallBack?.onAdShow()
            }

            override fun onAdClick() {
                currentAdCallBack?.onAdClick()
            }
        }
    }
}
