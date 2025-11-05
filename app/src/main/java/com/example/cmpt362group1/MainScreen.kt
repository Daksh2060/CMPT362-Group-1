package com.example.cmpt362group1

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.cmpt362group1.event.CreateEvent
import com.example.cmpt362group1.event.Fab
import com.example.cmpt362group1.navigation.BottomNavigationBar
import com.example.cmpt362group1.navigation.explore.MapStateHolder
import com.example.cmpt362group1.navigation.planner.PlannerScreen
import com.example.cmpt362group1.navigation.profile.ProfileScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cmpt362group1.event.EventViewModel

@Composable
fun NavigationBar(currentRoute: String, navController: NavHostController) {
    AnimatedVisibility(
        visible = currentRoute != Route.CreateEvent.route,
    ) {
        BottomNavigationBar(
            currentScreen = currentRoute,
            onTabSelected = { route ->
                navController.navigate(route) {
                    popUpTo(Route.Explore.route) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )
    }
}

@Composable
fun FloatingActionButton(currentRoute: String, navController: NavHostController) {
    AnimatedVisibility(
        currentRoute == Route.Explore.route || currentRoute == Route.Planner.route
    ) {
        Fab(
            label = "Create Event",
            icon = Icons.Default.Add,
            onClick = { navController.navigate(Route.CreateEvent.route) },
        )
    }
}

@Composable
fun MainScreen(
    eventViewModel: EventViewModel = viewModel(),
    navController: NavHostController = rememberNavController()
) {
    val defaultRoute: String = Route.Explore.route
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route ?: defaultRoute

    Scaffold(
        bottomBar = {
            NavigationBar(currentRoute, navController)
        },
        floatingActionButtonPosition = FabPosition.Start,
        floatingActionButton = {
            FloatingActionButton(currentRoute, navController)
        }
    ) { innerPadding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = defaultRoute,
            ) {
                composable(Route.Explore.route) { MapStateHolder(eventViewModel) }

                composable(Route.Planner.route) { PlannerScreen() }

                composable(Route.Profile.route) { ProfileScreen() }

                composable(Route.CreateEvent.route) {
                    CreateEvent(
                        onExit = {
                            navController.popBackStack()
                        },
                        eventViewModel
                    )
                }
            }
        }
    }
}
