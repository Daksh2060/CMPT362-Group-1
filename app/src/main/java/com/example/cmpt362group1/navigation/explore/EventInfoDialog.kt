package com.example.cmpt362group1.navigation.explore

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.cmpt362group1.R
import com.example.cmpt362group1.database.Event
import com.example.cmpt362group1.navigation.explore.weather.WeatherHelper
import com.example.cmpt362group1.navigation.explore.weather.WeatherResult
import com.example.cmpt362group1.utils.Summarizer
import com.example.cmpt362group1.utils.SummaryResult
import com.example.cmpt362group1.utils.TextToSpeechManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun EventInfoDialog(
    event: Event,
    weatherData: WeatherResult?,
    weatherError: String?,
    onDismiss: () -> Unit,
    onEventPageClick: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = event.title,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp,
                        color = Color.Black,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    SummarizeAndSpeakButton(event)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    InfoRow(label = "Location", value = event.location)
                    InfoRow(label = "Date", value = "${event.startDate} - ${event.endDate}")
                    InfoRow(label = "Time", value = "${event.startTime} - ${event.endTime}")

                    if (event.description.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Description",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = Color(0xFF444444)
                        )
                        Text(
                            text = event.description,
                            fontSize = 15.sp,
                            color = Color.Black
                        )
                    }

                    if (event.dressCode.isNotEmpty()) {
                        InfoRow(label = "Dress Code", value = event.dressCode)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(
                        Modifier,
                        DividerDefaults.Thickness,
                        color = Color(0xFFE0E0E0)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Expected Weather",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    when {
                        weatherData != null -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "${weatherData.temperature.toInt()}°C",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp,
                                    color = Color.Black
                                )

                                val symbol = WeatherHelper.getWeatherSymbol(weatherData.condition.lowercase())

                                Text(
                                    text = symbol,
                                    fontSize = 24.sp
                                )

                                Text(
                                    text = weatherData.condition,
                                    fontSize = 16.sp,
                                    color = Color(0xFF555555)
                                )
                            }
                        }

                        weatherError != null -> {
                            Text(
                                text = "Weather not yet available.",
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Red
                            )
                            Text(
                                text = weatherError,
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                        }

                        else -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                                Text(
                                    text = "Loading weather...",
                                    fontSize = 13.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ElevatedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        ),
                        elevation = ButtonDefaults.elevatedButtonElevation(
                            defaultElevation = 6.dp,
                            pressedElevation = 12.dp,
                            focusedElevation = 8.dp,
                            hoveredElevation = 8.dp
                        )
                    ) {
                        Text(
                            text = "Close",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    ElevatedButton(
                        onClick = {
                            onEventPageClick(event.id)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        ),
                        elevation = ButtonDefaults.elevatedButtonElevation(
                            defaultElevation = 6.dp,
                            pressedElevation = 12.dp,
                            focusedElevation = 8.dp,
                            hoveredElevation = 8.dp
                        )
                    ) {
                        Text(
                            text = "Event Page",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SummarizeAndSpeakButton(
    event: Event,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isSpeaking by remember { mutableStateOf(false) }
    var summary by remember { mutableStateOf<String?>(null) }

    val ttsManager = remember {
        TextToSpeechManager(context) { initialized ->
            if (!initialized) {
                Log.e("TTS", "Failed to initialize")
            }
        }
    }

    val summarizer = remember { Summarizer() }

    val scope = rememberCoroutineScope()

    DisposableEffect(Unit) {
        onDispose {
            ttsManager.shutdown()
        }
    }

    IconButton(
        onClick = {
            if (isSpeaking) {
                ttsManager.stop()
                isSpeaking = false
            } else {
                scope.launch {
                    // Generate summary if not already done
                    if (summary == null) {
                        val textToSummarize = buildString {
                            append("Event: ${event.title}. ")
                            if (event.location.isNotBlank()) {
                                append("Location: ${event.location}. ")
                            }
                            if (event.startDate.isNotBlank()) {
                                append("Date: ${event.startDate}")
                                if (event.startTime.isNotBlank()) {
                                    append(" at ${event.startTime}")
                                }
                                append(". ")
                            }
                            if (event.description.isNotBlank()) {
                                append(event.description)
                            }
                        }

                        when (val result = summarizer.summarize(textToSummarize)) {
                            is SummaryResult.Success -> {
                                summary = result.summary
                                ttsManager.speak(result.summary)
                                isSpeaking = true

                                // Monitor speaking status
                                launch {
                                    while (ttsManager.isSpeaking()) {
                                        delay(100)
                                    }
                                    isSpeaking = false
                                }
                            }
                            else -> {
                                Log.e("Summarizer", "Failed to summarize")
                            }
                        }
                    } else {
                        // Summary already exists, just speak it
                        ttsManager.speak(summary!!)
                        isSpeaking = true

                        scope.launch {
                            while (ttsManager.isSpeaking()) {
                                delay(100)
                            }
                            isSpeaking = false
                        }
                    }
                }
            }
        },
        modifier = modifier
    ) {
        Icon(
            painter = painterResource(R.drawable.tts),
            modifier = Modifier.size(20.dp),
            contentDescription = if (isSpeaking) "Stop" else "Summarize & Speak",
            tint = Color.Black
        )
    }
}