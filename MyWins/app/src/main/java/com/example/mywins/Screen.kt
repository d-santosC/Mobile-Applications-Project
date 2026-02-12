package com.example.mywins

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object SignUp : Screen("signUp")
    object Main : Screen("main")
    object EditGoal : Screen("editGoal")
    object Statistics : Screen("statistics")
    object Friends : Screen("friends")
    object Profile : Screen("profile")
}

@JvmInline
value class ScreenRoute(val route: String)