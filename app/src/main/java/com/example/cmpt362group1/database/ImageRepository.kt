package com.example.cmpt362group1.database

import android.content.Context
import android.net.Uri
import java.util.UUID

interface ImageRepository {
    suspend fun uploadImage(
        uri: Uri,
        context: Context,
        path: ImageStoragePath
    ) : ImageUploadResult

    suspend fun deleteImage(imageUrl: String) : Boolean
}

//sealed class ImageStoragePath(val path: String) {
//    data class EventImage(val eventId: String) : ImageStoragePath("events/$eventId")
//    data class UserProfile(val userId: String) : ImageStoragePath("users/$userId/profile")
//    data class UserGallery(val userId: String, val imageId: String = UUID.randomUUID().toString()) :
//        ImageStoragePath("users/$userId/gallery/$imageId")
//}

sealed class ImageStoragePath(val path: String) {
    object EventImage : ImageStoragePath("events/")
    object UserProfile : ImageStoragePath("users/profile")
}

sealed class ImageUploadResult {
    data class Success(val url: String) : ImageUploadResult()
    data class Error(val message: String) : ImageUploadResult()
    object Loading : ImageUploadResult()
}