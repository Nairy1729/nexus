package com.nexus.data.contacts

import com.nexus.data.model.CallRecord
import com.nexus.data.model.CallType
import com.nexus.data.model.Contact
import com.nexus.data.model.MessageRecord

/**
 * Phase 1 mock universe.
 *
 * Everything is anchored to "now" at construction time so TODAY / YESTERDAY grouping,
 * relative timestamps ("4m ago"), and the greeting all stay believable whenever the app runs.
 */
object MockData {

    private const val MINUTE = 60_000L
    private const val HOUR = 60 * MINUTE
    private const val DAY = 24 * HOUR

    private val startedAt = System.currentTimeMillis()

    fun minutesAgo(minutes: Long): Long = startedAt - minutes * MINUTE
    fun hoursAgo(hours: Long): Long = startedAt - hours * HOUR
    fun daysAgo(days: Long): Long = startedAt - days * DAY

    val contacts: List<Contact> = listOf(
        Contact(
            id = "rahul", name = "Rahul Menon", number = "+91 98765 43210",
            isFavorite = true, lifetimeCalls = 142, lifetimeDurationSeconds = 142 * 252,
            lifetimeMessages = 214, weeklyInteractions = 14, lastInteractionMillis = minutesAgo(4),
            subtitle = "Mobile",
        ),
        Contact(
            id = "mom", name = "Mom", number = "+91 98450 11223",
            isFavorite = true, lifetimeCalls = 388, lifetimeDurationSeconds = 388 * 410,
            lifetimeMessages = 640, weeklyInteractions = 11, lastInteractionMillis = minutesAgo(118),
            subtitle = "Mobile",
        ),
        Contact(
            id = "dad", name = "Dad", number = "+91 98450 44556",
            isFavorite = true, lifetimeCalls = 214, lifetimeDurationSeconds = 214 * 305,
            lifetimeMessages = 297, weeklyInteractions = 7, lastInteractionMillis = minutesAgo(470),
            subtitle = "Mobile",
        ),
        Contact(
            id = "priya", name = "Priya Nair", number = "+91 99001 55667",
            isFavorite = false, lifetimeCalls = 74, lifetimeDurationSeconds = 74 * 340,
            lifetimeMessages = 489, weeklyInteractions = 6, lastInteractionMillis = minutesAgo(340),
            subtitle = "Mobile",
        ),
        Contact(
            id = "amit", name = "Amit Sharma", number = "+91 99870 22334",
            isFavorite = true, lifetimeCalls = 96, lifetimeDurationSeconds = 96 * 492,
            lifetimeMessages = 152, weeklyInteractions = 5, lastInteractionMillis = hoursAgo(28),
            subtitle = "Mobile",
        ),
        Contact(
            id = "zoya", name = "Zoya Khan", number = "+91 98190 66363",
            lifetimeCalls = 41, lifetimeDurationSeconds = 41 * 260,
            lifetimeMessages = 88, weeklyInteractions = 4, lastInteractionMillis = minutesAgo(500),
            subtitle = "Mobile",
        ),
        Contact(
            id = "sneha", name = "Sneha", number = "+91 98860 77881",
            lifetimeCalls = 58, lifetimeDurationSeconds = 58 * 195,
            lifetimeMessages = 133, weeklyInteractions = 3, lastInteractionMillis = minutesAgo(430),
            subtitle = "Mobile",
        ),
        Contact(
            id = "raj", name = "Raj Kulkarni", number = "+91 98760 11223",
            lifetimeCalls = 33, lifetimeDurationSeconds = 33 * 420,
            lifetimeMessages = 41, weeklyInteractions = 2, lastInteractionMillis = minutesAgo(600),
            subtitle = "Mobile",
        ),
        Contact(
            id = "rakesh", name = "Rakesh Iyer", number = "+91 98761 44556",
            lifetimeCalls = 27, lifetimeDurationSeconds = 27 * 150,
            lifetimeMessages = 19, weeklyInteractions = 2, lastInteractionMillis = hoursAgo(36),
            subtitle = "Mobile",
        ),
        Contact(
            id = "vikram", name = "Vikram Rao", number = "+91 99460 22991",
            lifetimeCalls = 19, lifetimeDurationSeconds = 19 * 240,
            lifetimeMessages = 12, weeklyInteractions = 1, lastInteractionMillis = minutesAgo(520),
            subtitle = "Mobile",
        ),
        Contact(
            id = "studio", name = "Studio", number = "080 4123 4567",
            lifetimeCalls = 88, lifetimeDurationSeconds = 88 * 130,
            lifetimeMessages = 4, weeklyInteractions = 1, lastInteractionMillis = daysAgo(4),
            subtitle = "Work",
        ),
    )

    private const val UNKNOWN_A = "+91 90080 77442"
    private const val UNKNOWN_B = "+91 88007 11223"
    private const val UNKNOWN_C = "+91 70420 55661"

    /** Recent-first is only a reading convenience; repositories sort explicitly. */
    val callRecords: List<CallRecord> = listOf(
        // ---- Today (12 interactions) --------------------------------------
        record("c01", "rahul", CallType.Incoming, minutesAgo(4), 272),
        record("c02", "mom", CallType.Outgoing, minutesAgo(118), 130),
        unknown("c03", UNKNOWN_A, CallType.Missed, minutesAgo(300)),
        record("c04", "priya", CallType.Outgoing, minutesAgo(340), 95),
        record("c05", "rahul", CallType.Outgoing, minutesAgo(400), 65),
        record("c06", "sneha", CallType.Incoming, minutesAgo(430), 180),
        record("c07", "dad", CallType.Outgoing, minutesAgo(470), 245),
        record("c08", "zoya", CallType.Missed, minutesAgo(500), 0),
        record("c09", "vikram", CallType.Incoming, minutesAgo(520), 60),
        record("c10", "mom", CallType.Incoming, minutesAgo(560), 600),
        record("c11", "raj", CallType.Outgoing, minutesAgo(600), 420),
        unknown("c12", UNKNOWN_B, CallType.Missed, minutesAgo(630)),
        // ---- Yesterday ------------------------------------------------------
        record("c13", "amit", CallType.Outgoing, hoursAgo(28), 492),
        record("c14", "rahul", CallType.Missed, hoursAgo(30), 0),
        record("c15", "mom", CallType.Incoming, hoursAgo(33), 600),
        record("c16", "rakesh", CallType.Outgoing, hoursAgo(36), 150),
        unknown("c17", UNKNOWN_C, CallType.Missed, hoursAgo(38)),
        record("c18", "priya", CallType.Incoming, hoursAgo(41), 45),
        // ---- Earlier --------------------------------------------------------
        record("c19", "dad", CallType.Outgoing, daysAgo(3), 900),
        record("c20", "rahul", CallType.Outgoing, daysAgo(5), 1800),
        record("c21", "sneha", CallType.Missed, daysAgo(6), 0),
        record("c22", "amit", CallType.Incoming, daysAgo(7), 505),
        record("c23", "zoya", CallType.Incoming, daysAgo(9), 330),
        record("c24", "raj", CallType.Outgoing, daysAgo(11), 420),
        record("c25", "studio", CallType.Outgoing, daysAgo(12), 118),
        record("c26", "rakesh", CallType.Incoming, daysAgo(13), 500),
        record("c27", "amit", CallType.Missed, daysAgo(15), 0),
        record("c28", "rahul", CallType.Incoming, daysAgo(18), 720),
        record("c29", "vikram", CallType.Outgoing, daysAgo(21), 240),
        record("c30", "mom", CallType.Outgoing, daysAgo(24), 380),
    )

    val messages: List<MessageRecord> = listOf(
        message("m01", "rahul", minutesAgo(50), outgoing = false),
        message("m02", "rahul", minutesAgo(58), outgoing = true),
        message("m03", "priya", minutesAgo(360), outgoing = true),
        message("m04", "rahul", minutesAgo(430), outgoing = true),
        message("m05", "mom", minutesAgo(180), outgoing = false),
        message("m06", "amit", hoursAgo(29), outgoing = false),
        message("m07", "priya", daysAgo(2), outgoing = false),
        message("m08", "rahul", daysAgo(2), outgoing = true),
        message("m09", "mom", daysAgo(3), outgoing = true),
        message("m10", "amit", daysAgo(5), outgoing = true),
        message("m11", "dad", daysAgo(6), outgoing = false),
        message("m12", "rahul", daysAgo(4), outgoing = false),
    )

    private fun record(
        id: String,
        contactId: String,
        type: CallType,
        timestamp: Long,
        duration: Int,
    ): CallRecord {
        val number = contacts.first { it.id == contactId }.number
        return CallRecord(id, contactId, number, type, timestamp, duration)
    }

    private fun unknown(id: String, number: String, type: CallType, timestamp: Long) =
        CallRecord(id, null, number, type, timestamp, 0)

    private fun message(id: String, contactId: String, timestamp: Long, outgoing: Boolean) =
        MessageRecord(id, contactId, timestamp, outgoing)
}
