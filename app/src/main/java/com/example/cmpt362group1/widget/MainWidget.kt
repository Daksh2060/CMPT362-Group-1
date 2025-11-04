package com.example.cmpt362group1.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.cmpt362group1.R
import org.json.JSONArray

class MainWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (id in appWidgetIds) {
            updateWidget(context, appWidgetManager, id)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == "com.cmpt362group1.WIDGET_UPDATE") {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val ids = appWidgetManager.getAppWidgetIds(
                ComponentName(context, MainWidget::class.java)
            )
            for (id in ids) {
                updateWidget(context, appWidgetManager, id)
            }
        }
    }

    private fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, id: Int) {
        val views = RemoteViews(context.packageName, R.layout.main_widget)

        val prefs = context.getSharedPreferences("events", Context.MODE_PRIVATE)
        val json = prefs.getString("list", "[]") ?: "[]"

        // display the events in a super long string for now...
        val events = try {
            val array = JSONArray(json)
            buildString {
                if (array.length() == 0) {
                    append("No events yet :(")
                } else {
                    for (i in 0 until array.length().coerceAtMost(5)) {
                        val obj = array.getJSONObject(i)
                        append("📍 ${obj.getString("title")}\n")
                        append("   ${obj.getString("location")}\n")
                        append("   ${obj.getString("startDate")}\n\n")
                    }
                }
            }
        } catch (_: Exception) {
            "Error loading events"
        }

        views.setTextViewText(R.id.widget_text, events)
        appWidgetManager.updateAppWidget(id, views)
    }
}
