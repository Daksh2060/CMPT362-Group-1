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
            0 -> "Clear Sky"
            1 -> "Mainly Clear"
            2 -> "Partly Cloudy"
            3 -> "Overcast"
            45 -> "Fog"
            48 -> "Depositing Rime Fog"
            51 -> "Light Drizzle"
            53 -> "Moderate Drizzle"
            55 -> "Dense drizzle"
            56 -> "Light Freezing Drizzle"
            57 -> "Dense Freezing Drizzle"
            61 -> "Slight Rain"
            63 -> "Moderate Rain"
            65 -> "Heavy Rain"
            66 -> "Light Freezing Rain"
            67 -> "Heavy Freezing Rain"
            71 -> "Slight Snow Fall"
            73 -> "Moderate Snow Fall"
            75 -> "Heavy Snow Fall"
            77 -> "Snow Grains"
            80 -> "Slight Rain Showers"
            81 -> "Moderate Rain Showers"
            82 -> "Violent Rain Showers"
            85 -> "Slight Snow Showers"
            86 -> "Heavy Snow Showers"
            95 -> "Thunderstorm"
            96 -> "Thunderstorm with Slight Hail"
            99 -> "Thunderstorm with Heavy Hail"
            else -> "Unknown"
        }
    }
}
