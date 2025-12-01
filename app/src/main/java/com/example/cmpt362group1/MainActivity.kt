package com.example.cmpt362group1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import com.example.cmpt362group1.auth.AuthViewModel
import com.example.cmpt362group1.database.EventRepository
import com.example.cmpt362group1.database.EventRepositoryImpl
import com.example.cmpt362group1.database.EventViewModel
import com.example.cmpt362group1.database.UserRepository
import com.example.cmpt362group1.database.UserRepositoryImpl
import com.example.cmpt362group1.database.UserViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val eventRepository: EventRepository = EventRepositoryImpl()
        EventViewModel(eventRepository)

        val userRepository: UserRepository = UserRepositoryImpl()
        val userViewModel = UserViewModel(userRepository)
        AuthViewModel(userViewModel = userViewModel)

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AuthScreen()
                }
            }
        }
    }
}
