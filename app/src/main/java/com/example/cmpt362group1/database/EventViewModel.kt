package com.example.cmpt362group1.database

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class EventViewModel(
    private val repository: EventRepository = EventRepositoryImpl(),
    private val context: Context
) : ViewModel() {
    private val _eventsState = MutableStateFlow<EventsUiState>(EventsUiState.Loading)
    val eventsState: StateFlow<EventsUiState> = _eventsState.asStateFlow()

    private val _eventState = MutableStateFlow<EventUiState>(EventUiState.Idle)
    val eventState: StateFlow<EventUiState> = _eventState.asStateFlow()

    private val _operationState = MutableStateFlow<OperationUiState>(OperationUiState.Idle)
    val operationState: StateFlow<OperationUiState> = _operationState.asStateFlow()

    init {
        loadAllEvents()
    }

    fun loadAllEvents() {
        viewModelScope.launch {
            _eventsState.value = EventsUiState.Loading
            Log.d("INFO", "LOADING EVENTS")
            repository.getAllEvents()
                .catch { e ->
                    Log.d("INFO","Failed to load events", e)
                    _eventsState.value =
                        EventsUiState.Error(e.message ?: "Unknown error occurred")
                }
                .collect { events ->
                    Log.d("INFO","Events loaded ${events}")
                    _eventsState.value =
                        EventsUiState.Success(events.filterNotNull())
                }
        }
    }

    fun loadEvent(eventId: String) {
        viewModelScope.launch {
            _eventState.value = EventUiState.Loading

            repository.getEvent(eventId)
                .catch { e ->
                    Log.e("EventViewModel", "Error loading event", e)
                    _eventState.value = EventUiState.Error(e.message ?: "Unknown error occurred")
                }
                .collect { event ->
                    _eventState.value = if (event != null) {
                        EventUiState.Success(event)
                    } else {
                        EventUiState.Error("Event not found")
                    }
                }
        }
    }

    fun saveEvent(event: Event) {
        viewModelScope.launch {
            _operationState.value = OperationUiState.Loading

            Log.d("EventViewModel", "Saving event: $event")

            val result = repository.addEvent(event)

            if (result.isSuccess) {
                val eventId = result.getOrNull()
                _operationState.value = OperationUiState.Success("Event saved successfully")
                Log.d("EventViewModel", "Event saved with ID: $eventId")

                updateWidget(context)
            } else {
                val error = result.exceptionOrNull()
                _operationState.value = OperationUiState.Error(
                    error?.message ?: "Failed to save event"
                )
            }
        }
    }

    fun addEvent(event: Event) {
        viewModelScope.launch {
            _operationState.value = OperationUiState.Loading

            val result = repository.addEvent(event)

            _operationState.value = if (result.isSuccess) {
                OperationUiState.Success("Event added successfully")
            } else {
                OperationUiState.Error(
                    result.exceptionOrNull()?.message ?: "Failed to add event"
                )
            }
        }
    }

    fun clearAllEvents() {
        viewModelScope.launch {
            _operationState.value = OperationUiState.Loading

            val result = repository.clearEvents()

            if (result.isSuccess) {
                _operationState.value = OperationUiState.Success("All events cleared")
                updateWidget(context)
            } else {
                val error = result.exceptionOrNull()
                _operationState.value = OperationUiState.Error(
                    error?.message ?: "Failed to clear events"
                )
            }
        }
    }

    private fun updateWidget(context: Context) {
        val intent = Intent("com.cmpt362group1.WIDGET_UPDATE")
        context.sendBroadcast(intent)
    }

    sealed class EventsUiState {
        data object Loading : EventsUiState()
        data class Success(val events: List<Event>) : EventsUiState()
        data class Error(val message: String) : EventsUiState()
    }

    sealed class EventUiState {
        data object Idle : EventUiState()
        data object Loading : EventUiState()
        data class Success(val event: Event) : EventUiState()
        data class Error(val message: String) : EventUiState()
    }

    sealed class OperationUiState {
        data object Idle : OperationUiState()
        data object Loading : OperationUiState()
        data class Success(val message: String) : OperationUiState()
        data class Error(val message: String) : OperationUiState()
    }
}