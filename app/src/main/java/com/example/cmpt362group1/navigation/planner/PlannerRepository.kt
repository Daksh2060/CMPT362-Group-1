package com.example.cmpt362group1.navigation.planner

import com.example.cmpt362group1.database.Event
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

interface PlannerRepository {
    fun streamEvents(query: String?): Flow<List<Event>>
}

//placeholder
class FakePlannerRepository : PlannerRepository {
    private val state = MutableStateFlow(
//        listOf(
//            Event("Open Gym", "Gym A", "2025-10-12", "2025-10-12", "10:00", "11:00"),
//            Event("Programming Club", "Room 204", "2025-10-12", "2025-10-12", "14:00", "16:00"),
//            Event("Book Club", "Library", "2025-10-13", "2025-10-13", "09:30", "10:30")
//        )
        emptyList<Event>()
    )

    override fun streamEvents(query: String?) =
        state.map { list ->
            if (query.isNullOrBlank()) list
            else list.filter { e ->
                e.title.contains(query, ignoreCase = true) ||
                        e.location.contains(query, ignoreCase = true)
            }
        }
}
