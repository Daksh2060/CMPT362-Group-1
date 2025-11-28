package com.example.cmpt362group1.event

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cmpt362group1.LoadingScreen
import com.example.cmpt362group1.Route
import com.example.cmpt362group1.auth.AuthViewModel
import com.example.cmpt362group1.database.EventViewModel
import com.example.cmpt362group1.database.ImageStoragePath
import com.example.cmpt362group1.database.ImageViewModel
import com.example.cmpt362group1.database.UserViewModel

@Composable
fun CreateEvent(
    onExit: () -> Unit,
    authViewModel: AuthViewModel = viewModel(),
) {
    val uid = authViewModel.getUserId()!!

    EventFormFlow(
        isEditMode = false,
        eventId = null,
        uid = uid,
        onExit = onExit,
    )
}

@Composable
fun EditEvent(
    eventId: String,
    onExit: () -> Unit,
    authViewModel: AuthViewModel = viewModel(),
) {
    val uid = authViewModel.getUserId() ?: return

    EventFormFlow(
        isEditMode = true,
        eventId = eventId,
        uid = uid,
        onExit = onExit,
    )
}

@Composable
private fun EventFormFlow(
    isEditMode: Boolean,
    eventId: String?,
    uid: String,
    onExit: () -> Unit,
    eventViewModel: EventViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
) {
    val context = LocalContext.current
    val eventFormViewModel: EventFormViewModel = viewModel()
    val imageViewModel: ImageViewModel = viewModel()
    val navController: NavHostController = rememberNavController()

    // For edit mode: load existing event
    if (isEditMode && eventId != null) {
        val eventState by eventViewModel.eventState.collectAsState()
        var initialized by remember { mutableStateOf(false) }

        LaunchedEffect(eventId) {
            eventViewModel.loadEvent(eventId)
        }

        when (val state = eventState) {
            is EventViewModel.EventUiState.Idle,
            is EventViewModel.EventUiState.Loading -> {
                LoadingScreen("Loading Event", "Please wait...")
                return
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
                return
            }

            is EventViewModel.EventUiState.Success -> {
                if (!initialized) {
                    eventFormViewModel.loadFromEvent(state.event)
                    initialized = true
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Route.CreateEventDetail.route
    ) {
        composable(Route.CreateEventDetail.route) {
            CreateEventDetails(
                isEditMode = isEditMode, // images are non-editable
                eventFormViewModel = eventFormViewModel,
                onExit = onExit,
                onContinue = {
                    navController.navigate(Route.CreateEventLocation.route)
                },
            )
        }

        composable(Route.CreateEventLocation.route) {
            CreateEventLocation(
                onBack = {
                    navController.popBackStack()
                },
                onConfirm = { lat, lng ->
                    eventFormViewModel.updateCoordinates(lat, lng)
                    if (!isEditMode) {
                        eventFormViewModel.setUserID(uid)
                    }

                    processEventSubmission(
                        isEditMode = isEditMode,
                        navController = navController,
                        eventFormViewModel = eventFormViewModel,
                        imageViewModel = imageViewModel,
                        eventViewModel = eventViewModel,
                        userViewModel = userViewModel,
                        authViewModel = authViewModel,
                        context = context,
                        onExit = onExit
                    )
                },
                eventFormViewModel = eventFormViewModel
            )
        }

        composable(Route.Loading.route) {
            LoadingScreen(
                if (isEditMode) "Updating Event" else "Creating Event",
                "It will be ready soon!"
            )
        }
    }
}

private fun processEventSubmission(
    isEditMode: Boolean,
    navController: NavHostController,
    eventFormViewModel: EventFormViewModel,
    imageViewModel: ImageViewModel,
    eventViewModel: EventViewModel,
    userViewModel: UserViewModel,
    authViewModel: AuthViewModel,
    context: Context,
    onExit: () -> Unit
) {
    fun saveOrUpdateEvent() {
        val event = eventFormViewModel.formInput

        if (isEditMode) {
            // Update existing event
            val eventId = event.id
            if (eventId.isBlank()) {
                Log.e("EventSubmission", "Missing event id when updating event")
                onExit()
                return
            }

            eventViewModel.updateEvent(
                eventId = eventId,
                updatedEvent = event,
                onSuccess = {
                    imageViewModel.resetState()
                    Toast.makeText(
                        context,
                        "Event Successfully Updated!",
                        Toast.LENGTH_LONG
                    ).show()
                    onExit()
                },
                onError = { error ->
                    Log.e("EventSubmission", "Failed to update event", error)
                    imageViewModel.resetState()
                    onExit()
                }
            )
        } else {
            // Create new event
            eventViewModel.saveEvent(
                event,
                onSuccess = { eventId ->
                    val uid = authViewModel.getUserId()
                    if (uid != null && userViewModel != null) {
                        userViewModel.addJoinedEvent(uid, eventId)
                        userViewModel.addCreatedEvent(uid, eventId)
                    }
                    imageViewModel.resetState()
                    eventFormViewModel.resetForm()
                    Toast.makeText(
                        context,
                        "Event Successfully Created!",
                        Toast.LENGTH_LONG
                    ).show()
                    onExit()
                },
                onError = { error ->
                    Log.e("EventSubmission", "Failed to save event: ${error?.message}")
                    imageViewModel.resetState()
                    onExit()
                }
            )
        }
    }

    // Handle image uploads
    val imageUris = eventFormViewModel.imageUris

    if (imageUris.isNotEmpty()) {
        // Navigate to loading screen
        navController.navigate(Route.Loading.route)

        Log.d("EventSubmission", "Uploading ${imageUris.size} images!")

        imageViewModel.uploadImages(
            imageUris,
            context,
            ImageStoragePath.EventImage,
            onSuccess = { result ->
                val url = result.uploadedUrl!! // legacy
                val urls = result.uploadedUrls
                eventFormViewModel.updateImageUrl(url) // legacy
                eventFormViewModel.updateImageUrls(urls)

                Log.d("EventSubmission", "Image uploads success! URLS: ${result.uploadedUrls}")
                saveOrUpdateEvent()
            },
            onError = { error ->
                Log.e("EventSubmission", error)
                Toast.makeText(
                    context,
                    "There was a problem uploading images. Please try again.",
                    Toast.LENGTH_LONG
                ).show()
                onExit()
            }
        )
    } else {
        Log.d("EventSubmission", "No images to upload, saving event directly!")
        saveOrUpdateEvent()
    }
}