package com.nexus.data.model

enum class CallType { Incoming, Outgoing, Missed }

/** One raw call-log row. [contactId] is null for numbers NEXUS does not know. */
data class CallRecord(
    val id: String,
    val contactId: String?,
    val number: String,
    val type: CallType,
    val timestampMillis: Long,
    val durationSeconds: Int,
)

/**
 * A call record with its contact resolved for display.
 * [displayName] falls back to "Unknown" for strangers, so UI never has to null-check.
 */
data class ResolvedCall(
    val record: CallRecord,
    val contactId: String?,
    val displayName: String,
    val number: String,
    val type: CallType,
    val timestampMillis: Long,
    val durationSeconds: Int,
    val isFavorite: Boolean,
) {
    val isMissed: Boolean get() = type == CallType.Missed
    val isUnknown: Boolean get() = contactId == null
}

/** A message event surfaced inside the Communication DNA timeline (mock data in Phase 1). */
data class MessageRecord(
    val id: String,
    val contactId: String,
    val timestampMillis: Long,
    val outgoing: Boolean,
)

/** Derived profile statistics — computed, never hard-coded in the UI. */
data class CommunicationStats(
    val totalCalls: Int,
    val totalDurationSeconds: Long,
    val averageDurationSeconds: Long,
    val missedCalls: Int,
    val messages: Int,
    val lastInteractionMillis: Long?,
    val interactionsThisWeek: Int,
)
