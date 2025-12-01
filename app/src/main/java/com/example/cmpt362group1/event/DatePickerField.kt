package com.example.cmpt362group1.event

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    label: String,
    selectedDate: String,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormatter = remember {
        DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.getDefault())
            .withZone(ZoneId.systemDefault())
    }

    OutlinedTextField(
        value = selectedDate,
        label = { Text(label) }, //NOP
        singleLine = true,
        readOnly = true,
        onValueChange = {},
        modifier = modifier
            .clickable { showDatePicker = true }
            .fillMaxWidth(),
        enabled = false,
    )

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis >= System.currentTimeMillis() - 86400000
            }
        }
    )

    val onConfirm: () -> Unit = {
        datePickerState.selectedDateMillis?.let { selectedMillis ->
            val localDate = Instant.ofEpochMilli(selectedMillis)
                .atZone(ZoneId.of("UTC"))
                .toLocalDate()
            onDateSelected(dateFormatter.format(localDate))
        }
        showDatePicker = false
    }

    val onDismiss: () -> Unit = { showDatePicker = false }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(
                    onClick = onConfirm,
                    enabled = datePickerState.selectedDateMillis != null
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onDismiss) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}