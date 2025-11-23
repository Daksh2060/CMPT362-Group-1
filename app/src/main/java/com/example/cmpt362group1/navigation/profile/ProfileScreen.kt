package com.example.cmpt362group1.navigation.profile

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.cmpt362group1.auth.AuthViewModel
import com.example.cmpt362group1.database.Event
import com.example.cmpt362group1.database.EventViewModel
import com.example.cmpt362group1.database.User
import com.example.cmpt362group1.database.UserUiState
import com.example.cmpt362group1.database.UserViewModel
import androidx.compose.ui.unit.times
import androidx.core.net.toUri
import com.example.cmpt362group1.Route

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    userViewModel: UserViewModel,
    eventViewModel: EventViewModel,
    mainNavController: NavHostController
) {
    val navController = rememberNavController()

    val uid = remember { authViewModel.getUserId() }

    Log.d("INFO ProfileScreen", "User ID: $uid")

    LaunchedEffect(uid) {
        uid?.let {
            userViewModel.loadUser(it) // from FB
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
        is UserUiState.Error -> { return } // later
        UserUiState.Idle -> { return } // later
        is UserUiState.Success -> {}
    }

    val userProfile = (userState as UserUiState.Success).user

    NavHost(
        navController = navController,
        startDestination = "profile_view",
    ) {
        composable("profile_view") {
            ProfileView(
                navController = navController,
                mainNavController = mainNavController,
                authViewModel = authViewModel,
                userProfile = userProfile,
                eventViewModel = eventViewModel
            )
        }
        composable("edit_profile") {
            EditProfileScreen(navController, userViewModel, userProfile)
        }
    }
}

@Composable
fun ProfileView(
    navController: NavHostController,
    mainNavController: NavHostController, // for event details
    authViewModel: AuthViewModel,
    userProfile: User,
    eventViewModel: EventViewModel
) {
    val context = LocalContext.current
    val pastEventsIds = userProfile.eventsJoined

    // load events
    val eventsState by eventViewModel.eventsState.collectAsState()
    val pastEvents = remember(eventsState,pastEventsIds) {
        when (eventsState) {
            is EventViewModel.EventsUiState.Success -> {
                val allEvents = (eventsState as EventViewModel.EventsUiState.Success).events
                allEvents
                    .filter { event -> pastEventsIds.contains(event.id) }
                    .sortedWith(compareByDescending<Event> { it.startDate }
                        .thenByDescending { it.startTime })
            }
            else -> emptyList()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text =userProfile.username.ifEmpty { "username" },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
        }

        item {
            Row(
                modifier=Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                //Profile picture
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(userProfile.photoUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription ="Profile Picture",
                    modifier = Modifier
                        .size(86.dp)
                        .clip(CircleShape)
                        .border(1.dp, Color.LightGray, CircleShape)
                )

                Spacer(modifier = Modifier.width(16.dp))

                // define order
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "${userProfile.eventsJoined.size}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Events",
                            style =MaterialTheme.typography.bodyMedium,
                            fontSize= 14.sp
                        )
                    }

                    // Followers
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "${userProfile.followers}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontSize= 18.sp
                        )
                        Text(
                            text= "Followers",
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 14.sp
                        )
                    }

                    // Following
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "${userProfile.following}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Following",
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = userProfile.displayName.ifEmpty {
                            if (userProfile.firstName.isNotEmpty() || userProfile.lastName.isNotEmpty()) {
                                "${userProfile.firstName} ${userProfile.lastName}".trim()
                            } else {
                                "Name"
                            }
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )

                    if (userProfile.pronouns.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = userProfile.pronouns,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        item {
            if (userProfile.description.isNotEmpty()) {
                Text(
                    text = userProfile.description,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
            }
        }

        item {
            if (userProfile.link.isNotEmpty()) {
                Text(
                    text = userProfile.link,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .clickable {
                            val intent = Intent(Intent.ACTION_VIEW,
                                if (!userProfile.link.startsWith("http://") &&
                                    !userProfile.link.startsWith("https://")
                                ) {
                                    "https://${userProfile.link}"
                                } else {
                                    userProfile.link
                                }.toUri())
                            context.startActivity(intent)
                        }
                )
            } else {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { navController.navigate("edit_profile") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Edit profile", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        item {
            // Events divider
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Events header
            Text(
                text = "Events",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier= Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            )
        }

        // grid for events
        item {
            if (pastEvents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No events yet!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalArrangement=Arrangement.spacedBy(2.dp),
                    modifier = Modifier.height((pastEvents.size / 3 + 1) * 130.dp),
                    userScrollEnabled = false
                ) {
                    items(pastEvents) { event ->
                        ProfileEventGridItem(
                            event = event,
                            onClick = {
                                mainNavController.navigate("${Route.EventDetail.route}/${event.id}?readonly=true")
                            }
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))

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
