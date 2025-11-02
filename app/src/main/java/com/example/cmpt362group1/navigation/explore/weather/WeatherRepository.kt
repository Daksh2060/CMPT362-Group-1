package com.example.cmpt362group1.navigation.explore.weather

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WeatherRepository {
    fun getWeatherData(
        latitude: Double,
        longitude: Double,
        onSuccess: (WeatherResponse?) -> Unit,
        onError: (String) -> Unit
    ) {
        RetrofitInstance.api.getWeather(latitude, longitude).enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                if (response.isSuccessful) {
                    onSuccess(response.body())
                } else {
                    onError("Error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                onError("Failed: ${t.message}")
            }
        })
    }

    fun getTemperatureAt(weather: WeatherResponse, dateTime: String): Double? {
        val index = weather.hourly?.time?.indexOf(dateTime)
        return if (index != null && index >= 0) weather.hourly?.temperature_2m?.get(index) else null
    }

}
