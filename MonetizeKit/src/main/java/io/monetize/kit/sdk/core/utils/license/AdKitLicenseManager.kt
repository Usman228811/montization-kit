package io.monetize.kit.sdk.core.utils.license

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.content.pm.PackageInfoCompat
import io.monetize.kit.sdk.BuildConfig
import io.monetize.kit.sdk.core.utils.AdKitPref
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.security.MessageDigest

// Google Apps Script web app URL (ends with /exec), see tools/license-server/Code.gs.
// Empty = license check off and every app is allowed.
private const val LICENSE_ENDPOINT =
    "https://script.google.com/macros/s/AKfycbzzqX2qTf4dxWzbyQc6SgD9rGpClRl_Sqe_rjR5IKfahKoElMtllawuSvkienU71-ra/exec"

// One check per device every 12 hours keeps the Apps Script well within its quotas
private const val CHECK_INTERVAL_MS = 12 * 60 * 60 * 1000L
private const val TIMEOUT_MS = 10_000

/**
 * Reports the host app (package, name, versions, signing certificate) to the license server.
 * When the server answers "not allowed" (Status DENY in the sheet), ads are switched off and the
 * app is crashed. Fails open: the crash only follows a fresh DENY from the server, never a cached
 * answer or a failed request, so a network or server problem can't take down an allowed app.
 *
 * Nothing runs on the main thread: prefs, package info and the request all happen on IO, and
 * the ad check only reads an in-memory flag, so AdKit.init and ad loading keep their timing.
 */
internal class AdKitLicenseManager private constructor() {

    companion object {
        @Volatile
        private var instance: AdKitLicenseManager? = null

        // In-memory answer read before every ad request. Static so the check needs no context
        // and works even before AdKit.init; true until the cached or fresh answer says otherwise.
        @Volatile
        private var isAllowed: Boolean = true

        internal fun isAppAllowed(): Boolean = isAllowed

        internal fun getInstance(): AdKitLicenseManager {
            return instance ?: synchronized(this) {
                instance ?: AdKitLicenseManager().also { instance = it }
            }
        }
    }

    fun verify(context: Context, pref: () -> AdKitPref) {
        if (LICENSE_ENDPOINT.isBlank()) return
        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefs = pref()
                // Cached answer first, long before the first ad (splash waits for Remote Config)
                isAllowed = prefs.isLicenseAllowed

                val now = System.currentTimeMillis()
                // A denied app re-checks on every start, so switching it back to ALLOW
                // in the sheet stops the crash on the next launch
                if (isAllowed && now - prefs.licenseCheckedAt in 0 until CHECK_INTERVAL_MS) {
                    return@launch
                }

                val allowed = requestLicense(appContext) ?: return@launch
                isAllowed = allowed
                prefs.isLicenseAllowed = allowed
                prefs.licenseCheckedAt = now
                if (!allowed) {
                    crashApp()
                }
            } catch (_: Exception) {
            }
        }
    }

    // Thrown on the main thread so it is a normal uncaught crash (outside the try/catch above)
    private fun crashApp() {
        Handler(Looper.getMainLooper()).post {
            throw IllegalStateException("Illegal App")
        }
    }

    // Returns null when there is no usable answer, so the cached result stays in place
    private fun requestLicense(context: Context): Boolean? {
        var connection: HttpURLConnection? = null
        return try {
            val packageInfo = packageInfo(context, signingFlag())
            val query = mapOf(
                "package" to context.packageName,
                "appName" to context.applicationInfo.loadLabel(context.packageManager).toString(),
                "versionName" to packageInfo?.versionName.orEmpty(),
                "versionCode" to (packageInfo?.let { PackageInfoCompat.getLongVersionCode(it) } ?: 0L).toString(),
                "libraryVersion" to BuildConfig.LIBRARY_VERSION,
                "sha256" to signingSha256(packageInfo),
            ).entries.joinToString("&") { (key, value) ->
                "$key=${URLEncoder.encode(value, "UTF-8")}"
            }

            connection = (URL("$LICENSE_ENDPOINT?$query").openConnection() as HttpURLConnection).apply {
                connectTimeout = TIMEOUT_MS
                readTimeout = TIMEOUT_MS
                // Apps Script returns the response through a redirect
                instanceFollowRedirects = true
            }
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                null
            } else {
                val body = connection.inputStream.bufferedReader().use { it.readText() }
                JSONObject(body).optBoolean("allowed", true)
            }
        } catch (_: Exception) {
            null
        } finally {
            connection?.disconnect()
        }
    }

    private fun signingFlag(): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            PackageManager.GET_SIGNING_CERTIFICATES
        } else {
            @Suppress("DEPRECATION")
            PackageManager.GET_SIGNATURES
        }

    private fun packageInfo(context: Context, flags: Int): PackageInfo? {
        val packageManager = context.packageManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getPackageInfo(
                context.packageName,
                PackageManager.PackageInfoFlags.of(flags.toLong())
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.getPackageInfo(context.packageName, flags)
        }
    }

    // Same format as the SHA-256 shown in Play Console (AB:CD:...). Apps installed from
    // Google Play report the Play app signing key, not the upload key.
    private fun signingSha256(packageInfo: PackageInfo?): String {
        val signature = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo?.signingInfo?.apkContentsSigners?.firstOrNull()
        } else {
            @Suppress("DEPRECATION")
            packageInfo?.signatures?.firstOrNull()
        } ?: return ""
        return MessageDigest.getInstance("SHA-256")
            .digest(signature.toByteArray())
            .joinToString(":") { "%02X".format(it) }
    }
}
