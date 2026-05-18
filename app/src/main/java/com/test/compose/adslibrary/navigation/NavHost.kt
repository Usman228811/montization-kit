package com.test.compose.adslibrary.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.test.compose.adslibrary.ui.banner.BannerScreen
import com.test.compose.adslibrary.ui.inter.InterAdsScreen
import com.test.compose.adslibrary.ui.main.MainScreen
import com.test.compose.adslibrary.ui.main2.MainScreen2
import com.test.compose.adslibrary.ui.nativead.FullNativeScreen
import com.test.compose.adslibrary.ui.nativead.NativeAdsScreen
import com.test.compose.adslibrary.ui.premium.SubscriptionScreen
import com.test.compose.adslibrary.ui.splash.SplashScreen

@Composable
fun AppNavHost(
    navHostController: NavHostController,
    languageChange: Boolean
) {

    val navigationActions = NavigationActions(navHostController)
    val startDestination = if (languageChange) {
        AppRoute.MainRoute.route
    } else {
        AppRoute.SplashRoute.route
    }

    NavHost(
        navController = navHostController, startDestination = startDestination
    ) {
        composable(AppRoute.SplashRoute.route) {
            SplashScreen(
                moveToNext = {
                    navigationActions.goToMainScreen()
                })
        }

        composable(AppRoute.MainRoute.route) {
            MainScreen(
                gotoBannerScreen = {
                    navigationActions.gotoBannerScreen()

                },
                gotoNativeAdsScreen = {
                    navigationActions.gotoNativeAdsScreen()
                },
                gotoSubscription = {
                    navigationActions.gotoPremiumScreen()

                },
                gotoMainScreen2 = {
                    navigationActions.goToMainScreen2()
                },
                gotoInterAds = {
                    navigationActions.goToInterAdsScreen()
                })
        }

        composable(AppRoute.SubscriptionRoute.route) {
            SubscriptionScreen()
        }

        composable(AppRoute.MainRoute2.route) {
            MainScreen2()
        }
        composable(AppRoute.BannerRoute.route) {
            BannerScreen()
        }
        composable(AppRoute.NativeAdsRoute.route) {
            NativeAdsScreen(
                gotoFullScreenNative = {
                    navigationActions.gotoFullNativeScreen()
                }
            )
        }
        composable(AppRoute.FullNativeRoute.route) {
            FullNativeScreen()
        }
        composable(AppRoute.InterAdsRoute.route) {
            InterAdsScreen()
        }


    }
}
