package com.example.cmpt362group1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.cmpt362group1.navigation.BottomNavigationBar
import com.example.cmpt362group1.navigation.explore.MapStateHolder
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

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                "Explore" -> MapStateHolder()
                "Planner" -> PlannerScreen()
                "Profile" -> ProfileScreen()
            }
        }

        BottomNavigationBar(
            currentScreen = selectedTab,
            onTabSelected = { selectedTab = it },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
