package com.example.cmpt362group1.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun BottomNavigationBar(
    currentScreen: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(modifier = modifier) {
        NavigationBarItem(
            selected = currentScreen == "Explore",
            onClick = { onTabSelected("Explore") },
            icon = { Icon(Icons.Default.Place, contentDescription = "Explore") },
            label = { Text("Explore") }
        )
        NavigationBarItem(
            selected = currentScreen == "Planner",
            onClick = { onTabSelected("Planner") },
            icon = { Icon(Icons.Default.DateRange, contentDescription = "Planner") },
            label = { Text("Planner") }
        )
        NavigationBarItem(
            selected = currentScreen == "Profile",
            onClick = { onTabSelected("Profile") },
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Profile") }
        )
    }
}
