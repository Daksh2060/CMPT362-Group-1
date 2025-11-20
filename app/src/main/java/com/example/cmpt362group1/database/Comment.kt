package com.example.cmpt362group1.database

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Comment(
    @DocumentId
    val id: String = "",

    val eventId: String = "",
    val userId: String = "",
    val userName: String = "",
    val text: String = "",

    val parentId: String? = null,

    @ServerTimestamp
    val createdAt: Date? = null
)
