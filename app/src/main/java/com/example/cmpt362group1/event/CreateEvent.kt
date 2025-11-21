package com.example.cmpt362group1.event

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cmpt362group1.auth.AuthViewModel
import com.example.cmpt362group1.database.EventViewModel
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

    // Used to reset VM data
    val eventFormViewModel: EventFormViewModel = viewModel()

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

                    val event = eventFormViewModel.formInput

                    eventViewModel.saveEvent(
                        event,
                        onSuccess = { eventId ->
                            val uid = authViewModel.getUserId()
                            if (uid != null) {
                                userViewModel.addJoinedEvent(uid, eventId)
                                userViewModel.addCreatedEvent(uid, eventId)
                            }
                            onExit()
                        },
                        onError = { error ->
                            Log.e("CreateEvent", "Failed to save event: ${error?.message}")
                            onExit()
                        }
                    )
                },
                eventFormViewModel
            )
        }
    }
}
