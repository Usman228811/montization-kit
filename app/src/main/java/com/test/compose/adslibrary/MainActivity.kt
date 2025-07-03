package com.test.compose.adslibrary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.test.compose.adslibrary.AppClass.Companion.appContext
import com.test.compose.adslibrary.navigation.AppNavHost
import com.test.compose.adslibrary.navigation.AppRoute
import io.monetize.kit.sdk.ads.interstitial.InterControllerConfig
import io.monetize.kit.sdk.ads.open.AdKitOpenAdManager
import io.monetize.kit.sdk.core.utils.init.AdKitInitializer

class MainActivity : ComponentActivity() {

    private var adKitInitializer: AdKitInitializer? = null
    private var adKitOpenAdManager: AdKitOpenAdManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adKitInitializer = AdKitInitializer.getInstance(this)
        adKitOpenAdManager = AdKitOpenAdManager.getInstance(this)

        (appContext as AppClass).initializeAppClass()
        adKitOpenAdManager?.setCurrentComposeRoute(AppRoute.SplashRoute.route)


        adKitInitializer?.init(
            InterControllerConfig(
                openAdId = "ca-app-pub-3940256099942544/9257395921",
                splashId = "ca-app-pub-3940256099942544/1033173712",
                appInterIds = listOf(
                    "ca-app-pub-3940256099942544/1033173712",
                    "ca-app-pub-3940256099942544/1033173712"
                ),
                splashInterEnable = false,
                openAdEnable = true,
                splashTime = 16L,
                interLoadingEnable = true,
                openAdLoadingEnable = true
            )
        )

        setContent {
            val paddingValues = WindowInsets.systemBars.asPaddingValues()
            val navHostController = rememberNavController()

            LaunchedEffect(key1 = navHostController) {

                navHostController.currentBackStackEntryFlow.collect { backStackEntry ->
                    val route = backStackEntry.destination.route
                    adKitOpenAdManager?.setCurrentComposeRoute(route)
                }
            }


            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                AppNavHost(navHostController)
            }

        }
    }
}

