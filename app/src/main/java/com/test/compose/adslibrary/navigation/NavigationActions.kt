package com.test.compose.adslibrary.navigation

import androidx.navigation.NavHostController

class NavigationActions(private val navHostController: NavHostController) {


    val goToMainScreen: () -> Unit = {
        navHostController.navigate(AppRoute.MainRoute){
            popUpTo(AppRoute.SplashRoute){
                inclusive = true
            }
        }
    }
}