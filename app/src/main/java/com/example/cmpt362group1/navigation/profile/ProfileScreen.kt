package com.example.cmpt362group1.navigation.profile

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.cmpt362group1.auth.AuthViewModel
import com.example.cmpt362group1.database.Event
import com.example.cmpt362group1.database.User
import com.example.cmpt362group1.database.UserUiState
import com.example.cmpt362group1.database.UserViewModel

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    userViewModel: UserViewModel
) {
    val navController = rememberNavController()

    val uid = authViewModel.getUserId()

    Log.d("INFO ProfileScreen", "User ID: $uid")

    LaunchedEffect(uid) {
        uid?.let {
            userViewModel.loadUser(it) // fetch from fb
        }
    }

    val userState by userViewModel.userState.collectAsState()
    when (val state = userState) {
        is UserUiState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return
        }
        is UserUiState.Error -> { return } // TODO
        UserUiState.Idle -> { return } // TODO
        is UserUiState.Success -> {} // NOP
    }

    val userProfile = (userState as UserUiState.Success).user

    NavHost(
        navController = navController,
        startDestination = "profile_view"
    ) {
        composable("profile_view") {
            ProfileView(navController, authViewModel, userProfile)
        }
        composable("edit_profile") {
            EditProfileScreen(navController, userViewModel, userProfile)
        }
    }
}

// for past events
data class PastEvent(
    val title: String,
    val club: String,
    val date: String,
    val role: String
)

@Composable
fun ProfileView(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    userProfile: User
) {
    // Some samples
    val pastEventsIds = userProfile.eventsJoined
    val pastEvents = emptyArray<Event>() // TODO: wire up the ids to get the real events

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            // profile image in circular format
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(userProfile.photoUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)

            )

            Spacer(modifier = Modifier.height(16.dp))

            // introduction
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text = userProfile.description,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // change profile button
            Button(
                onClick = { navController.navigate("edit_profile") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Change Profile")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // past events section header
            Text(
                text = "Past Events",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )
        }

        // past events list
        items(pastEvents) { event ->
            PastEventCard(event)
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            Button(
                onClick = { authViewModel.signOut() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Sign Out")
            }

            Button(
                onClick = { authViewModel.deleteUser({}, {}) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("DEBUG: Delete User & Sign Out")
            }
        }
    }
}

@Composable
fun PastEventCard(event: Event) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = event.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
