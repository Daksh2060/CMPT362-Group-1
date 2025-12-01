package com.example.cmpt362group1.database

import android.content.Context
import android.net.Uri

interface ImageRepository {
    suspend fun uploadImage(
        uri: Uri,
        context: Context,
        path: ImageStoragePath
    ) : ImageUploadResult

    suspend fun uploadImages(
        uris: List<Uri>,
        context: Context,
        path: ImageStoragePath
    ): BatchImageUploadResult

    suspend fun deleteImages(imageUrls: List<String>) : Boolean
}

sealed class ImageStoragePath(val path: String) {
    object EventImage : ImageStoragePath("events/")
    object UserProfile : ImageStoragePath("users/profile")
}

sealed class ImageUploadResult {
    data class Success(val url: String) : ImageUploadResult()
    data class Error(val message: String) : ImageUploadResult()
    object Loading : ImageUploadResult()
}

data class BatchImageUploadResult(
    val successUrls: List<String>,
    val errors: List<String>,
    val totalUploaded: Int,
    val totalFailed: Int
) {
    val isCompleteFailure: Boolean get() = totalUploaded == 0
}