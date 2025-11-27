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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.scale
import com.example.cmpt362group1.R
import com.example.cmpt362group1.navigation.explore.CampusLocation
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

val PRESET_LOCATIONS = listOf(
    CampusLocation("SFU SUB", LatLng(49.278674781151594, -122.91826602450429), 17f),
    CampusLocation("West Mall Centre", LatLng(49.280005, -122.921964), 17f),
    CampusLocation("Academic Quadrangle", LatLng(49.279005, -122.916947), 17f),
    CampusLocation("W.A.C. Bennett Library", LatLng(49.279724, -122.918769), 17f),
    CampusLocation("SFU Burnaby", LatLng(49.27950740672236, -122.92027053404077), 17f),
    CampusLocation("SFU Surrey", LatLng(49.18746318600051, -122.84971226676801), 17f),
    CampusLocation("SFU Vancouver", LatLng(49.28449770398332, -123.1115119797306), 17f)
)

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

    val coroutineScope = rememberCoroutineScope()

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

    Box(modifier = modifier) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
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

        PresetLocationDropdown(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            onLocationSelected = { preset ->
                markerState = MarkerState(position = preset.latLng)
                onLocationSelected(preset.latLng)
                coroutineScope.launch {
                    cameraPositionState.animate(
                        update = CameraUpdateFactory.newLatLngZoom(
                            preset.latLng, preset.zoom
                        ),
                        durationMs = 500
                    )
                }
            }
        )
    }
}

@Composable
fun PresetLocationDropdown(
    modifier: Modifier = Modifier,
    onLocationSelected: (CampusLocation) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        FloatingActionButton(
            onClick = { expanded = true },
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Quick locations"
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            PRESET_LOCATIONS.forEach { preset ->
                DropdownMenuItem(
                    text = { Text(preset.name) },
                    onClick = {
                        onLocationSelected(preset)
                        expanded = false
                    }
                )
            }
        }
    }
}
