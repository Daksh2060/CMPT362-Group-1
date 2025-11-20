package com.example.cmpt362group1.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.cmpt362group1.Route

@Composable
fun BottomNavigationBar(
    currentScreen: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = Color.White,
        contentColor = Color.Black
    ) {
        NavigationBarItem(
            selected = currentScreen == Route.Explore.route,
            onClick = { onTabSelected(Route.Explore.route) },
            icon = { Icon(Icons.Default.Place, contentDescription = "Explore") },
            label = { Text("Explore") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Black,
                unselectedIconColor = Color.Gray,
                selectedTextColor = Color.Black,
                unselectedTextColor = Color.Gray,
                indicatorColor = Color(0xFFE0E0E0)
            )
        )
        NavigationBarItem(
            selected = currentScreen == Route.Planner.route,
            onClick = { onTabSelected(Route.Planner.route) },
            icon = { Icon(Icons.Default.DateRange, contentDescription = "Planner") },
            label = { Text("Planner") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Black,
                unselectedIconColor = Color.Gray,
                selectedTextColor = Color.Black,
                unselectedTextColor = Color.Gray,
                indicatorColor = Color(0xFFE0E0E0)
            )
        )
        NavigationBarItem(
            selected = currentScreen == Route.Profile.route,
            onClick = { onTabSelected(Route.Profile.route) },
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Profile") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Black,
                unselectedIconColor = Color.Gray,
                selectedTextColor = Color.Black,
                unselectedTextColor = Color.Gray,
                indicatorColor = Color(0xFFE0E0E0)
            )
        )
    }
}
