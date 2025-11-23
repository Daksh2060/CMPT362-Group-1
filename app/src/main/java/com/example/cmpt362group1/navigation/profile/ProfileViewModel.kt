package com.example.cmpt362group1.navigation.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cmpt362group1.database.Event
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val repository: ProfileRepository = FirestoreProfileRepository()
) : ViewModel() {

    private val _eventsState = MutableStateFlow<ProfileEventsState>(ProfileEventsState.Idle)
    val eventsState: StateFlow<ProfileEventsState> = _eventsState.asStateFlow()

    fun loadUserEvents(eventIds: List<String>) {
        if (eventIds.isEmpty()) {
            _eventsState.value = ProfileEventsState.Success(emptyList())
            return
        }

        viewModelScope.launch {
            _eventsState.value = ProfileEventsState.Loading

            repository.getUserEvents(eventIds)
                .catch { e ->
                    Log.e("ProfileViewModel", "Error loading user events", e)
                    _eventsState.value = ProfileEventsState.Error(e.message ?: "Unknown error")
                }
                .collect { events ->
                    _eventsState.value = ProfileEventsState.Success(events)
                    Log.d("ProfileViewModel", "Loaded ${events.size} events")
                }
        }
    }
}

sealed class ProfileEventsState {
    data object Idle : ProfileEventsState()
    data object Loading : ProfileEventsState()
    data class Success(val events: List<Event>) : ProfileEventsState()
    data class Error(val message: String) : ProfileEventsState()
}
