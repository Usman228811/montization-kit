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
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.test.compose.adslibrary.AppClass.Companion.appContext
import com.test.compose.adslibrary.navigation.AppNavHost
import io.monetize.kit.sdk.ads.interstitial.InterControllerConfig
import io.monetize.kit.sdk.core.utils.init.AdSdkInitializer
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val adSdkInitializer: AdSdkInitializer by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (appContext as AppClass).initializeAppClass()

        adSdkInitializer.init(
            InterControllerConfig(
                openAdId = "ca-app-pub-3940256099942544/9257395921",
                splashId = "ca-app-pub-3940256099942544/1033173712",
                appInterIds = listOf(
                    "ca-app-pub-3940256099942544/1033173712",
                    "ca-app-pub-3940256099942544/1033173712"
                ),
                splashInterEnable = true,
                openAdEnable = true,
                splashTime = 16L,
                interLoadingEnable = true,
                openAdLoadingEnable = true
            )
        )

        setContent {
            val paddingValues = WindowInsets.systemBars.asPaddingValues()
            val navHostController = rememberNavController()


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

