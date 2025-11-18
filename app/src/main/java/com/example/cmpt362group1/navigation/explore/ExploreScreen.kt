package com.example.cmpt362group1.navigation.explore

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.cmpt362group1.database.Event
import com.example.cmpt362group1.database.EventViewModel
import com.example.cmpt362group1.database.UserViewModel
import com.google.android.gms.maps.model.LatLng

@Composable
fun MapStateHolder(
    eventViewModel: EventViewModel,
    userViewModel: UserViewModel,
    onEventSelected: (String) -> Unit
) {
    val sfuLocations = listOf(
        CampusLocation("Burnaby", LatLng(49.2781, -122.9197)),
        CampusLocation("Surrey", LatLng(49.1866, -122.8480)),
        CampusLocation("Vancouver", LatLng(49.2847, -123.1118))
    )

    val cityNames = sfuLocations.map { it.name }
    val initialLocation = sfuLocations.first()
    var selectedLocation by remember { mutableStateOf(initialLocation) }

    // we need to subscribe to the
    val eventsState by eventViewModel.eventsState.collectAsState()
    val events: List<Event> = when (eventsState) {
        is EventViewModel.EventsUiState.Success ->
            (eventsState as EventViewModel.EventsUiState.Success).events
        else -> emptyList()
    }

    Log.d("MapStateHolder INFO", "Read events: ${events}")

    Box(modifier = Modifier.fillMaxSize()) {
        MapScreen(
            selectedLocation = selectedLocation,
            events = events,
            modifier = Modifier.fillMaxWidth(),
            onEventSelected = onEventSelected
        )

        Segment(
            items = cityNames,
            initialSelectedItem = cityNames.first(),
            onItemSelected = { newCityName ->
                selectedLocation = sfuLocations.first { it.name == newCityName }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
