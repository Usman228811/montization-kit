package com.test.compose.adslibrary.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class AppRoute(val route: String) {
    @Serializable
    data object SplashRoute : AppRoute("splash")

    @Serializable
    data object MainRoute : AppRoute("main")

    @Serializable
    data object MainRoute2 : AppRoute("main2")

    @Serializable
    data object SubscriptionRoute : AppRoute("Subscription")

}