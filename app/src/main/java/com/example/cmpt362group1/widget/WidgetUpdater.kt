package com.example.cmpt362group1.widget

import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.cmpt362group1.database.Event
import com.example.cmpt362group1.navigation.explore.formatDateTimeForWeatherApi
import com.example.cmpt362group1.navigation.explore.weather.WeatherRepository
import com.example.cmpt362group1.navigation.explore.weather.WeatherResult
import androidx.core.content.edit
import com.example.cmpt362group1.navigation.explore.startDateTime
import java.util.Date

object WidgetUpdater {
    fun updateWidget(context: Context, events: List<Event>) {
        Log.d("INFO WIDGET", "Update widget with event list length: ${events.size}")
        val weatherRepository = WeatherRepository()

        val prefs = context.getSharedPreferences("widget_data", Context.MODE_PRIVATE)
        prefs.edit { clear() }

        events
            .filter { it ->
                it.latitude != null &&
                        it.longitude != null &&
                        it.startDate.isNotEmpty() &&
                        it.startDateTime().after(
                            Date()
                        )
            }
            .sortedBy { it ->
                it.startDateTime()
            }
            .take(3)
            .forEach { event ->
                val dateTime = formatDateTimeForWeatherApi(event.startDate, event.startTime)

                weatherRepository.getWeatherForDateTime(
                    event.latitude!!,
                    event.longitude!!,
                    dateTime!!,
                    onSuccess = { weather ->
                        Log.d("INFO WIDGET", "Saving event with weather $weather")
                        saveEventWithWeather(context, event, weather)
                        refreshWidget(context)
                    },
                    onError = { error ->
                        Log.d("INFO WIDGET", "Saving event with no weather: $error")
                        saveEventWithWeather(context, event, null)
                        refreshWidget(context)
                    }
                )
            }
    }

    @SuppressLint("MutatingSharedPrefs")
    private fun saveEventWithWeather(
        context: Context,
        event: Event,
        weather: WeatherResult?
    ) {
        val prefs = context.getSharedPreferences("widget_data", Context.MODE_PRIVATE)
        val existingData = prefs.getStringSet("events", mutableSetOf()) ?: mutableSetOf()

        val eventData = buildString {
            append(event.title).append("|")
            append(event.location).append("|")
            append(event.startDate).append("|")
            append(event.startTime).append("|")
            append(weather?.condition ?: "").append("|")
            append(weather?.temperature ?: "")
        }

        Log.d("INFO WIDGET", "Saving event to prefs: $eventData")

        existingData.add(eventData)
        prefs.edit {
            putStringSet("events", existingData)
                .putLong("last_updated", System.currentTimeMillis())
        }
    }

    private fun refreshWidget(context: Context) {
        Log.d("INFO WIDGET", "Refreshing widget")
        val intent = Intent(context, MainWidget::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            val ids = AppWidgetManager.getInstance(context)
                .getAppWidgetIds(ComponentName(context, MainWidget::class.java))
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        }
        context.sendBroadcast(intent)
    }
}