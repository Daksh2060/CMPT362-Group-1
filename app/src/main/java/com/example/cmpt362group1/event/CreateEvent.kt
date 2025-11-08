package com.example.cmpt362group1.event

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cmpt362group1.database.EventViewModel

@Composable
fun CreateEvent(
    onExit: () -> Unit,
    eventViewModel: EventViewModel
) {
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
                    eventViewModel.saveEvent(eventFormViewModel.formInput)
                    onExit()
                },
                eventFormViewModel
            )
        }
    }
}
