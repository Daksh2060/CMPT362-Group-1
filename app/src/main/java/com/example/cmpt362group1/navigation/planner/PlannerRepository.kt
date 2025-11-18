package com.example.cmpt362group1.navigation.planner

import com.example.cmpt362group1.database.Event
import com.example.cmpt362group1.database.EventRepository
import com.example.cmpt362group1.database.EventRepositoryImpl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

interface PlannerRepository {
    fun streamEvents(query: String?): Flow<List<Event>>
}

class FirestorePlannerRepository(
    private val eventRepository: EventRepository = EventRepositoryImpl()
) : PlannerRepository {

    override fun streamEvents(query: String?): Flow<List<Event>> =
        eventRepository
            .getAllEvents()
            .map { events ->
                val list = events.filterNotNull()
                if (query.isNullOrBlank()) {
                    list
                } else {
                    list.filter { e ->
                        e.title.contains(query, ignoreCase = true) ||
                                e.location.contains(query, ignoreCase = true)
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
