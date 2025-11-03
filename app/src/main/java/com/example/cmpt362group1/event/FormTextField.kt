package com.example.cmpt362group1.event

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    isSingleLine: Boolean = true,
    isOptional: Boolean = false,
    readOnly: Boolean = false, // for date pickers
    onClick: (() -> Unit)? = null, // for date pickers
) {
    val fullLabel = if (isOptional) "$label - Optional" else label

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(fullLabel) },
        singleLine = isSingleLine,
        enabled = !readOnly || onClick != null,
        modifier = modifier
    )
}

