package com.example.cmpt362group1.database

import androidx.compose.runtime.snapshotFlow
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class EventRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : EventRepository {
    override val collection: String
        get() = "events"

    override fun getEvent(id: String): Flow<Event?> = callbackFlow {
        val listenerRegistration = firestore.collection(collection)
            .document(id)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val event = snapshot?.toObject<Event>()
                trySend(event)
            }

        awaitClose { listenerRegistration.remove() }
    }

    override fun getAllEvents(): Flow<List<Event?>> = callbackFlow {
        val listenerRegistration = firestore.collection(collection)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val events = snapshot
                    ?.documents
                    ?.mapNotNull {
                        it.toObject<Event>()
                    }
                    ?: emptyList()
                trySend(events)
            }

        awaitClose { listenerRegistration.remove() }
    }

    override suspend fun addEvent(event: Event): Result<String> {
        return try {
            val documentRef = firestore.collection(collection).document()
            val eventTagged= event.copy(id = documentRef.id)
            documentRef.set(eventTagged).await()
            Result.success(documentRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun clearEvents(): Result<Unit> {
        return try {
            val snapshot = firestore.collection(collection).get().await()
            val batch = firestore.batch()
            for (document in snapshot.documents) {
                batch.delete(document.reference)
            }

            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}