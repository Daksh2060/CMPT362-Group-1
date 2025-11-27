package com.example.cmpt362group1.navigation.explore

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cmpt362group1.database.Event
import com.example.cmpt362group1.database.EventViewModel
import com.example.cmpt362group1.database.UserViewModel
import com.example.cmpt362group1.widget.WidgetUpdater
import com.google.android.gms.maps.model.LatLng


@Composable
fun MapStateHolder(
    onEventSelected: (String) -> Unit,
    onOpenSwipeDecider: () -> Unit = {},
    eventViewModel: EventViewModel = viewModel(),
) {
    val sfuLocations = listOf(
        CampusLocation("Burnaby", LatLng(49.279161057278586, -122.91807989898375), zoom = 15f),
        CampusLocation("Surrey", LatLng(49.18855095340025, -122.85009015452918), zoom = 17.3f),
        CampusLocation("Vancouver", LatLng(49.284572597611565, -123.11142976880664), zoom = 19f)
    )

    val context = LocalContext.current

    val cityNames = sfuLocations.map { it.name }
    val initialLocation = sfuLocations.first()
    var selectedLocation by remember { mutableStateOf(initialLocation) }

    val eventsState by eventViewModel.eventsState.collectAsState()
    val events: List<Event> = when (eventsState) {
        is EventViewModel.EventsUiState.Success ->
            (eventsState as EventViewModel.EventsUiState.Success).events
        else -> emptyList()
    }

    LaunchedEffect(events) {
        WidgetUpdater.updateWidget(context, events) // push updates for widget
    }

    Log.d("MapStateHolder INFO", "Read events: $events")

    Box(modifier = Modifier.fillMaxSize()) {
        MapScreen(
            selectedLocation = selectedLocation,
            events = events,
            modifier = Modifier.fillMaxWidth(),
            onEventSelected = onEventSelected
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SegmentFloatingBar(
                items = cityNames,
                initialSelectedItem = cityNames.first(),
                onItemSelected = { newCityName ->
                    selectedLocation = sfuLocations.first { it.name == newCityName }
                },
                modifier = Modifier
                    .fillMaxWidth()
            )

            Button(
                onClick = onOpenSwipeDecider,
                modifier = Modifier
                    .align(Alignment.Start)
                    .shadow(elevation = 6.dp, shape = RoundedCornerShape(12.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Swipe to Decide",
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun SegmentFloatingBar(
    items: List<String>,
    initialSelectedItem: String,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selected by remember { mutableStateOf(initialSelectedItem) }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
        ) {
            items.forEachIndexed { index, item ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            selected = item
                            onItemSelected(item)
                        }
                        .then(
                            if (selected == item) Modifier.background(Color(0xFFF2F2F2))
                            else Modifier
                        )
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item,
                        color = if (selected == item) Color.Black else Color.Gray,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                }
                if (index < items.lastIndex) {
                    Spacer(
                        modifier = Modifier
                            .width(1.dp)
                            .height(32.dp)
                            .background(Color(0xFFE9E9E9))
                    )

                }
            }
        }
    }
}
