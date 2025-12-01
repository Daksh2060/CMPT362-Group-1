package com.example.cmpt362group1.event

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Close
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
import com.example.cmpt362group1.navigation.explore.endDateTime
import com.example.cmpt362group1.navigation.explore.startDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventDetails(
    isEditMode: Boolean,
    eventFormViewModel: EventFormViewModel = viewModel(),
    onExit: () -> Unit,
    onContinue: () -> Unit,
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
                isEditMode,
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
    isEditMode: Boolean,
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

    fun validDate(): Boolean {
        return eventFormViewModel.formInput.startDateTime() < eventFormViewModel.formInput.endDateTime()
    }

    fun handleContinueOnClick() {
        if (validForm() && validDate()) {
            onContinue()
        } else {
            val message = if (!validForm()) {
                "Please fill in all required fields"
            } else {
                "Start date must be before end date"
            }

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
            if (!isEditMode) {
                ImageSelector(eventFormViewModel)
            }
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
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Continue")
            }
        }
    }
}

@Composable
fun ImageSelector(eventFormViewModel: EventFormViewModel) {
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = EventFormViewModel.MAX_IMAGE_UPLOAD)
    ) { uris: List<Uri> ->
        eventFormViewModel.updateImageUris(uris)
    }

    val context = LocalContext.current

    fun launchPhotoPicker() {
        if (eventFormViewModel.imageUris.size == EventFormViewModel.MAX_IMAGE_UPLOAD) {
            Toast.makeText(context, "You already have three images selected!", 2000)
            return
        }

        photoPicker.launch(
            PickVisualMediaRequest(
                ActivityResultContracts.PickVisualMedia.ImageOnly
            )
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // add images placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
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
                    launchPhotoPicker()
                },
            contentAlignment = Alignment.Center
        ) {
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
                    text = if (eventFormViewModel.imageUris.isEmpty())
                        "Add Event Photos"
                    else
                        "Add More Photos",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Tap to select up to ${EventFormViewModel.MAX_IMAGE_UPLOAD} images",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // Selected Images Grid (non-scrollable)
        if (eventFormViewModel.imageUris.isNotEmpty()) {
            ImageGrid(
                imageUris = eventFormViewModel.imageUris,
                onRemoveImage = { index -> eventFormViewModel.removeImageUri(index) }
            )
        }
    }
}

@Composable
fun ImageGrid(
    imageUris: List<Uri>,
    onRemoveImage: (Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        imageUris.chunked(3).forEach { rowImages ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowImages.forEachIndexed { _, uri ->
                    val actualIndex = imageUris.indexOf(uri)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        AsyncImage(
                            modifier = Modifier.fillMaxSize(),
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(uri)
                                .crossfade(enable = true)
                                .build(),
                            contentDescription = "Event Image ${actualIndex + 1}",
                            contentScale = ContentScale.Crop,
                        )

                        // Remove button
                        IconButton(
                            onClick = { onRemoveImage(actualIndex) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(10.dp)
                                .background(
                                    color = Color.Black.copy(alpha = 0.6f),
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove image",
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
                // Fill empty spaces in the last row
                repeat(3 - rowImages.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}