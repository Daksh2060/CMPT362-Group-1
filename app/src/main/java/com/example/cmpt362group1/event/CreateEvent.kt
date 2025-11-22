package com.example.cmpt362group1.event

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cmpt362group1.auth.AuthViewModel
import com.example.cmpt362group1.database.EventViewModel
import com.example.cmpt362group1.database.ImageStoragePath
import com.example.cmpt362group1.database.ImageViewModel
import com.example.cmpt362group1.database.UserViewModel
import com.example.cmpt362group1.database.OperationUiState

@Composable
fun CreateEvent(
    onExit: () -> Unit,
    eventViewModel: EventViewModel,
    userViewModel: UserViewModel,
    authViewModel: AuthViewModel, // uid
) {
    val uid = authViewModel.getUserId()!!

    val STATE_FORM = 0
    val STATE_LOCATION = 1

    val context = LocalContext.current

    // Used to reset VM data
    val eventFormViewModel: EventFormViewModel = viewModel()
    val imageViewModel: ImageViewModel = viewModel()

    var step by remember { mutableStateOf(STATE_FORM) }
    when (step) {
        STATE_FORM -> {
            CreateEventDetails(
                onExit = onExit,
                onContinue = {
                    step = STATE_LOCATION
                },
                eventFormViewModel
            )
        }

        STATE_LOCATION -> {
            CreateEventLocation(
                onBack = { step = STATE_FORM },
                onConfirm = { lat, lng ->
                    eventFormViewModel.updateCoordinates(lat, lng)
                    eventFormViewModel.setUserID(uid)

                    processEventUpload(
                        eventViewModel,
                        eventFormViewModel,
                        userViewModel,
                        authViewModel,
                        imageViewModel,
                        context,
                        onExit,
                    )
                },
                eventFormViewModel
            )
        }
    }
}

fun processEventUpload(
    eventViewModel: EventViewModel,
    eventFormViewModel: EventFormViewModel,
    userViewModel: UserViewModel,
    authViewModel: AuthViewModel,
    imageViewModel: ImageViewModel,
    context: Context,
    onExit: () -> Unit
) {
    fun saveEvent() {
        val event = eventFormViewModel.formInput

        eventViewModel.saveEvent(
            event,
            onSuccess = { eventId ->
                val uid = authViewModel.getUserId()
                if (uid != null) {
                    userViewModel.addJoinedEvent(uid, eventId)
                    userViewModel.addCreatedEvent(uid, eventId)
                }
                imageViewModel.resetState()
                onExit()
            },
            onError = { error ->
                Log.e("CreateEvent", "Failed to save event: ${error?.message}")
                imageViewModel.resetState()
                onExit()
            }
        )
    }

    if (eventFormViewModel.imageUri != null) {
        // save event AFTER image has been uploaded
        val uri: Uri = eventFormViewModel.imageUri!!
        imageViewModel.uploadImage(
            uri, context, ImageStoragePath.EventImage, {
                eventFormViewModel.updateImageUrl(it)
                saveEvent()
            }
        )
    } else {
        saveEvent()
    }
}
