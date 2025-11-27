package com.example.cmpt362group1.event

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cmpt362group1.Route
import com.example.cmpt362group1.auth.AuthViewModel
import com.example.cmpt362group1.database.EventViewModel
import com.example.cmpt362group1.database.ImageStoragePath
import com.example.cmpt362group1.database.ImageViewModel
import com.example.cmpt362group1.database.UserViewModel
import com.example.cmpt362group1.database.OperationUiState
import kotlinx.coroutines.launch

@Composable
fun CreateEvent(
    onExit: () -> Unit,
    eventViewModel: EventViewModel,
    userViewModel: UserViewModel,
    authViewModel: AuthViewModel, // uid
) {
    val uid = authViewModel.getUserId()!!

    val context = LocalContext.current

    // Used to reset VM data
    val eventFormViewModel: EventFormViewModel = viewModel()
    val imageViewModel: ImageViewModel = viewModel()

    // upload event to server - link image
    fun processEventUpload() {
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
                    eventFormViewModel.resetForm()

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

    val createEventNavController: NavHostController = rememberNavController()

    NavHost(
        navController = createEventNavController,
        startDestination = Route.CreateEventDetail.route
    ) {
        composable(Route.CreateEventDetail.route) {
            CreateEventDetails(
                onExit = onExit,
                onContinue = {
                    createEventNavController.navigate(Route.CreateEventLocation.route)
                },
                eventFormViewModel = eventFormViewModel
            )
        }

        composable(Route.CreateEventLocation.route) {
            CreateEventLocation(
                onBack = {
                    createEventNavController.popBackStack()
                },
                onConfirm = { lat, lng ->
                    eventFormViewModel.updateCoordinates(lat, lng)
                    eventFormViewModel.setUserID(uid)
                    processEventUpload()
                },
                eventFormViewModel = eventFormViewModel
            )
        }
    }
}


@Composable
fun EditEvent(
    eventId: String,
    onExit: () -> Unit,
    eventViewModel: EventViewModel,
    userViewModel: UserViewModel,
    authViewModel: AuthViewModel,
) {
    val uid = authViewModel.getUserId() ?: return

    val STATE_FORM = 0
    val STATE_LOCATION = 1

    val context = LocalContext.current

    val eventFormViewModel: EventFormViewModel = viewModel()
    val imageViewModel: ImageViewModel = viewModel()

    val eventState by eventViewModel.eventState.collectAsState()
    var initialized by remember { mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(eventId) {
        eventViewModel.loadEvent(eventId)
    }

    when (val state = eventState) {
        is EventViewModel.EventUiState.Idle,
        is EventViewModel.EventUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is EventViewModel.EventUiState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        is EventViewModel.EventUiState.Success -> {
            if (!initialized) {
                eventFormViewModel.loadFromEvent(state.event)
                initialized = true
            }

            var step by remember { mutableStateOf(STATE_FORM) }

            when (step) {
                STATE_FORM -> {
                    CreateEventDetails(
                        onExit = onExit,
                        onContinue = { step = STATE_LOCATION },
                        eventFormViewModel = eventFormViewModel
                    )
                }

                STATE_LOCATION -> {
                    CreateEventLocation(
                        onBack = { step = STATE_FORM },
                        onConfirm = { lat, lng ->
                            eventFormViewModel.updateCoordinates(lat, lng)
                            processEventUpdate(
                                eventViewModel = eventViewModel,
                                eventFormViewModel = eventFormViewModel,
                                imageViewModel = imageViewModel,
                                context = context,
                                onExit = onExit
                            )
                        },
                        eventFormViewModel = eventFormViewModel
                    )
                }
            }
        }
    }
}

fun processEventUpdate(
    eventViewModel: EventViewModel,
    eventFormViewModel: EventFormViewModel,
    imageViewModel: ImageViewModel,
    context: Context,
    onExit: () -> Unit
) {
    fun updateEvent() {
        val event = eventFormViewModel.formInput
        val eventId = event.id

        if (eventId.isBlank()) {
            Log.e("EditEvent", "Missing event id when updating event")
            onExit()
            return
        }

        eventViewModel.updateEvent(
            eventId = eventId,
            updatedEvent = event,
            onSuccess = {
                imageViewModel.resetState()
                onExit()
            },
            onError = { error ->
                Log.e("EditEvent", "Failed to update event", error)
                imageViewModel.resetState()
                onExit()
            }
        )
    }

    if (eventFormViewModel.imageUri != null) {
        val uri: Uri = eventFormViewModel.imageUri!!
        imageViewModel.uploadImage(
            uri,
            context,
            ImageStoragePath.EventImage,
            { url ->
                eventFormViewModel.updateImageUrl(url)
                updateEvent()
            }
        )
    } else {
        updateEvent()
    }
}
