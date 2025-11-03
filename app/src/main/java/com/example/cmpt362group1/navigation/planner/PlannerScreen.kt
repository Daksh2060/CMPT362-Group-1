package com.example.cmpt362group1.navigation.planner

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable


@Composable
fun PlannerScreen(
    uiState: PlannerUiState = PlannerUiState.Content(emptyList()),
    onSearchChange: (String) -> Unit = {},
    onEventClick: (String) -> Unit = {},
    onEditClick: (String) -> Unit = {},
    onCreateClick: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            val q = (uiState as? PlannerUiState.Content)?.query.orEmpty()
            var text by rememberSaveable { mutableStateOf(q) }
            LaunchedEffect(q) {
                if (q != text) text = q
            }

            OutlinedTextField(
                value = text,
                onValueChange = {
                    text = it
                    onSearchChange(it)
                },
                placeholder = { Text("Find Your Events") },
                trailingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                singleLine = true,
                enabled = true,
                readOnly = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .heightIn(min = 48.dp)
            )
        }


    ) { inner ->
        when (uiState) {
            PlannerUiState.Loading -> Box(Modifier.fillMaxSize().padding(inner)) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }
            is PlannerUiState.Empty -> Box(Modifier.fillMaxSize().padding(inner)) {
                Text(uiState.hint, Modifier.align(Alignment.Center))
            }
            is PlannerUiState.Error -> Box(Modifier.fillMaxSize().padding(inner)) {
                Text(uiState.message, Modifier.align(Alignment.Center))
            }
            is PlannerUiState.Content -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(inner),
                    contentPadding = PaddingValues(12.dp)
                ) {
                    uiState.sections.forEach { section ->
                        item {
                            Text(
                                text = EventGrouping.niceDateHeader(section.date),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp, horizontal = 4.dp)
                            )
                        }
                        items(
                            items = section.items,
                            key = { (it.event.title + it.event.startDate + it.event.startTime).hashCode() }
                        ) { ew ->
                            EventRow(
                                title = ew.event.title,
                                location = ew.event.location,
                                time = ew.start.toLocalTime().toString(),
                                onClick = { onEventClick(ew.event.title) },
                                onEditClick = { onEditClick(ew.event.title) }
                            )
                            Spacer(Modifier.height(8.dp))
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
    onClick: () -> Unit,
    onEditClick: () -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        tonalElevation = 2.dp,
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    location,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    time,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onEditClick) {
                Icon(Icons.Default.Settings, contentDescription = "Edit")
            }
        }
    }
}
