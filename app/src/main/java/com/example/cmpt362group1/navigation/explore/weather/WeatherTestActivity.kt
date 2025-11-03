package com.example.cmpt362group1.navigation.explore.weather

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class WeatherTestActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                WeatherTestScreen()
            }
        }
    }
}

@Composable
fun WeatherTestScreen() {
    var weatherInfo by remember { mutableStateOf("Loading weather data...") }

    val latitude = 49.2488
    val longitude = -122.9805
    val requestedDateTime = "2025-11-05T12:00"

    LaunchedEffect(Unit) {
        val repo = WeatherRepository()
        repo.getWeatherForDateTime(
            latitude = latitude,
            longitude = longitude,
            dateTime = requestedDateTime,
            onSuccess = { result ->
                weatherInfo = "At $requestedDateTime\n" +
                        "Temperature: ${result.temperature} °C\n" +
                        "Condition: ${result.condition}"
                Log.d("WeatherTest", "Temp: ${result.temperature}, Condition: ${result.condition}")
            },
            onError = { error ->
                weatherInfo = "Error: $error"
                Log.e("WeatherTest", error)
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = weatherInfo, style = MaterialTheme.typography.bodyLarge)
    }
}
