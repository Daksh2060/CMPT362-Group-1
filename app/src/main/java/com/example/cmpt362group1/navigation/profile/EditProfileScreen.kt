package com.example.cmpt362group1.navigation.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(navController: NavHostController) {

    // state variables for fields
    var profileIntro by remember {
        mutableStateOf("Hello! I'm a CS student passionate about mobile development. Love attending tech events and meeting new people!")
    }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var faculty by remember { mutableStateOf("") }
    var occupancy by remember { mutableStateOf("") }
    var enrollmentYear by remember { mutableStateOf("") }
    var selectedHobby by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var expandedHobby by remember { mutableStateOf(false) }

    val hobbies = listOf("Reading", "Sports", "Gaming", "Music", "Cooking", "Photography", "Traveling", "Art")

    val datePickerState = rememberDatePickerState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // profile image in circular format
        Icon(
            imageVector = Icons.Outlined.Person,
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // change profile image button
        Button(
            onClick = { /* TODO */ }
        ) {
            Text("Change Profile Image")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // introduction
        OutlinedTextField(
            value = profileIntro,
            onValueChange = { profileIntro = it },
            label = { Text("Profile Introduction") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            maxLines = 5,
            placeholder = { Text("Tell us about yourself!") }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // First, Last name
        OutlinedTextField(
            value = firstName,
            onValueChange = { firstName = it },
            label = { Text("First Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = lastName,
            onValueChange = { lastName = it },
            label = { Text("Last Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // birth date with calendar
        OutlinedTextField(
            value = birthDate,
            onValueChange = { },
            label = { Text("Birth Date") },
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                    Text("📅")
                }
            }
        )

        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                val date = Date(millis)
                                val formatter = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
                                birthDate = formatter.format(date)
                            }
                            showDatePicker = false
                        }
                    ) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // faculty
        OutlinedTextField(
            value = faculty,
            onValueChange = { faculty = it },
            label = { Text("Faculty") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // occupancy
        OutlinedTextField(
            value = occupancy,
            onValueChange = { occupancy = it },
            label = { Text("Occupancy") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // enrollment year
        OutlinedTextField(
            value = enrollmentYear,
            onValueChange = { enrollmentYear = it },
            label = { Text("Enrollment Year") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // hobby/interest dropdown (Spinner with some samples)
        ExposedDropdownMenuBox(
            expanded = expandedHobby,
            onExpandedChange = { expandedHobby = !expandedHobby }
        ) {
            OutlinedTextField(
                value = selectedHobby,
                onValueChange = {},
                readOnly = true,
                label = { Text("Hobby/Interest") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedHobby) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expandedHobby,
                onDismissRequest = { expandedHobby = false }
            ) {
                hobbies.forEach { hobby ->
                    DropdownMenuItem(
                        text = { Text(hobby) },
                        onClick = {
                            selectedHobby = hobby
                            expandedHobby = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // save and cancel button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // cancel Button
            OutlinedButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel")
            }

            // save Button
            Button(
                onClick = {
                    // TODO: Save profile data
                    navController.popBackStack()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Save")
            }
        }
    }
}