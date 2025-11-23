package com.example.cmpt362group1.navigation.explore.weather

object WeatherHelper {
    fun getWeatherSymbol(condition: String): String {
        return when (condition.lowercase()) {
            "clear sky", "mainly clear" -> "☀️"
            "partly cloudy" -> "⛅"
            "overcast" -> "☁️"
            "fog", "depositing rime fog" -> "🌫️"
            "light drizzle", "moderate drizzle", "dense drizzle",
            "light freezing drizzle", "dense freezing drizzle",
            "slight rain", "moderate rain", "heavy rain",
            "light freezing rain", "heavy freezing rain",
            "slight rain showers", "moderate rain showers", "violent rain showers" -> "🌧️"
            "slight snow fall", "moderate snow fall", "heavy snow fall",
            "snow grains", "slight snow showers", "heavy snow showers" -> "❄️"
            "thunderstorm", "thunderstorm with slight hail", "thunderstorm with heavy hail" -> "⛈️"
            else -> "🌡️"
        }
    }
}