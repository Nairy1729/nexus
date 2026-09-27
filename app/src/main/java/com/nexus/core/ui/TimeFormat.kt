package com.nexus.core.ui

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * All time language used across NEXUS lives here so the voice stays consistent:
 * short, lowercase-suffix, never chatty.
 */
private val clockFormat = DateTimeFormatter.ofPattern("HH:mm")
private val dayMonthFormat = DateTimeFormatter.ofPattern("d MMM")

fun formatClock(millis: Long): String =
    LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault()).format(clockFormat)

/** "04:32" / "1:02:07" — call durations use a fixed, tabular feel. */
fun formatDuration(totalSeconds: Long): String {
    val s = totalSeconds.coerceAtLeast(0)
    val h = s / 3600
    val m = (s % 3600) / 60
    val sec = s % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, sec) else "%02d:%02d".format(m, sec)
}

/** "just now" / "4m ago" / "2h ago" / "Yesterday" / "Mon" / "12 Sep". */
fun relativeTime(millis: Long, now: Long = System.currentTimeMillis()): String {
    val diff = (now - millis).coerceAtLeast(0)
    val minutes = diff / 60_000
    val zone = ZoneId.systemDefault()
    val then = Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()
    val today = Instant.ofEpochMilli(now).atZone(zone).toLocalDate()
    return when {
        minutes < 1 -> "just now"
        minutes < 60 -> "${minutes}m ago"
        then == today && minutes < 24 * 60 -> "${minutes / 60}h ago"
        then == today.minusDays(1) -> "Yesterday"
        then.isAfter(today.minusDays(6)) ->
            then.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
        then.year == today.year -> then.format(dayMonthFormat)
        else -> then.format(DateTimeFormatter.ofPattern("d MMM yyyy"))
    }
}

/** Timeline group headers: TODAY / YESTERDAY / MON / 12 SEP. */
fun dayLabel(millis: Long, now: Long = System.currentTimeMillis()): String {
    val zone = ZoneId.systemDefault()
    val date = Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()
    val today = Instant.ofEpochMilli(now).atZone(zone).toLocalDate()
    return when (date) {
        today -> "TODAY"
        today.minusDays(1) -> "YESTERDAY"
        else -> date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).uppercase(Locale.getDefault())
    }
}

fun dayGroupKey(millis: Long): LocalDate =
    Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()

/** GOOD MORNING / GOOD AFTERNOON / GOOD EVENING. */
fun greeting(millis: Long = System.currentTimeMillis()): String {
    val hour = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).hour
    return when (hour) {
        in 5..11 -> "GOOD MORNING"
        in 12..16 -> "GOOD AFTERNOON"
        else -> "GOOD EVENING"
    }
}

fun formatNumber(number: String): String = number
