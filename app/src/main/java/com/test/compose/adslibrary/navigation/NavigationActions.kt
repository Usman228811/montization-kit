package com.test.compose.adslibrary.navigation

import androidx.navigation.NavHostController

class NavigationActions(private val navHostController: NavHostController) {


    val goToMainScreen: () -> Unit = {
        navHostController.navigate(AppRoute.MainRoute.route){
            popUpTo(AppRoute.SplashRoute.route){
                inclusive = true
            }
        }
    }

    val goToSettingScreen: () -> Unit = {
        navHostController.navigate(AppRoute.SubscriptionRoute.route)
    }
}