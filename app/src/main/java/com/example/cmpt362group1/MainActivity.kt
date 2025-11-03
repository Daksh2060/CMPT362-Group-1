package com.example.cmpt362group1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.cmpt362group1.event.CreateEvent
import com.example.cmpt362group1.event.Fab
import com.example.cmpt362group1.navigation.BottomNavigationBar
import com.example.cmpt362group1.navigation.explore.MapStateHolder
import com.example.cmpt362group1.navigation.planner.PlannerHost
import com.example.cmpt362group1.navigation.planner.PlannerScreen
import com.example.cmpt362group1.navigation.profile.ProfileScreen
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainScreen()
                }
            }
        }
    }
}

@Composable
fun MainScreen() {
    var selectedTab by remember { mutableStateOf("Explore") }
    var isCreatingEvent by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                currentScreen = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        },
        floatingActionButtonPosition = FabPosition.Start,
        floatingActionButton = {
            Fab(
                label = "Create Event",
                icon = Icons.Default.Add,
                onClick = { isCreatingEvent = true },
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
        ) {
            when (selectedTab) {
                "Explore" -> MapStateHolder()
                "Planner" -> PlannerHost()
                "Profile" -> ProfileScreen()
            }
        }
    }

    if (isCreatingEvent) {
        CreateEvent(
            onExit = { isCreatingEvent = false }
        )
    }
}
