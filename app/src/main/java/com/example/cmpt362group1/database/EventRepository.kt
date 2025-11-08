package com.example.cmpt362group1.database

import kotlinx.coroutines.flow.Flow

interface EventRepository {
    val collection: String

    fun getEvent(id: String): Flow<Event?>

    fun getAllEvents(): Flow<List<Event?>>

    suspend fun addEvent(event: Event): Result<String>

    suspend fun clearEvents(): Result<Unit>
}