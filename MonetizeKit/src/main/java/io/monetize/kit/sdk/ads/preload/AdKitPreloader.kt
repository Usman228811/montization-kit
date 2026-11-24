package io.monetize.kit.sdk.ads.preload

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAd
import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAdPreloader
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.common.PreloadCallback
import com.google.android.libraries.ads.mobile.sdk.common.PreloadConfiguration
import com.google.android.libraries.ads.mobile.sdk.common.ResponseInfo
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAd
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAdPreloader
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardedAd
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardedAdPreloader
import io.monetize.kit.sdk.core.utils.firebaseLong

internal enum class PreloadFormat { INTERSTITIAL, REWARDED, APP_OPEN }

/**
 * Next-gen SDK Preloading API for the full-screen formats: interstitial, rewarded and app open. Once started for an ad unit, the SDK keeps a
 * buffer of loaded ads for it and loads a new one after each poll, retrying on its own after failures.
 *
 * The preload ID is the ad unit ID, so each unit of an ad ID key's rotation has its own buffer.
 * SDK callbacks arrive on a background thread; everything this reports is posted to the main thread.
 */
internal object AdKitPreloader {

    private const val TAG = "AdKit_Preload"

    private val mainHandler = Handler(Looper.getMainLooper())

    // One-shot requests waiting for the next ad of any of their ad units (instant / load-and-show flows)
    private class Waiter(val format: PreloadFormat, val adUnitIds: Set<String>, val onResult: (loaded: Boolean, reason: String) -> Unit)

    private val waiters = mutableListOf<Waiter>()

    // Told every time an ad of the format is ready (e.g. the app open listener's onAdLoaded)
    private val onPreloaded = mutableMapOf<PreloadFormat, () -> Unit>()

    private fun callback(format: PreloadFormat) = object : PreloadCallback {
        override fun onAdPreloaded(preloadId: String, responseInfo: ResponseInfo) {
            Log.d(TAG, "$format preloaded: $preloadId")
            mainHandler.post {
                onPreloaded[format]?.invoke()
                notifyWaiters(format, preloadId, true, "ad preloaded")
            }
        }

        override fun onAdFailedToPreload(preloadId: String, adError: LoadAdError) {
            Log.d(TAG, "$format failed: $preloadId code: ${adError.code} message: ${adError.message}")
            mainHandler.post {
                notifyWaiters(format, preloadId, false, "ad failed to load code: ${adError.code} message: ${adError.message}")
            }
        }

        override fun onAdsExhausted(preloadId: String) {
            Log.d(TAG, "$format exhausted: $preloadId")
        }
    }

    private val callbacks = PreloadFormat.entries.associateWith { callback(it) }

    // Remote Config "PRELOAD_BUFFER_SIZE": ads kept ready per ad unit; 0 or missing = the SDK's default
    private fun configuration(adUnitId: String) = PreloadConfiguration(
        AdRequest.Builder(adUnitId).build(),
        firebaseLong("PRELOAD_BUFFER_SIZE", 0).toInt().takeIf { it > 0 },
    )

    /** Starts preloading every ad unit that isn't preloading yet. */
    fun start(format: PreloadFormat, adUnitIds: List<String>) {
        adUnitIds.filter { it.isNotBlank() }.distinct().forEach { id ->
            try {
                if (isStarted(format, id)) return@forEach
                val configuration = configuration(id)
                val callback = callbacks.getValue(format)
                val started = when (format) {
                    PreloadFormat.INTERSTITIAL -> InterstitialAdPreloader.start(id, configuration, callback)
                    PreloadFormat.REWARDED -> RewardedAdPreloader.start(id, configuration, callback)
                    PreloadFormat.APP_OPEN -> AppOpenAdPreloader.start(id, configuration, callback)
                }
                Log.d(TAG, "$format start $id: $started")
            } catch (e: Exception) {
                Log.d(TAG, "$format start $id failed: ${e.message}")
            }
        }
    }

    private fun isStarted(format: PreloadFormat, adUnitId: String): Boolean = when (format) {
        PreloadFormat.INTERSTITIAL -> InterstitialAdPreloader.getConfiguration(adUnitId) != null
        PreloadFormat.REWARDED -> RewardedAdPreloader.getConfiguration(adUnitId) != null
        PreloadFormat.APP_OPEN -> AppOpenAdPreloader.getConfiguration(adUnitId) != null
    }

    fun isAvailable(format: PreloadFormat, adUnitIds: List<String>): Boolean = adUnitIds.any { id ->
        try {
            when (format) {
                PreloadFormat.INTERSTITIAL -> InterstitialAdPreloader.isAdAvailable(id)
                PreloadFormat.REWARDED -> RewardedAdPreloader.isAdAvailable(id)
                PreloadFormat.APP_OPEN -> AppOpenAdPreloader.isAdAvailable(id)
            }
        } catch (_: Exception) {
            false
        }
    }

    // nextId gives the ad units in rotation order (the ID managers' getNext…Id); the first one with an ad wins
    fun pollInterstitial(count: Int, nextId: () -> String?): Pair<String, InterstitialAd>? =
        poll(count, nextId) { InterstitialAdPreloader.pollAd(it) }

    fun pollRewarded(count: Int, nextId: () -> String?): Pair<String, RewardedAd>? =
        poll(count, nextId) { RewardedAdPreloader.pollAd(it) }

    fun pollAppOpen(adUnitId: String): AppOpenAd? = try {
        AppOpenAdPreloader.pollAd(adUnitId)
    } catch (_: Exception) {
        null
    }

    private fun <T> poll(count: Int, nextId: () -> String?, pollAd: (String) -> T?): Pair<String, T>? {
        repeat(count) {
            val id = nextId()?.takeIf { it.isNotBlank() } ?: return null
            val ad = try {
                pollAd(id)
            } catch (_: Exception) {
                null
            }
            if (ad != null) return id to ad
        }
        return null
    }

    /**
     * Calls [onResult] once, on the main thread, with the next preloaded ad (true) or the next failure
     * (false) of any of [adUnitIds]. Returns a function that cancels the wait (e.g. on timeout).
     */
    fun awaitNext(format: PreloadFormat, adUnitIds: List<String>, onResult: (loaded: Boolean, reason: String) -> Unit): () -> Unit {
        val waiter = Waiter(format, adUnitIds.toSet(), onResult)
        onMain { waiters += waiter }
        return { onMain { waiters -= waiter } }
    }

    // Right away when already on the main thread, so a waiter is registered before the preload it waits for starts
    private fun onMain(block: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) block() else mainHandler.post(block)
    }

    private fun notifyWaiters(format: PreloadFormat, preloadId: String, loaded: Boolean, reason: String) {
        val matching = waiters.filter { it.format == format && preloadId in it.adUnitIds }
        waiters -= matching.toSet()
        matching.forEach { it.onResult(loaded, reason) }
    }

    fun setOnPreloaded(format: PreloadFormat, listener: (() -> Unit)?) {
        onMain {
            if (listener == null) onPreloaded.remove(format) else onPreloaded[format] = listener
        }
    }

    /** Stops all preloading and drops the buffered ads (app purchased, ads disabled). */
    fun destroyAll() {
        try {
            InterstitialAdPreloader.destroyAll()
            RewardedAdPreloader.destroyAll()
            AppOpenAdPreloader.destroyAll()
        } catch (_: Exception) {
        }
    }
}
