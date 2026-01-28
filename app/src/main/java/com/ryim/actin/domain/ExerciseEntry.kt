package com.ryim.actin.domain

import kotlinx.serialization.Serializable
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.time.ExperimentalTime
import kotlinx.datetime.Instant
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import java.util.UUID


@Serializable
data class ExerciseEntry(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val sets: Int,
    val reps: List<Int>,
    val weights: List<Float>,
    val useKg: Boolean,
    val day: Int,
    val month: Int,
    val year: Int,
    val timestamp: String? = null,
    val workout: String? = null
)

// domain/ExerciseEntry.kt

@OptIn(ExperimentalTime::class)
fun ExerciseEntry.noonTimestamp(): String {
    val date = LocalDate(year, month, day)
    val time = LocalTime(12, 0)
    val localDateTime = LocalDateTime(date, time)

    val zone = TimeZone.currentSystemDefault()
    return localDateTime.toInstant(zone).toString()
}

fun ExerciseEntry.localDate(zone: TimeZone): LocalDate =
    Instant.parse(timestamp!!)
        .toLocalDateTime(zone)
        .date

fun ExerciseEntry.formattedDate(zone: TimeZone): String {
    val date = Instant.parse(timestamp!!)
        .toLocalDateTime(zone)
        .date

    val day = date.dayOfMonth.toString().padStart(2, '0')
    val month = monthAbbrev(date.monthNumber)
    val year = date.year.toString()

    return "$day $month $year"
}


fun monthAbbrev(month: Int): String {
    return when (month) {
        1 -> "Jan"
        2 -> "Feb"
        3 -> "Mar"
        4 -> "Apr"
        5 -> "May"
        6 -> "Jun"
        7 -> "Jul"
        8 -> "Aug"
        9 -> "Sep"
        10 -> "Oct"
        11 -> "Nov"
        12 -> "Dec"
        else -> ""
    }
}

fun formatTimestampPretty(timestamp: String): String {
    val instant = Instant.parse(timestamp)
    val ldt = instant.toLocalDateTime(TimeZone.currentSystemDefault())

    val day = ldt.date.dayOfMonth
    val month = ldt.date.monthNumber
    val year = ldt.date.year % 100

    val hour = ldt.time.hour
    val minute = ldt.time.minute

    return "%02d %s %02d • %02d:%02d".format(
        day,
        monthAbbrev(month),
        year,
        hour,
        minute
    )
}
