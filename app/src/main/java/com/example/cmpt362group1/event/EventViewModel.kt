package com.example.cmpt362group1.event
/**
 * Implementation of App Widget functionality.
 */

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import org.json.JSONArray
import org.json.JSONObject

class EventViewModel(private val context: Context) : ViewModel() {
    var formData by mutableStateOf(Event())
        private set

    var events = mutableStateOf<List<Event>>(emptyList())
        private set

    fun updateTitle(value: String) {
        formData = formData.copy(title = value)
    }

    fun updateLocation(value: String) {
        formData = formData.copy(location = value)
    }

    fun updateStartDate(value: String) {
        formData = formData.copy(startDate = value)
    }

    fun updateEndDate(value: String) {
        formData = formData.copy(endDate = value)
    }

    fun updateStartTime(value: String) {
        formData = formData.copy(startTime = value)
    }

    fun updateEndTime(value: String) {
        formData = formData.copy(endTime = value)
    }

    fun updateDescription(value: String) {
        formData = formData.copy(description = value)
    }

    fun updateDressCode(value: String) {
        formData = formData.copy(dressCode = value)
    }

    fun updateCoordinates(lat: Double, lng: Double) {
        formData = formData.copy(latitude = lat, longitude = lng)
    }

    fun saveEvent() {
        Log.d("INFO", "Saving ${formData}")
        events.value = events.value + formData
        _saveEvents()
        _updateWidget()
        Log.d("INFO", "Saved ${events.value}")
    }

    fun getEvents(): List<Event> {
        return events.value
    }

    fun resetForm() {
        formData = Event()
    }

    init {
        _loadEvents()
    }

    // Events below are temporary, since we don't have a backend/database yet
    private fun _saveEvents() {
        val prefs = context.getSharedPreferences("events", Context.MODE_PRIVATE)
        val json = JSONArray()
        events.value.forEach { e ->
            json.put(JSONObject().apply {
                put("title", e.title)
                put("location", e.location)
                put("startDate", e.startDate)
                put("endDate", e.endDate)
                put("startTime", e.startTime)
                put("endTime", e.endTime)
                put("description", e.description)
                put("dressCode", e.dressCode)
                if (e.latitude != null) put("latitude", e.latitude)
                if (e.longitude != null) put("longitude", e.longitude)
            })
        }
        prefs.edit().putString("list", json.toString()).apply()
    }
    private fun _loadEvents() {
        val prefs = context.getSharedPreferences("events", Context.MODE_PRIVATE)
        val json = prefs.getString("list", "[]") ?: "[]"

        val array = JSONArray(json)
        val loaded = mutableListOf<Event>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            loaded.add(Event(
                title = obj.optString("title", ""),
                location = obj.optString("location", ""),
                startDate = obj.optString("startDate", ""),
                endDate = obj.optString("endDate", ""),
                startTime = obj.optString("startTime", ""),
                endTime = obj.optString("endTime", ""),
                description = obj.optString("description", ""),
                dressCode = obj.optString("dressCode", ""),
                latitude = if (obj.has("latitude")) obj.getDouble("latitude") else null,
                longitude = if (obj.has("longitude")) obj.getDouble("longitude") else null
            ))
        }
        events.value = loaded
    }

    private fun _updateWidget() {
        val intent = Intent("com.cmpt362group1.WIDGET_UPDATE")
        context.sendBroadcast(intent)
    }
}
