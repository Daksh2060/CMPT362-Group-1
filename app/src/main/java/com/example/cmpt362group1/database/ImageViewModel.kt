package com.example.cmpt362group1.database

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ImageViewModel (
    private val repository: ImageRepository = ImageRepositoryImpl()
) : ViewModel() {
    private val _uploadState = MutableStateFlow(ImageUploadState())
    val uploadState: StateFlow<ImageUploadState> = _uploadState.asStateFlow()

    fun uploadImage(
        uri: Uri,
        context: Context,
        path: ImageStoragePath,
        onSuccess: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            _uploadState.value = ImageUploadState(isLoading = true)

            when (val result = repository.uploadImage(uri, context, path)) {
                is ImageUploadResult.Success -> {
                    _uploadState.value = ImageUploadState(uploadedUrl = result.url)
                    onSuccess(result.url)
                }
                is ImageUploadResult.Error -> {
                    _uploadState.value = ImageUploadState(error = result.message)
                }
                is ImageUploadResult.Loading -> {}
            }
        }
    }

    fun resetState() {
        _uploadState.value = ImageUploadState()
    }
}

data class ImageUploadState(
    val isLoading: Boolean = false,
    val uploadedUrl: String? = null,
    val error: String? = null
)