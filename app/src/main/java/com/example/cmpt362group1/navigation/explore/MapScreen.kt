package com.example.cmpt362group1.navigation.explore

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.cmpt362group1.R
import com.example.cmpt362group1.database.Event
import com.example.cmpt362group1.navigation.explore.weather.WeatherRepository
import com.example.cmpt362group1.navigation.explore.weather.WeatherResult
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
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.graphics.scale

@Composable
fun MapScreen(
    selectedLocation: CampusLocation,
    events: List<Event> = emptyList(),
    modifier: Modifier,
    onEventSelected: (String) -> Unit
) {
    val context = LocalContext.current
    var selectedEvent by remember { mutableStateOf<Event?>(null) }
    var weatherData by remember { mutableStateOf<WeatherResult?>(null) }
    var weatherError by remember { mutableStateOf<String?>(null) }
    val weatherRepository = remember { WeatherRepository() }
    val coroutineScope = rememberCoroutineScope()

    val restrictedBounds = LatLngBounds(
        LatLng(49.15, -123.2),
        LatLng(49.35, -122.8)
    )

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(selectedLocation.latLng, 12f)
    }

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

    Log.d("MapScreen INFO", "Drawing events: $events")

    val markerBitmap = remember {
        try {

            val bitmap = android.graphics.BitmapFactory.decodeResource(
                context.resources,
                R.drawable.event_marker
            )

            if (bitmap != null) {

                bitmap.scale(100, 100, false)
            } else {
                Log.e("MapScreen", "Failed to decode PNG marker icon")
                null
            }
        } catch (e: Exception) {
            Log.e("MapScreen", "Error loading PNG marker icon", e)
            null
        }
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = mapProperties
    ) {
        events.forEach { event ->
            if (event.latitude != null && event.longitude != null) {
                val markerIcon = markerBitmap?.let { bitmap ->
                    try {
                        BitmapDescriptorFactory.fromBitmap(bitmap)
                    } catch (e: Exception) {
                        Log.e("MapScreen", "Error creating BitmapDescriptor", e)
                        null
                    }
                }

                Marker(
                    state = MarkerState(
                        position = LatLng(event.latitude, event.longitude)
                    ),
                    title = event.title,
                    snippet = event.location,
                    icon = markerIcon,
                    onClick = {
                        selectedEvent = event
                        weatherData = null
                        weatherError = null

                        val dateTime = formatDateTimeForWeatherApi(event.startDate, event.startTime)
                        Log.d("MapScreen", "Formatted dateTime for weather API: $dateTime")
                        if (dateTime != null) {
                            weatherRepository.getWeatherForDateTime(
                                latitude = event.latitude,
                                longitude = event.longitude,
                                dateTime = dateTime,
                                onSuccess = { result ->
                                    weatherData = result

                                    Log.d("MapScreen", "Weather data received: ${result.temperature}°C, ${result.condition}"
                                    )
                                },
                                onError = { error ->
                                    weatherError = error
                                    Log.e("MapScreen", "Weather fetch error: $error for datetime: $dateTime"
                                    )
                                }
                            )
                        } else {
                            weatherError = "Could not parse date/time format"
                            Log.e("MapScreen", "Failed to format date: '${event.startDate}' time: '${event.startTime}'"
                            )
                        }
                        true
                    }
                )
            }
        }
    }

    selectedEvent?.let { event ->
        EventInfoDialog(
            event = event,
            weatherData = weatherData,
            weatherError = weatherError,
            onDismiss = { selectedEvent = null },
            onEventPageClick = { id ->
                onEventSelected(id)
                selectedEvent = null
            }
        )
    }
}

@Composable
fun EventInfoDialog(
    event: Event,
    weatherData: WeatherResult?,
    weatherError: String?,
    onDismiss: () -> Unit,
    onEventPageClick: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(12.dp))

                InfoRow(label = "Location", value = event.location)

                InfoRow(label = "Date", value = "${event.startDate} - ${event.endDate}")

                InfoRow(label = "Time", value = "${event.startTime} - ${event.endTime}")

                if (event.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Description",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = event.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (event.dressCode.isNotEmpty()) {
                    InfoRow(label = "Dress Code", value = event.dressCode)
                }

                Spacer(modifier = Modifier.height(12.dp))
                Divider()
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Expected Weather",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                when {
                    weatherData != null -> {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${String.format("%.1f", weatherData.temperature)}°C",
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = weatherData.condition,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    weatherError != null -> {
                        Column {
                            Text(
                                text = "Weather data unavailable",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = weatherError,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    else -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = "Loading weather...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Close")
                    }

                    Button(
                        onClick = {
                            onEventPageClick(event.id)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Event Page")
                    }
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    if (value.isNotEmpty()) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

fun formatDateTimeForWeatherApi(date: String, time: String): String? {
    return try {
        // Firestore formats:
        val inputDateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
        val inputTimeFormat = SimpleDateFormat("hh:mm a", Locale.US)

        val parsedDate = inputDateFormat.parse(date)
        val parsedTime = inputTimeFormat.parse(time)

        if (parsedDate != null && parsedTime != null) {
            val calendar = Calendar.getInstance()
            calendar.time = parsedDate

            val timeCal = Calendar.getInstance()
            timeCal.time = parsedTime

            calendar.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY))
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)

            val outputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:00", Locale.US)
            outputFormat.format(calendar.time)
        } else {
            null
        }
    } catch (e: Exception) {
        Log.e("MapScreen", "Error formatting date/time for weather API", e)
        null
    }
}
