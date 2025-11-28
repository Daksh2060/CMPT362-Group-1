package com.example.cmpt362group1.database

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.io.output.ByteArrayOutputStream
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ImageRepositoryImpl(
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
): ImageRepository {
    override suspend fun uploadImage(
        uri: Uri,
        context: Context,
        path: ImageStoragePath
    ): ImageUploadResult {
        return try {
            val bitmap = getBitmapFromUri(uri, context)
            val compressedBytes = compressBitmap(bitmap)
            bitmap.recycle()

            val filename = "${UUID.randomUUID()}.jpg"
            val storageRef: StorageReference = storage.reference.child("${path.path}/$filename")

            storageRef.putBytes(compressedBytes).await() // upload to fb

            val downloadUrl = storageRef.downloadUrl.await().toString() // get url from fb
            ImageUploadResult.Success(downloadUrl)
        } catch (e: Exception) {
            ImageUploadResult.Error(e.message ?: "Error uploading image in repository!")
        }
    }

    override suspend fun uploadImages(
        uris: List<Uri>,
        context: Context,
        path: ImageStoragePath
    ): BatchImageUploadResult = coroutineScope {
        val results = uris.map { uri ->
            async {
                uploadImage(uri, context, path)
            }
        }.awaitAll()

        val successUrls = mutableListOf<String>()
        val errors = mutableListOf<String>()

        results.forEach { result ->
            when (result) {
                is ImageUploadResult.Success -> successUrls.add(result.url)
                is ImageUploadResult.Error -> errors.add(result.message)
                else -> {} // nop
            }
        }

        BatchImageUploadResult(
            successUrls = successUrls,
            errors = errors,
            totalUploaded = successUrls.size,
            totalFailed = errors.size
        )
    }

    override suspend fun deleteImages(imageUrls: List<String>): Boolean {
        return try {
            imageUrls.forEach { url ->
                val storageRef = storage.getReferenceFromUrl(url)
                storageRef.delete().await()
            }
            true
        } catch (e: Exception) {
            Log.d("Image Repository INFO", e.message!!)
            false
        }
    }
}

private fun compressBitmap(bitmap: Bitmap): ByteArray {
    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 5, outputStream)

    val compressedBytes = outputStream.toByteArray()

    outputStream.close()

    return compressedBytes
}

private fun getBitmapFromUri(uri: Uri, context: Context): Bitmap {
    val inputStream = context.contentResolver.openInputStream(uri)
    val bitmap = BitmapFactory.decodeStream(inputStream)
    inputStream?.close()
    return bitmap
}