package com.example.cmpt362group1.event

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.cmpt362group1.database.Event

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventDetails(
    onExit: () -> Unit,
    onContinue: () -> Unit,
    eventFormViewModel: EventFormViewModel = viewModel(),
) {
    val colors = lightColorScheme(
        background = Color.White,
        surface = Color.White,
        primary = Color.Black,
        onSurface = Color.Black,
        onSurfaceVariant = Color(0xFF666666),
        outline = Color(0xFFDDDDDD)
    )

    MaterialTheme(colorScheme = colors) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Create New Event",
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onExit) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
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
}

@Composable
fun EventForm(
    eventFormViewModel: EventFormViewModel,
    entry: Event,
    onContinue: () -> Unit,
    paddingValues: PaddingValues,
) {
    val context: Context = LocalContext.current

    fun validForm(): Boolean {
        return eventFormViewModel.formInput.title.isNotBlank() &&
                eventFormViewModel.formInput.location.isNotBlank() &&
                eventFormViewModel.formInput.startDate.isNotBlank() &&
                eventFormViewModel.formInput.endDate.isNotBlank() &&
                eventFormViewModel.formInput.startTime.isNotBlank() &&
                eventFormViewModel.formInput.endTime.isNotBlank() &&
                eventFormViewModel.formInput.description.isNotBlank()
    }

    fun handleContinueOnClick() {
        if (validForm()) {
            onContinue()
        } else {
            val message = "Please fill in all required fields"
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentPadding = PaddingValues(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            ImageSelector(eventFormViewModel)
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
                onClick = { handleContinueOnClick() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Continue")
            }
        }
    }
}

@Composable
fun ImageSelector(eventFormViewModel: EventFormViewModel) {
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        eventFormViewModel.updateImageUri(uri)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f),
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                    )
                )
            )
            .clickable {
                photoPicker.launch(
                    PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageOnly
                    )
                )
            },
        contentAlignment = Alignment.Center
    ) {
        if (eventFormViewModel.imageUri == null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AddCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    modifier = Modifier.size(64.dp)
                )
                Text(
                    text = "Add Event Photo",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Tap anywhere to upload",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        AsyncImage(
            modifier = Modifier.fillMaxSize(),
            model = ImageRequest.Builder(LocalContext.current)
                .data(eventFormViewModel.imageUri)
                .crossfade(enable = true)
                .build(),
            contentDescription = "Event Image",
            contentScale = ContentScale.Crop,
        )
    }
}