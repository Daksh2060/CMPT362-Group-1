package com.example.cmpt362group1.database

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class UserRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : UserRepository {

    companion object {
        private const val USERS_COLLECTION = "users"
    }

    override fun getUser(uid: String): Flow<User?> = callbackFlow {
        val listenerRegistration = firestore.collection(USERS_COLLECTION)
            .document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val user = snapshot?.toObject<User>()
                trySend(user)
            }

        awaitClose { listenerRegistration.remove() }
    }

    override suspend fun createUser(user: User): Result<Unit> {
        return try {
            firestore.collection(USERS_COLLECTION)
                .document(user.id)
                .set(user)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUser(uid: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            firestore.collection(USERS_COLLECTION)
                .document(uid)
                .update(updates)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteUser(uid: String): Result<Unit> {
        return try {
            firestore.collection(USERS_COLLECTION)
                .document(uid)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addCreatedEvent(uid: String, eventId: String): Result<Unit> {
        return try {
            firestore.collection(USERS_COLLECTION)
                .document(uid)
                .update("eventsCreated", FieldValue.arrayUnion(eventId))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addJoinedEvent(uid: String, eventId: String): Result<Unit> {
        return try {
            firestore.collection(USERS_COLLECTION)
                .document(uid)
                .update("eventsJoined", FieldValue.arrayUnion(eventId))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removeCreatedEvent(uid: String, eventId: String): Result<Unit> {
        return try {
            firestore.collection(USERS_COLLECTION)
                .document(uid)
                .update("eventsCreated", FieldValue.arrayRemove(eventId))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removeJoinedEvent(uid: String, eventId: String): Result<Unit> {
        return try {
            firestore.collection(USERS_COLLECTION)
                .document(uid)
                .update("eventsJoined", FieldValue.arrayRemove(eventId))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}