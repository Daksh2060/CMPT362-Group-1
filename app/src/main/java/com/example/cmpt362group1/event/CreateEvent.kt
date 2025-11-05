package com.example.cmpt362group1.event

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun CreateEvent(
    onExit: () -> Unit,
    viewModel: EventViewModel
) {
    val STATE_FORM = 0
    val STATE_LOCATION = 1

    // Used to reset VM data
    LaunchedEffect(Unit) {
        viewModel.resetForm()
    }

    var step by remember { mutableStateOf(STATE_FORM) }
    when (step) {
        STATE_FORM -> {
            CreateEventDetails(
                onExit = onExit,
                onContinue = {
                    step = STATE_LOCATION
                },
                viewModel
            )
        }

        STATE_LOCATION -> {
            CreateEventLocation(
                onBack = { step = STATE_FORM },
                onConfirm = { lat, lng ->
                    viewModel.updateCoordinates(lat, lng)
                    viewModel.saveEvent()
                    onExit()
                }, viewModel
            )
        }
    }
}
