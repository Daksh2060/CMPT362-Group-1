package com.example.cmpt362group1.navigation.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cmpt362group1.auth.AuthViewModel

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "profile_view"
    ) {
        composable("profile_view") {
            ProfileView(navController = navController, authViewModel = authViewModel)
        }
        composable("edit_profile") {
            EditProfileScreen(navController = navController)
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
    authViewModel: AuthViewModel
) {
    // profile data
    var profileIntro by remember {
        mutableStateOf("Hello! I'm a CS student passionate about mobile development. Love attending tech events and meeting new people!")
    }

    // Some samples
    val pastEvents = listOf(
        PastEvent("Tech Talk: Introduction to Kotlin", "Computer Science Club", "Oct 15, 2024", "Participant"),
        PastEvent("Hackathon 2024", "SFU Innovation Hub", "Sep 22-24, 2024", "Team Member"),
        PastEvent("Career Fair", "Career Services", "Sep 10, 2024", "Attendee"),
        PastEvent("AI Workshop", "Machine Learning Society", "Aug 28, 2024", "Volunteer"),
        PastEvent("Game Night", "Gaming Club", "Aug 15, 2024", "Organizer"),
        PastEvent("Study Session: Data Structures", "CS Study Group", "Jul 30, 2024", "Participant")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            // profile image in circular format
            Icon(
                imageVector = Icons.Outlined.Person,
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
                    text = profileIntro,
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
        }
    }
}

@Composable
fun PastEventCard(event: PastEvent) {
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

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = event.club,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = event.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = event.role,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}
