package com.example.mywins

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

data class BottomNavItem(
    val label: String,
    val icon: ImageVector? = null,
    @DrawableRes val drawableRes: Int? = null,
    val route: String
)

@Composable
fun BottomNavBar(navController: NavController) {
    val items = listOf(
        BottomNavItem("Goals", icon = Icons.Default.CheckCircle, route = Screen.Main.route),
        BottomNavItem("Friends", drawableRes = R.drawable.friends, route = Screen.Friends.route),
        BottomNavItem("Statistics", drawableRes = R.drawable.statistics, route = Screen.Statistics.route),
        BottomNavItem("Profile", icon = Icons.Default.Person, route = Screen.Profile.route)
    )

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val selectedColor = Color(0xFF9F7AEA)

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = { navController.navigate(item.route) },
                icon = {
                    item.icon?.let {
                        Icon(
                            it,
                            contentDescription = item.label,
                            modifier = Modifier.size(28.dp),

                        )
                    } ?: item.drawableRes?.let {
                        Image(
                            painter = painterResource(id = it),
                            contentDescription = item.label,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                label = {
                    Text(
                        item.label,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = selectedColor,
                    selectedTextColor = selectedColor,
                    indicatorColor = selectedColor.copy(alpha = 0.1f),
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray
                )
            )
        }
    }
}