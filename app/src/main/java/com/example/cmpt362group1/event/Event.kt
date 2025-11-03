package com.example.cmpt362group1.event

data class Event(
    val title: String = "",
    val location: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val description: String = "",
    val dressCode: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null
)