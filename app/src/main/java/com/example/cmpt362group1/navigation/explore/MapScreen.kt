package com.example.cmpt362group1.navigation.explore

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    val restrictedBounds = LatLngBounds(
        LatLng(49.15, -123.2),
        LatLng(49.35, -122.8)
    )

    val cameraPositionState = rememberCameraPositionState()

    LaunchedEffect(selectedLocation) {
        cameraPositionState.animate(
            update = CameraUpdateFactory.newLatLngZoom(selectedLocation.latLng, selectedLocation.zoom),
            durationMs = 500
        )
    }

    val mapProperties = remember {
        MapProperties(
            latLngBoundsForCameraTarget = restrictedBounds,
            minZoomPreference = 10.0f
        )
    }


    val futureEvents = events.filter { it.startDateTime() > Date() }

    Log.d("MapScreen INFO", "Drawing future events: $futureEvents")

    val markerBitmap = remember {
        try {
            val bitmap = android.graphics.BitmapFactory.decodeResource(
                context.resources,
                R.drawable.event_marker
            )
            bitmap?.scale(120, 120, false)

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
        futureEvents.forEach { event ->
            Log.d("MapScreen INFO", "${event.title} ${event.latitude} ${event.longitude}")
            if (event.latitude != null && event.longitude != null) {
                val markerIcon = markerBitmap?.let { bitmap ->
                    try {
                        BitmapDescriptorFactory.fromBitmap(bitmap)
                    } catch (e: Exception) {
                        Log.e("MapScreen INFO", "Error creating BitmapDescriptor", e)
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
                        if (dateTime != null) {
                            weatherRepository.getWeatherForDateTime(
                                latitude = event.latitude,
                                longitude = event.longitude,
                                dateTime = dateTime,
                                onSuccess = { result -> weatherData = result },
                                onError = { error -> weatherError = error }
                            )
                        } else {
                            weatherError = "Could not parse date/time format"
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

fun Event.startDateTime(): Date {
    return try {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.US)
        dateFormat.parse("$startDate $startTime") ?: Date(0)
    } catch (e: Exception) {
        Log.e("MapScreen", "Error parsing event date/time: $startDate $startTime", e)
        Date(0)
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    if (value.isNotEmpty()) {
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = label,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            color = Color(0xFF555555)
        )

        Text(
            text = value,
            fontSize = 15.sp,
            color = Color.Black
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
