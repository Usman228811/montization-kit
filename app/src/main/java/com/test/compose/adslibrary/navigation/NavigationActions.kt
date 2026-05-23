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

    val goToMainScreen2: () -> Unit = {
        navHostController.navigate(AppRoute.MainRoute2.route)
    }
    val goToInterAdsScreen: () -> Unit = {
        navHostController.navigate(AppRoute.InterAdsRoute.route)
    }
    val gotoPremiumScreen: () -> Unit = {
        navHostController.navigate(AppRoute.SubscriptionRoute.route)
    }
    val gotoBannerScreen: () -> Unit = {
        navHostController.navigate(AppRoute.BannerRoute.route)
    }
    val gotoNativeAdsScreen: () -> Unit = {
        navHostController.navigate(AppRoute.NativeAdsRoute.route)
    }
    val gotoFullNativeScreen: () -> Unit = {
        navHostController.navigate(AppRoute.FullNativeRoute.route)
    }
}