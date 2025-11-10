package com.example.cmpt362group1.navigation.explore

import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.cmpt362group1.database.Event
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.example.cmpt362group1.R
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.GroundOverlay
import com.google.maps.android.compose.GroundOverlayPosition
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch
import com.google.android.gms.maps.model.BitmapDescriptorFactory

@Composable
fun MapScreen(
    selectedLocation: CampusLocation,
    events: List<Event> = emptyList(),
    modifier: Modifier,
) {
    val context = LocalContext.current
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

    val overlayBitmap = remember {
        val options = BitmapFactory.Options().apply {
            inSampleSize = 2 // Increase to reduce quality
        }
        BitmapFactory.decodeResource(context.resources, R.drawable.burnaby_map, options)
    }


    val overlayBounds = remember {
        calculateOverlayBounds(
            px1 = 2535f, py1 = 1190f,
            px2 = 2720f, py2 = 1170f,
            lat1 = 49.2789336256193, lng1 = -122.91708559164219,
            lat2 = 49.27881833869711, lng2 = -122.91582093155128,
            imageWidth = 4650f,
            imageHeight = 1870f
        )
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = mapProperties
    ) {
        if (overlayBitmap != null) {
            GroundOverlay(
                position = GroundOverlayPosition.create(overlayBounds),
                image = BitmapDescriptorFactory.fromBitmap(overlayBitmap),
                transparency = 0.4f, // 1 = Fully trasnparent
                visible = true,
                bearing = 13f // Rotate
            )
        }

        events.forEach { event ->
            if (event.latitude != null && event.longitude != null) {
                Marker(
                    state = MarkerState(
                        position = LatLng(event.latitude, event.longitude)
                    ),
                    title = event.title,
                    snippet = event.location,
                )
            }
        }
    }
}

fun calculateOverlayBounds(
    px1: Float, py1: Float,
    px2: Float, py2: Float,
    lat1: Double, lng1: Double,
    lat2: Double, lng2: Double,
    imageWidth: Float,
    imageHeight: Float
): LatLngBounds {

    val latPerPixel = (lat2 - lat1) / (py2 - py1)
    val lngPerPixel = (lng2 - lng1) / (px2 - px1)

    val northLat = lat1 + latPerPixel * (0 - py1)
    val southLat = lat1 + latPerPixel * (imageHeight - py1)

    val westLng = lng1 + lngPerPixel * (0 - px1)
    val eastLng = lng1 + lngPerPixel * (imageWidth - px1)

    val finalNorth = maxOf(northLat, southLat)
    val finalSouth = minOf(northLat, southLat)
    val finalWest = minOf(westLng, eastLng)
    val finalEast = maxOf(westLng, eastLng)

    return LatLngBounds(
        LatLng(finalSouth, finalWest),
        LatLng(finalNorth, finalEast)
    )
}