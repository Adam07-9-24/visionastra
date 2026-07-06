package com.tecsup.visionastra.mobile.navigation

sealed class AppDestination(val route: String) {
    data object Splash : AppDestination("splash")
    data object Login : AppDestination("login")
    data object Dashboard : AppDestination("dashboard")
}
