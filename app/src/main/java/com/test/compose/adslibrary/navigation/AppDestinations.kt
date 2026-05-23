package com.test.compose.adslibrary.navigation

import kotlinx.serialization.Serializable


sealed class AppRoute(val route: String) {
    data object SplashRoute : AppRoute("splash")

    data object MainRoute : AppRoute("main")

    data object MainRoute2 : AppRoute("main2")

    data object SubscriptionRoute : AppRoute("Subscription")
    data object BannerRoute : AppRoute("BannerRoute")
    data object NativeAdsRoute : AppRoute("NativeAdsRoute")
    data object FullNativeRoute : AppRoute("FullNativeRoute")
    data object InterAdsRoute : AppRoute("InterAdsRoute")

}