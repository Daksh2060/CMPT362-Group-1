package com.example.cmpt362group1.navigation.explore

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

@Composable
fun MapScreen(
    selectedLocation: CampusLocation,
    modifier: Modifier,
) {
    val restrictedBounds = LatLngBounds(
        LatLng(49.15, -123.2),
        LatLng(49.35, -122.8)
    )

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(selectedLocation.latLng, 12f)
    }

    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(selectedLocation) {
        val newPosition = CameraPosition.fromLatLngZoom(selectedLocation.latLng, 12f)
        coroutineScope.launch {
            cameraPositionState.animate(
                update = CameraUpdateFactory.newCameraPosition(newPosition),
                durationMs = 500
            )
        }
    }

    val mapProperties = remember {
        MapProperties(
            latLngBoundsForCameraTarget = restrictedBounds,
            minZoomPreference = 10.0f
        )
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = mapProperties
    ) {
    }
}
