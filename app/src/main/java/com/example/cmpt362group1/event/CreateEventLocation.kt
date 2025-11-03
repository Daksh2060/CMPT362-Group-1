package com.example.cmpt362group1.event

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventLocation(
    onBack: () -> Unit,
    onConfirm: (Double, Double) -> Unit,
    viewModel: EventViewModel,
) {
    val initialLocation = viewModel.formData.latitude to viewModel.formData.longitude

    var selectedLat by remember {
        mutableStateOf(initialLocation.first ?: 49.25)
    }
    var selectedLng by remember {
        mutableStateOf(initialLocation.second ?: -123.0)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Event Location") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        floatingActionButtonPosition = FabPosition.Start,
        floatingActionButton = {
            Fab(
                label = "Create",
                icon = Icons.Default.CheckCircle,
                onClick = { onConfirm(selectedLat, selectedLng) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                LocationPicker(
                    modifier = Modifier.fillMaxSize(),
                    selectedLocation = LatLng(selectedLat, selectedLng),
                    onLocationSelected = { latLng ->
                        selectedLat = latLng.latitude
                        selectedLng = latLng.longitude
                    }
                )
            }
        }
    }
}

@Composable
fun LocationPicker(
    modifier: Modifier = Modifier,
    selectedLocation: LatLng,
    onLocationSelected: (LatLng) -> Unit
) {
    val restrictedBounds = LatLngBounds(
        LatLng(49.15, -123.2),
        LatLng(49.35, -122.8)
    )

    val mapProperties = MapProperties(
        latLngBoundsForCameraTarget = restrictedBounds,
        minZoomPreference = 10.0f
    )

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(selectedLocation, 13f)
    }

    GoogleMap(
        modifier = modifier,
        properties = mapProperties,
        cameraPositionState = cameraPositionState,
        onMapClick = { latLng ->
            onLocationSelected(latLng)
        }
    ) {
        Marker(
            state = MarkerState(position = selectedLocation), // pin will change locations on click
            title = "Event Location",
            snippet = "Tap map to move pin"
        )
    }
}

