package com.example.cmpt362group1.navigation.planner

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth

@Composable
fun PlannerScreen(
    uiState: PlannerUiState = PlannerUiState.Content(emptyList()),
    onSearchChange: (String) -> Unit = {},
    onEventClick: (String) -> Unit = {},
    onEditClick: (String) -> Unit = {},
    onCreateClick: () -> Unit = {}
) {

    val colors = lightColorScheme(
        background = Color.White,
        surface = Color.White,
        primary = Color.Black,
        onSurface = Color.Black,
        onSurfaceVariant = Color(0xFF666666),
        outline = Color(0xFFDDDDDD)
    )

    MaterialTheme(colorScheme = colors) {
        PlannerScreenContent(
            uiState,
            onSearchChange,
            onEventClick,
            onEditClick,
            onCreateClick
        )
    }
}

@Composable
private fun PlannerScreenContent(
    uiState: PlannerUiState,
    onSearchChange: (String) -> Unit,
    onEventClick: (String) -> Unit,
    onEditClick: (String) -> Unit,
    onCreateClick: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            val q = (uiState as? PlannerUiState.Content)?.query.orEmpty()
            var text by rememberSaveable { mutableStateOf(q) }

            LaunchedEffect(q) { if (q != text) text = q }

            OutlinedTextField(
                value = text,
                onValueChange = {
                    text = it
                    onSearchChange(it)
                },
                placeholder = {
                    Text("Find Your Events", color = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                trailingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                shape = MaterialTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.outline,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .height(52.dp)
            )
        }
    ) { inner ->
        Box(Modifier.padding(inner)) {
            when (uiState) {

                PlannerUiState.Loading ->
                    CircularProgressIndicator(
                        Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )

                is PlannerUiState.Empty ->
                    Text(
                        text = uiState.hint,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.Center)
                    )

                is PlannerUiState.Error ->
                    Text(
                        text = uiState.message,
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center)
                    )

                is PlannerUiState.Content -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp)
                    ) {

                        uiState.sections.forEach { section ->
                            item {
                                Text(
                                    text = EventGrouping.niceDateHeader(section.date),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp)
                                )
                            }

                            items(section.items) { ew ->
                                EventRow(
                                    title = ew.event.title,
                                    location = ew.event.location,
                                    time = ew.startTime.toString(),
                                    createdBy = ew.event.createdBy,
                                    currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: "",
                                    onClick = { onEventClick(ew.event.id) },
                                    onEditClick = { onEditClick(ew.event.id) }
                                )
                                Spacer(Modifier.height(10.dp))
                            }

                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun EventRow(
    title: String,
    location: String,
    time: String,
    createdBy: String,
    currentUserId: String,
    onClick: () -> Unit,
    onEditClick: () -> Unit
) {
    val canEdit = createdBy == currentUserId

    Surface(
        shape = MaterialTheme.shapes.large,
        color = Color(0xFFF7F7F7),
        tonalElevation = 1.dp,
        shadowElevation = 2.dp,
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = location,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = time,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (canEdit) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(start = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Host",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Host",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

        }
    }
}



