package com.example.cmpt362group1.navigation.profile

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.cmpt362group1.database.User
import com.example.cmpt362group1.database.UserViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavHostController,
    userViewModel: UserViewModel,
    userProfile: User
) {

    fun formatEnrollmentYearToString(year: Int?): String = year?.toString() ?: ""

    fun formatDateToString(date: Date?): String {
        return date?.let {
            Log.d("INFO EditProfileScreen", "Format date")
            SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(it)
        } ?: ""
    }

    // state variables for fields
    var intro by remember { mutableStateOf(userProfile.description) }
    var firstName by remember { mutableStateOf(userProfile.firstName) }
    var lastName by remember { mutableStateOf(userProfile.lastName) }
    var birthDate by remember { mutableStateOf(formatDateToString(userProfile.birthdate)) }
    var faculty by remember { mutableStateOf(userProfile.faculty) }
    var occupancy by remember { mutableStateOf(userProfile.occupancy) }
    var enrollmentYear by remember { mutableStateOf(formatEnrollmentYearToString(userProfile.enrollmentYear)) }
    var selectedHobby by remember { mutableStateOf(userProfile.hobby) }
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
            value = intro,
            onValueChange = { intro = it },
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
            onValueChange = { birthDate = it },
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
            onValueChange = { it ->
                enrollmentYear = it.filter { it.isDigit() }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
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
                    val updates = mutableMapOf<String, Any>()
                    updates["description"] = intro
                    updates["firstName"] = firstName
                    updates["lastName"] = lastName
                    updates["faculty"] = faculty
                    updates["occupancy"] = occupancy
                    updates["hobby"] = selectedHobby
                    enrollmentYear.toIntOrNull()?.let { updates["enrollmentYear"] = it }

                    if (birthDate.isNotBlank()) {
                        SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
                            .parse(birthDate)?.let {
                                updates["birthdate"] = it
                            }
                    }

                    userViewModel.updateUser(userProfile.id, updates)
                    navController.popBackStack()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Save")
            }
        }
    }
}