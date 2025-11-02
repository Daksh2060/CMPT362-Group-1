package com.example.cmpt362group1.event

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEvent(
    onExit: () -> Unit,
    viewModel: EventViewModel = viewModel(),
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
            viewModel,
            viewModel.formData,
            paddingValues
        )
    }
}

@Composable
fun EventForm(
    entryVM: EventViewModel,
    entry: Event,
    paddingValues: PaddingValues,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            FormTextField(
                value = entry.title,
                onValueChange = entryVM::updateTitle,
                label = "Event Title"
            )
        }

        item {
            FormTextField(
                value = entry.location,
                onValueChange = entryVM::updateLocation,
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
                    onDateSelected = entryVM::updateStartDate,
                    modifier = Modifier.weight(1.0f)
                )

                DatePickerField(
                    label = "End Date",
                    selectedDate = entry.endDate,
                    onDateSelected = entryVM::updateEndDate,
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
                    onTimeSelected = entryVM::updateStartTime,
                    modifier = Modifier.weight(1.0f)
                )
                TimePickerField(
                    label = "End Time",
                    selectedTime = entry.endTime,
                    onTimeSelected = entryVM::updateEndTime,
                    modifier = Modifier.weight(1.0f)
                )
            }
        }

        item {
            FormTextField(
                value = entry.description,
                onValueChange = entryVM::updateDescription,
                label = "Description",
                isSingleLine = false
            )
        }

        item {
            FormTextField(
                value = entry.dressCode,
                onValueChange = entryVM::updateDressCode,
                label = "Dress Code",
                isOptional = true
            )
        }

        item {
            Button(
                onClick =  entryVM::saveEvent,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Continue")
            }
        }
    }
}