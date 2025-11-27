package com.example.cmpt362group1.event

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.scale
import com.example.cmpt362group1.R
import com.google.android.gms.maps.model.BitmapDescriptorFactory
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
    eventFormViewModel: EventFormViewModel,
) {
    val initialLocation = eventFormViewModel.formInput.latitude to eventFormViewModel.formInput.longitude

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
                    currentLocation = LatLng(selectedLat, selectedLng),
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
    currentLocation: LatLng,
    onLocationSelected: (LatLng) -> Unit
) {
    val context: Context = LocalContext.current

    var markerState by remember { mutableStateOf(MarkerState(position = currentLocation)) }
    LaunchedEffect(markerState.position) { // observe marker position changes from dragging
        snapshotFlow { markerState.position }
            .collect { position ->
                if (position != currentLocation) {
                    onLocationSelected(position)
                }
            }
    }

    val mapProperties = remember {
        MapProperties(
            latLngBoundsForCameraTarget = LatLngBounds(
                LatLng(49.15, -123.2),
                LatLng(49.35, -122.8)
            ),
            minZoomPreference = 10.0f
        )
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(49.25, -123.0),
            13f
        )
    }

    val markerIcon = remember {
        try {
            val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.event_marker)
            val scaledBitmap = bitmap.scale(120, 120)
            bitmap.recycle() // Free memory
            BitmapDescriptorFactory.fromBitmap(scaledBitmap)
        } catch (e: Exception) {
            Log.e("MapScreen", "Error loading marker icon", e)
            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
        }
    }

    GoogleMap(
        modifier = modifier,
        properties = mapProperties,
        cameraPositionState = cameraPositionState,
    ) {
        Marker(
            state = markerState,
            title = "Event Location",
            icon = markerIcon,
            snippet = "Drag pin or tap map to adjust location",
            draggable = true
        )
    }
}

