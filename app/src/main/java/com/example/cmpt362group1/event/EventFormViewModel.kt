package com.example.cmpt362group1.event

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.cmpt362group1.database.Event

class EventFormViewModel() : ViewModel() {
    var formInput by mutableStateOf(Event())
        private set

    var imageUri: Uri? by mutableStateOf(null)
        private set

    fun setUserID(value: String) {
        formInput = formInput.copy(createdBy = value)
    }

    fun updateTitle(value: String) {
        formInput = formInput.copy(title = value)
    }

    fun updateLocation(value: String) {
        formInput = formInput.copy(location = value)
    }

    fun updateStartDate(value: String) {
        formInput = formInput.copy(startDate = value)
    }

    fun updateEndDate(value: String) {
        formInput = formInput.copy(endDate = value)
    }

    fun updateStartTime(value: String) {
        formInput = formInput.copy(startTime = value)
    }

    fun updateEndTime(value: String) {
        formInput = formInput.copy(endTime = value)
    }

    fun updateDescription(value: String) {
        formInput = formInput.copy(description = value)
    }

    fun updateDressCode(value: String) {
        formInput = formInput.copy(dressCode = value)
    }

    fun updateCoordinates(lat: Double, lng: Double) {
        formInput = formInput.copy(latitude = lat, longitude = lng)
    }

    fun updateImageUrl(url: String) {
        formInput = formInput.copy(imageUrl = url)
    }

    fun updateImageUri(uri: Uri?) {
        imageUri = uri
    }

    fun resetForm() {
        formInput = Event()
    }
}