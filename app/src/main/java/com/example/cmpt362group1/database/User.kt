package com.example.cmpt362group1.database

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class User(
    @DocumentId
    val id: String = "",

    @ServerTimestamp
    val createdAt: Date? = null,

    val email: String = "",
    val displayName: String = "",
    val photoUrl: String = "",
    val description: String = "I don't have a bio yet... :/",

    val firstName: String = "",
    val lastName: String = "",
    val birthdate: Date? = null,
    val faculty: String = "",
    val occupancy: String = "",
    val enrollmentYear: Int? = null,
    val hobby: String = "",

    val eventsCreated: ArrayList<String> = arrayListOf(),
    val eventsJoined: ArrayList<String> = arrayListOf(),
)
