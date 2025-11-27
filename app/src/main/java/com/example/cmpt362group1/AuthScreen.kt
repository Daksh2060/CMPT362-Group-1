package com.example.cmpt362group1

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cmpt362group1.auth.AuthState
import com.example.cmpt362group1.auth.AuthViewModel
import com.example.cmpt362group1.auth.LoginScreen
import com.example.cmpt362group1.database.EventViewModel
import com.example.cmpt362group1.database.UserViewModel

@Composable
fun AuthScreen(
    authViewModel: AuthViewModel = viewModel(),
) {
    // will toggle screens depending on authentication status
    val authState by authViewModel.authState.collectAsState()

    when (val state = authState) {
        is AuthState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        // show login screen (default)
        is AuthState.Unauthenticated -> {
            LoginScreen(
                onLoginSuccess = {
                    // auth state will automatically update via listener
                }
            )
        }

        // only show main screen once user is authenticated
        is AuthState.Authenticated -> {
            MainScreen()
        }
    }
}
