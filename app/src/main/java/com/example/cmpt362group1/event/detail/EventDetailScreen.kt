package com.example.cmpt362group1.event.detail

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cmpt362group1.auth.AuthViewModel
import com.example.cmpt362group1.database.Event
import com.example.cmpt362group1.database.EventViewModel
import com.example.cmpt362group1.database.UserViewModel
import com.example.cmpt362group1.database.UserUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    eventId: String,
    eventViewModel: EventViewModel,
    userViewModel: UserViewModel,
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit
) {
    val eventState by eventViewModel.eventState.collectAsState()
    val userState by userViewModel.userState.collectAsState()

    LaunchedEffect(eventId) {
        eventViewModel.loadEvent(eventId)
    }

    LaunchedEffect(Unit) {
        val uid = authViewModel.getUserId()
        if (uid != null) {
            userViewModel.loadUser(uid)
        }
    }

    var isJoined by remember { mutableStateOf(false) }

    LaunchedEffect(userState, eventId) {
        val uid = authViewModel.getUserId()
        if (uid != null && userState is UserUiState.Success) {
            val user = (userState as UserUiState.Success).user
            isJoined = user.eventsJoined.contains(eventId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Event Detail") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        when (eventState) {
            is EventViewModel.EventUiState.Idle,
            is EventViewModel.EventUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is EventViewModel.EventUiState.Error -> {
                val message = (eventState as EventViewModel.EventUiState.Error).message
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            is EventViewModel.EventUiState.Success -> {
                val event = (eventState as EventViewModel.EventUiState.Success).event

                EventDetailContent(
                    event = event,
                    isJoined = isJoined,
                    onToggleJoin = {
                        val uid = authViewModel.getUserId()
                        if (uid != null) {
                            if (isJoined) {
                                userViewModel.removeJoinedEvent(uid, event.id)
                            } else {
                                userViewModel.addJoinedEvent(uid, event.id)
                            }
                            isJoined = !isJoined
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(20.dp)
                )
            }
        }
    }
}

@Composable
private fun EventDetailContent(
    event: Event,
    isJoined: Boolean,
    onToggleJoin: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = event.title,
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = event.location,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = "${event.startDate} ${event.startTime}  -  ${event.endDate} ${event.endTime}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (event.description.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Description",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = event.description,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (event.dressCode.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Dress Code: ${event.dressCode}",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = onToggleJoin,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isJoined) "Remove from Planner" else "Add to Planner")
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Comments (coming soon...)",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
