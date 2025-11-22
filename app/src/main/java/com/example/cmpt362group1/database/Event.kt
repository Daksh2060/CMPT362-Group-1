package com.example.cmpt362group1.database


import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Event(
    @DocumentId
    val id: String = "",

    @ServerTimestamp
    val createdAt: Date? = null,
    val createdBy: String = "",

    val title: String = "",
    val location: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val description: String = "",
    val dressCode: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val imageUrl: String = "",
)