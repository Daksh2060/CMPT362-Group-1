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
import com.example.cmpt362group1.event.EventViewModel
import com.example.cmpt362group1.event.Fab
import com.example.cmpt362group1.navigation.BottomNavigationBar
import com.example.cmpt362group1.navigation.explore.MapStateHolder
import com.example.cmpt362group1.navigation.planner.PlannerScreen
import com.example.cmpt362group1.navigation.profile.ProfileScreen
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val eventViewModel = EventViewModel(applicationContext)

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainScreen(eventViewModel)
                }
            }
        }
    }
}
