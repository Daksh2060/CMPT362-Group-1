package com.example.cmpt362group1.database

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class EventViewModel(
    private val repository: EventRepository = EventRepositoryImpl(),
    private val context: Context
) : ViewModel() {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private var commentsListener: ListenerRegistration? = null
    private var participantsListener: ListenerRegistration? = null

    private val _eventsState = MutableStateFlow<EventsUiState>(EventsUiState.Loading)
    val eventsState: StateFlow<EventsUiState> = _eventsState.asStateFlow()

    private val _eventState = MutableStateFlow<EventUiState>(EventUiState.Idle)
    val eventState: StateFlow<EventUiState> = _eventState.asStateFlow()

    private val _operationState = MutableStateFlow<OperationUiState>(OperationUiState.Idle)
    val operationState: StateFlow<OperationUiState> = _operationState.asStateFlow()

    private val _commentsState =
        MutableStateFlow<CommentsUiState>(CommentsUiState.Loading)
    val commentsState: StateFlow<CommentsUiState> = _commentsState.asStateFlow()

    private val _participantsCount = MutableStateFlow(0)
    val participantsCount: StateFlow<Int> = _participantsCount.asStateFlow()

    init {
        loadAllEvents()
    }

    fun loadAllEvents() {
        viewModelScope.launch {
            _eventsState.value = EventsUiState.Loading
            Log.d("INFO", "LOADING EVENTS")
            repository.getAllEvents()
                .catch { e ->
                    Log.d("INFO", "Failed to load events", e)
                    _eventsState.value =
                        EventsUiState.Error(e.message ?: "Unknown error occurred")
                }
                .collect { events ->
                    Log.d("INFO", "Events loaded $events")
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

    fun saveEvent(
        event: Event,
        onSuccess: (String) -> Unit = {},
        onError: (Throwable?) -> Unit = {}
    ) {
        viewModelScope.launch {
            _operationState.value = OperationUiState.Loading

            Log.d("EventViewModel", "Saving event: $event")

            val result = repository.addEvent(event)

            if (result.isSuccess) {
                val eventId = result.getOrNull()!!

                onSuccess(eventId)
                _operationState.value = OperationUiState.Success("Event saved successfully")
                Log.d("EventViewModel", "Event saved with ID: $eventId")

                updateWidget(context)

                if (eventId != null) {
                    onSuccess(eventId)
                }
            } else {
                val error = result.exceptionOrNull()
                _operationState.value = OperationUiState.Error(
                    error?.message ?: "Failed to save event"
                )

                onError(error)
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

    fun startComments(eventId: String) {
        _commentsState.value = CommentsUiState.Loading

        commentsListener?.remove()
        commentsListener = db.collection("events")
            .document(eventId)
            .collection("comments")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("EventViewModel", "Error listening comments", error)
                    _commentsState.value =
                        CommentsUiState.Error(error.message ?: "Failed to load comments")
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    _commentsState.value = CommentsUiState.Success(emptyList())
                    return@addSnapshotListener
                }

                val comments = snapshot.toObjects(Comment::class.java)
                _commentsState.value = CommentsUiState.Success(comments)
            }
    }

    fun postComment(
        eventId: String,
        userId: String,
        userName: String,
        text: String,
        parentId: String? = null
    ) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return

        val comment = Comment(
            eventId = eventId,
            userId = userId,
            userName = userName,
            text = trimmed,
            parentId = parentId
        )

        db.collection("events")
            .document(eventId)
            .collection("comments")
            .add(comment)
            .addOnSuccessListener {
                Log.d("EventViewModel", "Comment added")
            }
            .addOnFailureListener { e ->
                Log.e("EventViewModel", "Failed to add comment", e)
            }
    }

    fun startParticipants(eventId: String) {
        participantsListener?.remove()
        participantsListener = db.collection("users")
            .whereArrayContains("eventsJoined", eventId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("EventViewModel", "Error listening participants", error)
                    _participantsCount.value = 0
                    return@addSnapshotListener
                }

                _participantsCount.value = snapshot?.size() ?: 0
            }
    }

    override fun onCleared() {
        super.onCleared()
        commentsListener?.remove()
        participantsListener?.remove()
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

    sealed class CommentsUiState {
        data object Loading : CommentsUiState()
        data class Success(val comments: List<Comment>) : CommentsUiState()
        data class Error(val message: String) : CommentsUiState()
    }
}
