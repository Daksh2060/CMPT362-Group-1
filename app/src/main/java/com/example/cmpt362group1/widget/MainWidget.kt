package com.example.cmpt362group1.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import com.example.cmpt362group1.R
import com.example.cmpt362group1.navigation.explore.weather.WeatherHelper

class MainWidget : AppWidgetProvider() {

    companion object {
        const val ACTION_REFRESH = "com.cmpt362group1.WIDGET_REFRESH"
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (id in appWidgetIds) {
            updateWidget(context, appWidgetManager, id)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) {
            val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
            Log.d("INFO WIDGET PROVIDER", "onReceive ${appWidgetId}")
            if (appWidgetId != -1) {
                updateWidget(context, AppWidgetManager.getInstance(context), appWidgetId)
            }
        }
    }

    private fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, id: Int) {
        val views = RemoteViews(context.packageName, R.layout.main_widget)

        val refreshIntent = Intent(context, MainWidget::class.java).apply {
            action = ACTION_REFRESH
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
        }

        val refreshPending = PendingIntent.getBroadcast(
            context,
            id,
            refreshIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        views.setOnClickPendingIntent(R.id.refresh_button, refreshPending)

        val prefs = context.getSharedPreferences("widget_data", Context.MODE_PRIVATE)
        val events = prefs.getStringSet("events", emptySet()) ?: emptySet()
        val lastUpdated = prefs.getLong("last_updated", 0)

        val displayText = if (events.isEmpty()) "No Upcoming Events" else buildDisplayText(events)
        views.setTextViewText(R.id.widget_events, displayText) //NEW - removed cast

        appWidgetManager.updateAppWidget(id, views)
    }

    private fun buildDisplayText(events: Set<String>): String { //NEW - added return type
        return buildString { //NEW - added return
            events.take(2).forEachIndexed { index, eventString ->
                val parts = eventString.split("|")
                if (parts.size >= 6) {
                    val title = parts[0]
                    val location = parts[1]
                    val date = parts[2]
                    val time = parts[3]
                    val weather = parts[4]
                    val temp = parts[5]

                    append("$title @")

                    if (location.isNotBlank()) {
                        append("📍$location  ")
                    }

                    if (weather.isNotBlank() && temp.isNotBlank()) {
                        val emoji = WeatherHelper.getWeatherSymbol(weather.lowercase())
                        val tempInt = temp.toDoubleOrNull()?.toInt() ?: 0
                        append("$emoji ${tempInt}°C\n")
                    }

                    if (date.isNotBlank()) {
                        append("🕐 $date")
                        if (time.isNotBlank()) {
                            append(" at $time")
                        }
                        append("\n")
                    }

                    if (index < events.size - 1) {
                        append("\n")
                    }
                }
            }
        }
    }
}