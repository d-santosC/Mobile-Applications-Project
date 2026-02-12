package com.example.mywins

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mywins.ui.theme.MyWinsTheme
import com.google.firebase.FirebaseApp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.mywins.goal.AddGoalScreen
import com.example.mywins.goal.EditGoalScreen
import com.example.mywins.database.goal.Goal
import com.example.mywins.database.goal.GoalRepository
import com.example.mywins.friends.FriendsScreen
import com.example.mywins.login_register.LoginScreen
import com.example.mywins.login_register.SignUpScreen
import com.example.mywins.profile.EditProfileScreen
import com.example.mywins.profile.ProfileScreen

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        enableEdgeToEdge()
        setContent {
            MyWinsTheme {
                AppNavigator()
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigator() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Login.route) {
        composable(Screen.Login.route) { LoginScreen(navController) }
        composable(Screen.SignUp.route) {
            SignUpScreen(navController)
        }
        composable(Screen.Main.route) { MainScreen(navController) }

        composable("edit_goal/{goalId}") { backStackEntry ->
            val goalId = backStackEntry.arguments?.getString("goalId")?.toIntOrNull()
            val context = LocalContext.current
            val repository = remember { GoalRepository(context) }

            var goal by remember { mutableStateOf<Goal?>(null) }

            LaunchedEffect(goalId) {
                if (goalId != null) {
                    goal = repository.getGoalById(goalId)
                    val loadedGoal = repository.getGoalById(goalId)
                    Log.d("EditGoal", "Loaded goal frequency: ${loadedGoal?.frequency}")
                    goal = loadedGoal
                }
            }

            EditGoalScreen(navController, goal)
        }

        composable("edit_profile") {
            EditProfileScreen(navController)
        }
        composable("add_goal") { AddGoalScreen(navController) }
        composable(Screen.Statistics.route) { StatisticsScreen(navController) }
        composable(Screen.Friends.route) { FriendsScreen(navController) }
        composable(Screen.Profile.route) { ProfileScreen(navController) }
    }
}

