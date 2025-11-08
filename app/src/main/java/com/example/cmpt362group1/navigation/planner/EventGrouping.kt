package com.example.cmpt362group1.navigation.planner

import com.example.cmpt362group1.database.Event
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

data class DaySection(
    val date: LocalDate,
    val items: List<EventWithTime>
)

data class EventWithTime(
    val event: Event,
    val start: LocalDateTime,
    val end: LocalDateTime?
)

object EventGrouping {
    private val DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val TIME = DateTimeFormatter.ofPattern("HH:mm")

    private fun toLdt(date: String, time: String): LocalDateTime? = try {
        LocalDateTime.of(LocalDate.parse(date, DATE), LocalTime.parse(time, TIME))
    } catch (_: Exception) { null }

    fun groupAndSort(events: List<Event>): List<DaySection> {
        val mapped = events.mapNotNull { e ->
            val start = toLdt(e.startDate, e.startTime) ?: return@mapNotNull null
            val end = if (e.endDate.isNotBlank() && e.endTime.isNotBlank())
                toLdt(e.endDate, e.endTime) else null
            EventWithTime(e, start, end)
        }

        return mapped
            .groupBy { it.start.toLocalDate() }
            .toSortedMap()
            .map { (date, list) ->
                DaySection(date, list.sortedBy { it.start.toLocalTime() })
            }
    }

    fun niceDateHeader(d: LocalDate): String {
        val month = d.month.getDisplayName(java.time.format.TextStyle.FULL, Locale.ENGLISH)
        val day = d.dayOfMonth
        val suffix = when {
            day in 11..13 -> "th"
            day % 10 == 1 -> "st"
            day % 10 == 2 -> "nd"
            day % 10 == 3 -> "rd"
            else -> "th"
        }
        return "$day$suffix $month"
    }
}
