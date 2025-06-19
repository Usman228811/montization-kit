package com.test.compose.adslibrary.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class AppRoute {
    @Serializable
    data object SplashRoute : AppRoute()

    @Serializable
    data object MainRoute : AppRoute()

}