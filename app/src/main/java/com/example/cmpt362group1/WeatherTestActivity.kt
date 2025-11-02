package com.example.cmpt362group1

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
import com.example.cmpt362group1.navigation.explore.weather.WeatherRepository
import com.example.cmpt362group1.navigation.explore.weather.WeatherResponse

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

    // Burnaby coordinates
    val latitude = 49.2488
    val longitude = -122.9805

    // Requested date/time
    val requestedDateTime = "2025-11-05T12:00"

    LaunchedEffect(Unit) {
        val repo = WeatherRepository()
        repo.getWeatherData(
            latitude = latitude,
            longitude = longitude,
            onSuccess = { weather ->
                val info = weather?.let { getWeatherAt(it, requestedDateTime) }
                weatherInfo = info ?: "Weather data unavailable for $requestedDateTime"
                Log.d("WeatherTest", weatherInfo)
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

// Returns weather + temperature for a specific time
fun getWeatherAt(weather: WeatherResponse, dateTime: String): String? {
    val times = weather.hourly?.time ?: return null
    val temps = weather.hourly.temperature_2m
    val codes = weather.hourly.weathercode

    val index = times.indexOf(dateTime)
    return if (index >= 0 && index < temps.size && index < codes.size) {
        val temp = temps[index]
        val code = codes[index]
        val condition = weatherCodeToString(code)
        "Weather at $dateTime: $condition, Temp: $temp °C"
    } else {
        null
    }
}

fun weatherCodeToString(code: Int): String {
    return when(code) {
        0 -> "Clear sky"
        1 -> "Mainly clear"
        2 -> "Partly cloudy"
        3 -> "Overcast"
        45 -> "Fog"
        48 -> "Depositing rime fog"
        51 -> "Light drizzle"
        53 -> "Moderate drizzle"
        55 -> "Dense drizzle"
        56 -> "Light freezing drizzle"
        57 -> "Dense freezing drizzle"
        61 -> "Slight rain"
        63 -> "Moderate rain"
        65 -> "Heavy rain"
        66 -> "Light freezing rain"
        67 -> "Heavy freezing rain"
        71 -> "Slight snow fall"
        73 -> "Moderate snow fall"
        75 -> "Heavy snow fall"
        77 -> "Snow grains"
        80 -> "Slight rain showers"
        81 -> "Moderate rain showers"
        82 -> "Violent rain showers"
        85 -> "Slight snow showers"
        86 -> "Heavy snow showers"
        95 -> "Thunderstorm"
        96 -> "Thunderstorm with slight hail"
        99 -> "Thunderstorm with heavy hail"
        else -> "Unknown"
    }
}

