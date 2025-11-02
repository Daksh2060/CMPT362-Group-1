package com.example.cmpt362group1.event

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class EventViewModel : ViewModel() {
    var formData by mutableStateOf(Event())
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
        // TODO: connect firebase etc.
        Log.d("INFO", formData.toString())
    }

    fun resetForm() {
        formData = Event()
    }
}