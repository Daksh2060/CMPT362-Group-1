package com.example.cmpt362group1.event

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.cmpt362group1.database.Event
import com.example.cmpt362group1.database.EventViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventDetails(
    onExit: () -> Unit,
    onContinue: () -> Unit,
    eventFormViewModel: EventFormViewModel = viewModel(),
) {
    Scaffold (
        topBar = {
            TopAppBar(
                title = { Text("Create New Event") },
                navigationIcon = {
                    IconButton(onClick = onExit) {
                        Icon (
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
    ) { paddingValues ->
        EventForm(
            eventFormViewModel,
            eventFormViewModel.formInput,
            onContinue,
            paddingValues
        )
    }
}

@Composable
fun EventForm(
    eventFormViewModel: EventFormViewModel,
    entry: Event,
    onContinue: () -> Unit,
    paddingValues: PaddingValues,
) {

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? -> // cb when image select
        eventFormViewModel.updateImageUri(uri)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            AsyncImage(
                modifier = Modifier
                    .size(250.dp)
                    .background(Color.LightGray)
                    .clickable {
                        photoPicker.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    },
                model = ImageRequest.Builder(LocalContext.current)
                    .data(eventFormViewModel.imageUri)
                    .crossfade(enable = true)
                    .build(),
                contentDescription = "Avatar Image",
                contentScale = ContentScale.Crop,
            )
        }

        item {
            FormTextField(
                value = entry.title,
                onValueChange = eventFormViewModel::updateTitle,
                label = "Event Title"
            )
        }

        item {
            FormTextField(
                value = entry.location,
                onValueChange = eventFormViewModel::updateLocation,
                label = "Location"
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DatePickerField(
                    label = "Start Date",
                    selectedDate = entry.startDate,
                    onDateSelected = eventFormViewModel::updateStartDate,
                    modifier = Modifier.weight(1.0f)
                )

                DatePickerField(
                    label = "End Date",
                    selectedDate = entry.endDate,
                    onDateSelected = eventFormViewModel::updateEndDate,
                    modifier = Modifier.weight(1.0f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TimePickerField(
                    label = "Start Time",
                    selectedTime = entry.startTime,
                    onTimeSelected = eventFormViewModel::updateStartTime,
                    modifier = Modifier.weight(1.0f)
                )
                TimePickerField(
                    label = "End Time",
                    selectedTime = entry.endTime,
                    onTimeSelected = eventFormViewModel::updateEndTime,
                    modifier = Modifier.weight(1.0f)
                )
            }
        }

        item {
            FormTextField(
                value = entry.description,
                onValueChange = eventFormViewModel::updateDescription,
                label = "Description",
                isSingleLine = false
            )
        }

        item {
            FormTextField(
                value = entry.dressCode,
                onValueChange = eventFormViewModel::updateDressCode,
                label = "Dress Code",
                isOptional = true
            )
        }

        item {
            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Continue")
            }
        }
    }
}