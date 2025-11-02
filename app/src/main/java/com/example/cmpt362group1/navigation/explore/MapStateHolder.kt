package com.example.cmpt362group1.navigation.explore

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.LatLng

@Composable
fun MapStateHolder() {
    val sfuLocations = listOf(
        CampusLocation("Burnaby", LatLng(49.2781, -122.9197)),
        CampusLocation("Surrey", LatLng(49.1866, -122.8480)),
        CampusLocation("Vancouver", LatLng(49.2847, -123.1118))
    )

    val cityNames = sfuLocations.map { it.name }
    val initialLocation = sfuLocations.first()
    var selectedLocation by remember { mutableStateOf(initialLocation) }

    Box(modifier = Modifier.fillMaxSize()) {
        MapScreen(
            selectedLocation = selectedLocation,
            modifier = Modifier.fillMaxWidth(),
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
