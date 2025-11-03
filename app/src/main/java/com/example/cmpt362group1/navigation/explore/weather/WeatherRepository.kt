package com.example.cmpt362group1.navigation.explore.weather

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

data class WeatherResult(
    val temperature: Double,
    val condition: String
)

class WeatherRepository {

    fun getWeatherForDateTime(
        latitude: Double,
        longitude: Double,
        dateTime: String,
        onSuccess: (WeatherResult) -> Unit,
        onError: (String) -> Unit
    ) {
        RetrofitInstance.api.getWeather(latitude, longitude).enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                if (!response.isSuccessful) {
                    onError("HTTP Error: ${response.code()}")
                    return
                }

                val weather = response.body()
                if (weather == null || weather.hourly == null) {
                    onError("No weather data available.")
                    return
                }

                val hourly = weather.hourly
                val index = hourly.time.indexOf(dateTime)

                if (index < 0 || index >= hourly.temperature_2m.size || index >= hourly.weathercode.size) {
                    onError("No matching weather data for $dateTime")
                    return
                }

                val temp = hourly.temperature_2m[index]
                val condition = weatherCodeToString(hourly.weathercode[index])
                onSuccess(WeatherResult(temp, condition))
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                onError("Network Failure: ${t.message}")
            }
        })
    }

    private fun weatherCodeToString(code: Int): String {
        return when (code) {
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
}
