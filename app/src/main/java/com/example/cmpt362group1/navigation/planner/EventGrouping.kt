package com.example.cmpt362group1.navigation.planner

import com.example.cmpt362group1.database.Event
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

data class DaySection(
    val date: LocalDate,
    val items: List<EventWithTime>
)

data class EventWithTime(
    val event: Event,
    val startTime: LocalTime,
    val endTime: LocalTime
)

object EventGrouping {
    private val DATE = DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH)
    private val TIME = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)

    private fun parseDateOrToday(raw: String): LocalDate {
        return try {
            if (raw.isBlank()) LocalDate.now()
            else LocalDate.parse(raw, DATE)
        } catch (_: Exception) {
            LocalDate.now()
        }
    }

    private fun parseTimeOrMidnight(raw: String): LocalTime {
        return try {
            if (raw.isBlank()) LocalTime.MIDNIGHT
            else LocalTime.parse(raw, TIME)
        } catch (_: Exception) {
            LocalTime.MIDNIGHT
        }
    }

    private fun toEventWithTime(event: Event): Pair<LocalDate, EventWithTime> {
        val date = parseDateOrToday(event.startDate)
        val start = parseTimeOrMidnight(event.startTime)
        val end = parseTimeOrMidnight(event.endTime)
        return date to EventWithTime(event, start, end)
    }

    fun groupAndSort(events: List<Event>): List<DaySection> {
        if (events.isEmpty()) return emptyList()

        val withTimes: List<Pair<LocalDate, EventWithTime>> =
            events.map { e -> toEventWithTime(e) }

        val grouped: Map<LocalDate, List<EventWithTime>> =
            withTimes.groupBy({ it.first }, { it.second })

        return grouped
            .toSortedMap()
            .map { (date, itemsForDay) ->
                DaySection(
                    date = date,
                    items = itemsForDay.sortedBy { it.startTime }
                )
            }
    }

    fun niceDateHeader(d: LocalDate): String {
        val month = d.month.getDisplayName(java.time.format.TextStyle.FULL, Locale.ENGLISH)
        val day = d.dayOfMonth
        val year = d.year
        val suffix = when {
            day in 11..13 -> "th"
            day % 10 == 1 -> "st"
            day % 10 == 2 -> "nd"
            day % 10 == 3 -> "rd"
            else -> "th"
        }
        return "$month $day$suffix, $year"
    }

}
