package io.monetize.kit.sdk.core.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

class AdKitInternetController private constructor(context: Context) {


    companion object {
        @Volatile
        private var instance: AdKitInternetController? = null


        internal   fun getInstance(
            context: Context,
        ): AdKitInternetController {
            return instance ?: synchronized(this) {
                instance ?: AdKitInternetController(
                    context.applicationContext,
                ).also {
                    instance = it
                }
            }
        }
    }

    private val connectivityManager by lazy {
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }
    val isConnected: Boolean
        get() {
            try {
                val network = connectivityManager.activeNetwork
                if (network != null) {
                    return connectivityManager.getNetworkCapabilities(network)
                        .isNetworkCapabilitiesValid()
                }
            } catch (_: Exception) {
            }
            return false
        }

    private fun NetworkCapabilities?.isNetworkCapabilitiesValid(): Boolean = when {
        this == null -> false
        hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) &&
                (hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        hasTransport(NetworkCapabilities.TRANSPORT_VPN) ||
                        hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) -> true

        else -> false
    }
}