package com.example.cmpt362group1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.LatLng

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MapStateHolder()
                }
            }
        }
    }
}

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
