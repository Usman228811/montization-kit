package com.test.compose.adslibrary.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.test.compose.adslibrary.ui.main.MainScreen
import com.test.compose.adslibrary.ui.settings.SettingScreen
import com.test.compose.adslibrary.ui.splash.SplashScreen

@Composable
fun AppNavHost(
    navHostController: NavHostController,
) {

    val navigationActions = NavigationActions(navHostController)

    NavHost(
        navController = navHostController, startDestination = AppRoute.SplashRoute.route
    ) {
        composable(AppRoute.SplashRoute.route) {
            SplashScreen(
                moveToNext = {
                    gotoNext(AppRoute.MainRoute, navigationActions)
                })
        }

        composable(AppRoute.MainRoute.route) {
            MainScreen(gotoSettings = {
                gotoNext(AppRoute.SettingsRoute, navigationActions)

            })
        }

        composable(AppRoute.SettingsRoute.route) {
            SettingScreen()
        }


    }
}

fun gotoNext(appRoute: AppRoute, navigationActions: NavigationActions) {
    when (appRoute) {
        AppRoute.MainRoute -> {
            navigationActions.goToMainScreen()
        }

        AppRoute.SplashRoute -> {

        }

        AppRoute.SettingsRoute -> {
            navigationActions.goToSettingScreen()
        }
    }
}
