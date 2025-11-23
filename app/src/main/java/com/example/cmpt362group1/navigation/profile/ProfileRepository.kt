package com.example.cmpt362group1.navigation.profile

import android.util.Log
import com.example.cmpt362group1.database.Event
import com.example.cmpt362group1.database.EventRepository
import com.example.cmpt362group1.database.EventRepositoryImpl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface ProfileRepository {
    fun getUserEvents(eventIds: List<String>): Flow<List<Event>>
}

class FirestoreProfileRepository(
    private val eventRepository: EventRepository = EventRepositoryImpl()
) : ProfileRepository {

    override fun getUserEvents(eventIds: List<String>): Flow<List<Event>> {
        Log.d("ProfileRepository", "Fetching events for IDs: $eventIds")

        return eventRepository
            .getAllEvents()
            .map { events ->
                events
                    .filterNotNull()
                    .filter { event -> eventIds.contains(event.id) }
                    .also { filtered ->
                        Log.d("ProfileRepository", "Found ${filtered.size} events")
                    }
            }
    }
}
