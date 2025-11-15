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

    val description: String = "",
    val hobby: String = "",

    val eventsCreated: ArrayList<String>,
    val eventsJoined: ArrayList<String>,
)
