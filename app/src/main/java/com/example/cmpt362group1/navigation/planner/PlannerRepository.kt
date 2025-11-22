package com.example.cmpt362group1.navigation.planner

import android.util.Log
import com.example.cmpt362group1.database.Event
import com.example.cmpt362group1.database.EventRepository
import com.example.cmpt362group1.database.EventRepositoryImpl
import com.example.cmpt362group1.database.UserRepository
import com.example.cmpt362group1.database.UserRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

interface PlannerRepository {
    fun streamEvents(query: String?): Flow<List<Event>>
}

class FirestorePlannerRepository(
    private val eventRepository: EventRepository = EventRepositoryImpl(),
    private val userRepository: UserRepository = UserRepositoryImpl(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
) : PlannerRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun streamEvents(query: String?): Flow<List<Event>> {
        val uid = auth.currentUser?.uid
        Log.d("PlannerDebug", "streamEvents() uid = $uid")

        if (uid == null) {
            return flowOf(emptyList())
        }

        return userRepository
            .getUser(uid)
            .flatMapLatest { user ->
                val joinedIds = user?.eventsJoined ?: emptyList<String>()
                Log.d(
                    "PlannerDebug",
                    "User ${user?.id} joinedIds = $joinedIds"
                )

                eventRepository
                    .getAllEvents()
                    .map { events ->
                        val allIds = events.map { it?.id }
                        val joinedEvents = events
                            .filterNotNull()
                            .filter { event -> joinedIds.contains(event.id) }

                        Log.d(
                            "PlannerDebug",
                            "All event ids = $allIds, joinedEvents = ${joinedEvents.map { it.id }}"
                        )

                        if (query.isNullOrBlank()) {
                            joinedEvents
                        } else {
                            joinedEvents.filter { e ->
                                e.title.contains(query, ignoreCase = true) ||
                                        e.location.contains(query, ignoreCase = true)
                            }
                        }
                    }
            }
    }
}

class FakePlannerRepository : PlannerRepository {
    private val state = MutableStateFlow(emptyList<Event>())

    override fun streamEvents(query: String?): Flow<List<Event>> =
        state.map { list ->
            if (query.isNullOrBlank()) list
            else list.filter { e ->
                e.title.contains(query, ignoreCase = true) ||
                        e.location.contains(query, ignoreCase = true)
            }
        }
}
