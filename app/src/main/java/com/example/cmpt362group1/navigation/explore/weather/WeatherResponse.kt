package com.example.cmpt362group1.navigation.explore.weather

data class WeatherResponse(
    val current: CurrentWeather?,
    val hourly: HourlyWeather?
)

data class CurrentWeather(
    val temperature: Double,
    val weathercode: Int,
    val time: String
)

data class HourlyWeather(
    val time: List<String>,
    val temperature_2m: List<Double>,
    val weathercode: List<Int>
)