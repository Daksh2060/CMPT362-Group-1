package com.example.cmpt362group1.navigation.explore

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cmpt362group1.database.Event
import com.example.cmpt362group1.database.EventViewModel
import com.example.cmpt362group1.database.UserViewModel
import com.google.android.gms.maps.model.LatLng

@Composable
fun MapStateHolder(eventViewModel: EventViewModel, userViewModel: UserViewModel) {
    val sfuLocations = listOf(
        CampusLocation("Burnaby", LatLng(49.279161057278586, -122.91807989898375), zoom = 15f),
        CampusLocation("Surrey", LatLng(49.18855095340025, -122.85009015452918), zoom = 17.3f),
        CampusLocation("Vancouver", LatLng(49.284572597611565, -123.11142976880664), zoom = 19f)
    )

    val cityNames = sfuLocations.map { it.name }
    val initialLocation = sfuLocations.first()
    var selectedLocation by remember { mutableStateOf(initialLocation) }

    val eventsState by eventViewModel.eventsState.collectAsState()
    val events: List<Event> = when (eventsState) {
        is EventViewModel.EventsUiState.Success ->
            (eventsState as EventViewModel.EventsUiState.Success).events
        else -> emptyList()
    }

    Log.d("MapStateHolder INFO", "Read events: $events")

    Box(modifier = Modifier.fillMaxSize()) {
        MapScreen(
            selectedLocation = selectedLocation,
            events = events,
            modifier = Modifier.fillMaxWidth()
        )

        SegmentFloatingBar(
            items = cityNames,
            initialSelectedItem = cityNames.first(),
            onItemSelected = { newCityName ->
                selectedLocation = sfuLocations.first { it.name == newCityName }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .wrapContentHeight()
                .align(Alignment.TopCenter)
                .padding(top = 12.dp)
        )
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


