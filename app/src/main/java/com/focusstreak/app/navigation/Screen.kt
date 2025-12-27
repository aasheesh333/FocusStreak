package com.focusstreak.app.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Progress : Screen("progress")
    object Settings : Screen("settings")
}
