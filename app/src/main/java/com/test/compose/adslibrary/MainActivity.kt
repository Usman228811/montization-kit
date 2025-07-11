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
import io.monetize.kit.sdk.ads.interstitial.InterAdsConfigs
import io.monetize.kit.sdk.core.utils.init.AdKit

class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (appContext as AppClass).initializeAppClass()
        AdKit.openAdManager.setCurrentComposeRoute(AppRoute.SplashRoute.route)


        AdKit.initializer.initAdsConfigs(
            interAdsConfigs = InterAdsConfigs(
                openAdEnable = true,
                interLoadingEnable = true,
                openAdLoadingEnable = true,
            ),

        )

        setContent {
            val paddingValues = WindowInsets.systemBars.asPaddingValues()
            val navHostController = rememberNavController()

            LaunchedEffect(key1 = navHostController) {

                navHostController.currentBackStackEntryFlow.collect { backStackEntry ->
                    val route = backStackEntry.destination.route
                    AdKit.openAdManager.setCurrentComposeRoute(route)
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

