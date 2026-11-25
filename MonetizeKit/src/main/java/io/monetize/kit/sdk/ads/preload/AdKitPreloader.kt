package io.monetize.kit.sdk.ads.preload

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
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
import java.util.concurrent.ConcurrentHashMap

private const val HOUR = 60 * 60 * 1000L

/** [bufferKey]: Remote Config key for this format's buffer size; [expiryMillis]: how long a loaded ad can be shown. */
internal enum class PreloadFormat(val bufferKey: String, val expiryMillis: Long) {
    INTERSTITIAL("PRELOAD_BUFFER_SIZE_INTER", HOUR),
    REWARDED("PRELOAD_BUFFER_SIZE_REWARDED", HOUR),
    APP_OPEN("PRELOAD_BUFFER_SIZE_APP_OPEN", 4 * HOUR),
}

/**
 * Next-gen SDK Preloading API for the full-screen formats: interstitial, rewarded and app open. Once started for an ad unit, the SDK keeps a
 * buffer of loaded ads for it and loads a new one after each poll, retrying on its own after failures.
 *
 * The preload ID is the ad unit ID, so each unit of an ad ID key's rotation has its own buffer.
 * SDK callbacks arrive on a background thread; everything this reports is posted to the main thread.
 *
 * While the app is in the background the preloaders are stopped, so the SDK doesn't keep loading (and replacing expired) ads nobody
 * sees: the ready ads of each ad unit are taken out and kept, and the preloader starts again when the first kept ad is used (or when
 * none is left, e.g. all expired) once the app is back.
 */
internal object AdKitPreloader : DefaultLifecycleObserver {

    private const val TAG = "AdKit_Preload"

    // Guards the background stop against an SDK that never runs out of ads
    private const val MAX_KEPT_PER_AD_UNIT = 15

    private val mainHandler = Handler(Looper.getMainLooper())

    private data class Key(val format: PreloadFormat, val adUnitId: String)

    private class HeldAd(val ad: Any, val loadedAt: Long) {
        fun isExpired(format: PreloadFormat) = System.currentTimeMillis() - loadedAt >= format.expiryMillis
    }

    // Load times of each ad unit's preloaded ads, oldest first (written on the SDK's thread)
    private val preloadTimes = ConcurrentHashMap<Key, ArrayDeque<Long>>()

    // Main thread only: ads taken out of preloaders stopped in the background (oldest first), and those stopped ad units
    private val held = mutableMapOf<Key, ArrayDeque<HeldAd>>()
    private val stopped = mutableSetOf<Key>()
    private var inBackground = false
    private var isObservingApp = false

    // One-shot requests waiting for the next ad of any of their ad units (instant / load-and-show flows)
    private class Waiter(val format: PreloadFormat, val adUnitIds: Set<String>, val onResult: (loaded: Boolean, reason: String) -> Unit)

    private val waiters = mutableListOf<Waiter>()

    // Told every time an ad of the format is ready (e.g. the app open listener's onAdLoaded)
    private val onPreloaded = mutableMapOf<PreloadFormat, () -> Unit>()

    private fun callback(format: PreloadFormat) = object : PreloadCallback {
        override fun onAdPreloaded(preloadId: String, responseInfo: ResponseInfo) {
            Log.d(TAG, "$format preloaded: $preloadId")
            val times = preloadTimes.getOrPut(Key(format, preloadId)) { ArrayDeque() }
            synchronized(times) { times.addLast(System.currentTimeMillis()) }
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

    // Ads kept ready per ad unit, from Remote Config: the format's key (PRELOAD_BUFFER_SIZE_INTER / _REWARDED / _APP_OPEN),
    // else PRELOAD_BUFFER_SIZE, else 1 (every extra ad that is never shown lowers the show rate)
    private fun bufferSize(format: PreloadFormat): Int =
        firebaseLong(format.bufferKey, 0).toInt().takeIf { it > 0 }
            ?: firebaseLong("PRELOAD_BUFFER_SIZE", 0).toInt().takeIf { it > 0 }
            ?: 1

    private fun configuration(format: PreloadFormat, adUnitId: String) =
        PreloadConfiguration(AdRequest.Builder(adUnitId).build(), bufferSize(format))

    /** Starts preloading every ad unit that isn't preloading yet (in the background: once the app is back). */
    fun start(format: PreloadFormat, adUnitIds: List<String>) {
        observeApp()
        adUnitIds.filter { it.isNotBlank() }.distinct().forEach { id ->
            val key = Key(format, id)
            // Stays stopped while in the background, or while its kept ads are waiting to be used
            if (inBackground || held[key].orEmpty().isNotEmpty()) {
                stopped += key
            } else {
                startPreloader(key)
            }
        }
    }

    private fun startPreloader(key: Key) {
        val (format, id) = key
        stopped -= key
        try {
            if (isStarted(format, id)) return
            val configuration = configuration(format, id)
            val callback = callbacks.getValue(format)
            val started = when (format) {
                PreloadFormat.INTERSTITIAL -> InterstitialAdPreloader.start(id, configuration, callback)
                PreloadFormat.REWARDED -> RewardedAdPreloader.start(id, configuration, callback)
                PreloadFormat.APP_OPEN -> AppOpenAdPreloader.start(id, configuration, callback)
            }
            Log.d(TAG, "$format start $id (buffer ${configuration.bufferSize}): $started")
        } catch (e: Exception) {
            Log.d(TAG, "$format start $id failed: ${e.message}")
        }
    }

    private fun isStarted(format: PreloadFormat, adUnitId: String): Boolean = when (format) {
        PreloadFormat.INTERSTITIAL -> InterstitialAdPreloader.getConfiguration(adUnitId) != null
        PreloadFormat.REWARDED -> RewardedAdPreloader.getConfiguration(adUnitId) != null
        PreloadFormat.APP_OPEN -> AppOpenAdPreloader.getConfiguration(adUnitId) != null
    }

    private fun numAvailable(format: PreloadFormat, adUnitId: String): Int = try {
        when (format) {
            PreloadFormat.INTERSTITIAL -> InterstitialAdPreloader.getNumAdsAvailable(adUnitId)
            PreloadFormat.REWARDED -> RewardedAdPreloader.getNumAdsAvailable(adUnitId)
            PreloadFormat.APP_OPEN -> AppOpenAdPreloader.getNumAdsAvailable(adUnitId)
        }
    } catch (_: Exception) {
        0
    }

    fun isAvailable(format: PreloadFormat, adUnitIds: List<String>): Boolean = adUnitIds.any { id ->
        held[Key(format, id)].orEmpty().any { !it.isExpired(format) } || numAvailable(format, id) > 0
    }

    // nextId gives the ad units in rotation order (the ID managers' getNext…Id); the first one with an ad wins
    fun pollInterstitial(count: Int, nextId: () -> String?): Pair<String, InterstitialAd>? =
        poll(count, nextId) { pollOne(PreloadFormat.INTERSTITIAL, it) { id -> InterstitialAdPreloader.pollAd(id) } }

    fun pollRewarded(count: Int, nextId: () -> String?): Pair<String, RewardedAd>? =
        poll(count, nextId) { pollOne(PreloadFormat.REWARDED, it) { id -> RewardedAdPreloader.pollAd(id) } }

    /** The app open ad and when it was loaded (for the 4-hour expiry check). */
    fun pollAppOpen(adUnitId: String): Pair<AppOpenAd, Long>? =
        pollOne(PreloadFormat.APP_OPEN, adUnitId) { id -> AppOpenAdPreloader.pollAd(id) }

    private fun <T> poll(count: Int, nextId: () -> String?, pollAd: (String) -> Pair<T, Long>?): Pair<String, T>? {
        repeat(count) {
            val id = nextId()?.takeIf { it.isNotBlank() } ?: return null
            val ad = pollAd(id)?.first
            if (ad != null) return id to ad
        }
        return null
    }

    // A kept ad first (app was in the background), else the preloader's; with the ad's load time
    private inline fun <reified T : Any> pollOne(format: PreloadFormat, adUnitId: String, noinline sdkPoll: (String) -> T?): Pair<T, Long>? {
        val key = Key(format, adUnitId)
        takeHeld(key)?.let { kept -> (kept.ad as? T)?.let { return it to kept.loadedAt } }
        return pollPreloader(key, sdkPoll)
    }

    // Polls the SDK and pairs the ad with its load time: the buffer is oldest first, and the SDK drops expired ads from
    // its front without telling, so the times beyond the number of ads still available are stale
    private fun <T : Any> pollPreloader(key: Key, sdkPoll: (String) -> T?): Pair<T, Long>? {
        val available = numAvailable(key.format, key.adUnitId)
        val ad = try {
            sdkPoll(key.adUnitId)
        } catch (_: Exception) {
            null
        } ?: return null
        val times = preloadTimes[key] ?: return ad to System.currentTimeMillis()
        val loadedAt = synchronized(times) {
            while (times.size > available) times.removeFirstOrNull()
            times.removeFirstOrNull()
        }
        return ad to (loadedAt ?: System.currentTimeMillis())
    }

    // Removes the oldest kept ad that hasn't expired (expired ones are dropped). The first one used starts the
    // ad unit's preloader again, so the next ad loads while the remaining kept ads are used
    private fun takeHeld(key: Key): HeldAd? {
        val kept = held[key] ?: return null
        var ad: HeldAd? = null
        while (ad == null && kept.isNotEmpty()) {
            ad = kept.removeFirstOrNull()?.takeUnless { it.isExpired(key.format) }
        }
        if (kept.isEmpty()) held.remove(key)
        if (key in stopped && !inBackground) startPreloader(key)
        return ad
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

    private fun observeApp() {
        onMain {
            if (isObservingApp) return@onMain
            isObservingApp = true
            try {
                ProcessLifecycleOwner.get().lifecycle.addObserver(this)
            } catch (e: Exception) {
                Log.d(TAG, "app lifecycle observer failed: ${e.message}")
            }
        }
    }

    // App in the background: keep every ready ad of each ad unit and stop its preloader. Runs on the main thread
    // after the app is already hidden (ProcessLifecycleOwner waits ~700 ms), so it can't delay a touch
    override fun onStop(owner: LifecycleOwner) {
        inBackground = true
        PreloadFormat.entries.forEach { format ->
            startedIds(format).forEach { id ->
                val key = Key(format, id)
                try {
                    val kept = held.getOrPut(key) { ArrayDeque() }
                    repeat(numAvailable(format, id).coerceAtMost(MAX_KEPT_PER_AD_UNIT)) {
                        val (ad, loadedAt) = pollPreloader(key) { adUnitId ->
                            when (format) {
                                PreloadFormat.INTERSTITIAL -> InterstitialAdPreloader.pollAd(adUnitId)
                                PreloadFormat.REWARDED -> RewardedAdPreloader.pollAd(adUnitId)
                                PreloadFormat.APP_OPEN -> AppOpenAdPreloader.pollAd(adUnitId)
                            }
                        } ?: return@repeat
                        kept.addLast(HeldAd(ad, loadedAt))
                    }
                    if (kept.isEmpty()) held.remove(key)
                    when (format) {
                        PreloadFormat.INTERSTITIAL -> InterstitialAdPreloader.destroy(id)
                        PreloadFormat.REWARDED -> RewardedAdPreloader.destroy(id)
                        PreloadFormat.APP_OPEN -> AppOpenAdPreloader.destroy(id)
                    }
                    preloadTimes.remove(key)
                    stopped += key
                    Log.d(TAG, "$format stopped in background $id, ads kept: ${kept.size}")
                } catch (e: Exception) {
                    Log.d(TAG, "$format stop $id failed: ${e.message}")
                }
            }
        }
    }

    // App back: drop expired kept ads, and start again every stopped ad unit with no kept ad left
    override fun onStart(owner: LifecycleOwner) {
        inBackground = false
        held.entries.removeAll { (key, kept) ->
            kept.removeAll { it.isExpired(key.format) }
            kept.isEmpty()
        }
        stopped.filter { held[it].isNullOrEmpty() }.forEach { startPreloader(it) }
    }

    private fun startedIds(format: PreloadFormat): Set<String> = try {
        when (format) {
            PreloadFormat.INTERSTITIAL -> InterstitialAdPreloader.getConfigurations().keys
            PreloadFormat.REWARDED -> RewardedAdPreloader.getConfigurations().keys
            PreloadFormat.APP_OPEN -> AppOpenAdPreloader.getConfigurations().keys
        }.toSet()
    } catch (_: Exception) {
        emptySet()
    }

    /** Stops all preloading and drops the buffered and kept ads (app purchased, ads disabled). */
    fun destroyAll() {
        try {
            InterstitialAdPreloader.destroyAll()
            RewardedAdPreloader.destroyAll()
            AppOpenAdPreloader.destroyAll()
        } catch (_: Exception) {
        }
        preloadTimes.clear()
        onMain {
            held.clear()
            stopped.clear()
        }
    }
}
