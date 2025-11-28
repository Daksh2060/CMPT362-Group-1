package com.example.cmpt362group1.event

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.cmpt362group1.database.Event

class EventFormViewModel() : ViewModel() {
    companion object {
        const val MAX_IMAGE_UPLOAD = 3
    }

    var formInput by mutableStateOf(Event())
        private set

    var imageUri: Uri? by mutableStateOf(null) // legacy
        private set

    private val _imageUris = mutableStateOf<List<Uri>>(emptyList())

    val imageUris: List<Uri> by _imageUris

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

    fun updateImageUrls(urls: List<String>) {
        formInput = formInput.copy(imageUrls = ArrayList(urls))
    }

    fun updateImageUri(uri: Uri?) { // legacy function
        imageUri = uri
    }

    fun updateImageUris(uris: List<Uri>) {
        val updatedUris = (_imageUris.value + uris).take(MAX_IMAGE_UPLOAD)
        _imageUris.value = updatedUris

        // legacy: for backwards compatibility
        updateImageUri(uris.first())
    }

    fun removeImageUri(index: Int) {
        _imageUris.value = _imageUris.value.toMutableList().apply {
            removeAt(index)
        }
    }

    fun resetForm() {
        formInput = Event()
    }

    fun loadFromEvent(event: Event) {
        formInput = event
    }
}