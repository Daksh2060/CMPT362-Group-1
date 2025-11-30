package com.example.cmpt362group1.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import com.example.cmpt362group1.R
import com.example.cmpt362group1.MainActivity
import com.example.cmpt362group1.navigation.explore.weather.WeatherHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
            Log.d("INFO WIDGET PROVIDER", "onReceive $appWidgetId")
            if (appWidgetId != -1) {
                updateWidget(context, AppWidgetManager.getInstance(context), appWidgetId)
            }
        }
    }

    private fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, id: Int) {
        val views = RemoteViews(context.packageName, R.layout.main_widget)

        // ===== REFRESH BUTTON =====
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

        // ===== TAP TO OPEN APP =====
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val openAppPending = PendingIntent.getActivity(
            context,
            id + 1000, // Different request code to avoid collision
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Make the entire content area clickable to open app
        views.setOnClickPendingIntent(R.id.widget_content_area, openAppPending)
        views.setOnClickPendingIntent(R.id.widget_events, openAppPending)

        // ===== LOAD EVENT DATA =====
        val prefs = context.getSharedPreferences("widget_data", Context.MODE_PRIVATE)
        val events = prefs.getStringSet("events", emptySet()) ?: emptySet()
        val lastUpdated = prefs.getLong("last_updated", 0)

        // Set events text
        val displayText = if (events.isEmpty()) {
            "No upcoming events\n\nTap to view your calendar"
        } else {
            buildDisplayText(events)
        }
        views.setTextViewText(R.id.widget_events, displayText)

        // Set last updated timestamp
        val lastUpdatedText = if (lastUpdated > 0) {
            val formatter = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
            "Updated ${formatter.format(Date(lastUpdated))}"
        } else {
            "Tap to open app"
        }
        views.setTextViewText(R.id.last_updated, lastUpdatedText)

        appWidgetManager.updateAppWidget(id, views)
    }

    private fun buildDisplayText(events: Set<String>): String {
        return buildString {
            events.take(3).forEachIndexed { index, eventString ->
                val parts = eventString.split("|")
                if (parts.size >= 6) {
                    val title = parts[0]
                    val location = parts[1]
                    val date = parts[2]
                    val time = parts[3]
                    val weather = parts[4]
                    val temp = parts[5]

                    // Event title - bold effect via caps or emphasis
                    append("📌 $title")

                    // Location
                    if (location.isNotBlank()) {
                        append("\n📍 $location")
                    }

                    // Date and time
                    if (date.isNotBlank()) {
                        append("\n🗓 $date")
                        if (time.isNotBlank()) {
                            append(" • $time")
                        }
                    }

                    // Weather info
                    if (weather.isNotBlank() && temp.isNotBlank()) {
                        val emoji = WeatherHelper.getWeatherSymbol(weather.lowercase())
                        val tempInt = temp.toDoubleOrNull()?.toInt() ?: 0
                        append("\n$emoji ${tempInt}°C")
                    }

                    // Add spacing between events
                    if (index < minOf(events.size, 3) - 1) {
                        append("\n\n")
                    }
                }
            }
        }
    }
}