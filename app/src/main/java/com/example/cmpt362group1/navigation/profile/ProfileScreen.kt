package com.example.cmpt362group1.navigation.profile

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
    userViewModel: UserViewModel,
    profileViewModel: ProfileViewModel = ProfileViewModel()
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

    // Load user's events
    LaunchedEffect(userProfile.eventsJoined) {
        profileViewModel.loadUserEvents(userProfile.eventsJoined)
    }

    NavHost(
        navController = navController,
        startDestination = "profile_view"
    ) {
        composable("profile_view") {
            ProfileView(navController, authViewModel, userProfile, profileViewModel)
        }
        composable("edit_profile") {
            EditProfileScreen(navController, userViewModel, userProfile)
        }
    }
}

@Composable
fun ProfileView(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    userProfile: User,
    profileViewModel: ProfileViewModel
) {
    val eventsState by profileViewModel.eventsState.collectAsState()

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
                text = "My Planner Events",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )
        }

        // past events grid (Instagram-like)
        item {
            when (val state = eventsState) {
                is ProfileEventsState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is ProfileEventsState.Success -> {
                    if (state.events.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No events in your planner yet",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        PastEventsGrid(events = state.events)
                    }
                }
                is ProfileEventsState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Error loading events: ${state.message}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                ProfileEventsState.Idle -> {}
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))

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
fun PastEventsGrid(events: List<Event>) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 600.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(events) { event ->
            EventGridItem(event)
        }
    }
}

@Composable
fun EventGridItem(event: Event) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(4.dp))
            .clickable { /* TODO: Navigate to event detail */ }
    ) {
        // Event image
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(event.imageUrl.ifEmpty { "https://via.placeholder.com/300" })
                .crossfade(true)
                .build(),
            contentDescription = event.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Gradient overlay at bottom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.7f)
                        )
                    )
                )
        )

        // Event title overlay
        Text(
            text = event.title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp)
        )
    }
}
