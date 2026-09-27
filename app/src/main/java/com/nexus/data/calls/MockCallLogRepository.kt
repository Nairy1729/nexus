package com.nexus.data.calls

import com.nexus.data.contacts.ContactRepository
import com.nexus.data.contacts.MockData
import com.nexus.data.model.CommunicationStats
import com.nexus.data.model.MessageRecord
import com.nexus.data.model.ResolvedCall
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** In-memory call log (Phase 1). Phase 2 replaces this with CallLogContract-backed data. */
class MockCallLogRepository(
    private val contacts: ContactRepository,
) : CallLogRepository {

    private val log = MutableStateFlow(
        MockData.callRecords
            .map { record ->
                val contact = contacts.contactsSnapshot().firstOrNull { it.id == record.contactId }
                ResolvedCall(
                    record = record,
                    contactId = record.contactId,
                    displayName = contact?.name ?: "Unknown",
                    number = record.number,
                    type = record.type,
                    timestampMillis = record.timestampMillis,
                    durationSeconds = record.durationSeconds,
                    isFavorite = contact?.isFavorite == true,
                )
            }
            .sortedByDescending { it.timestampMillis }
    )

    override fun observeCallLog(): Flow<List<ResolvedCall>> = log

    override fun observeForContact(contactId: String): Flow<List<ResolvedCall>> =
        log.map { list -> list.filter { it.contactId == contactId } }

    override fun observeMessagesForContact(contactId: String): Flow<List<MessageRecord>> =
        MutableStateFlow(
            MockData.messages
                .filter { it.contactId == contactId }
                .sortedByDescending { it.timestampMillis }
        )

    override fun observeStats(contactId: String): Flow<CommunicationStats> =
        log.map { list ->
            val contact = contacts.contactsSnapshot().firstOrNull { it.id == contactId }
            val calls = list.filter { it.contactId == contactId }
            val messages = MockData.messages.filter { it.contactId == contactId }
            val last = (calls.map { it.timestampMillis } +
                messages.map { it.timestampMillis } +
                listOfNotNull(contact?.lastInteractionMillis)).maxOrNull()
            CommunicationStats(
                totalCalls = contact?.lifetimeCalls ?: calls.size,
                totalDurationSeconds = contact?.lifetimeDurationSeconds
                    ?: calls.sumOf { it.durationSeconds }.toLong(),
                averageDurationSeconds = when {
                    contact == null || contact.lifetimeCalls == 0 -> 0
                    else -> contact.lifetimeDurationSeconds / contact.lifetimeCalls
                },
                missedCalls = calls.count { it.isMissed },
                messages = contact?.lifetimeMessages ?: messages.size,
                lastInteractionMillis = last,
                interactionsThisWeek = contact?.weeklyInteractions ?: 0,
            )
        }

    override fun observeTodayCount(): Flow<Int> {
        val today = LocalDate.now()
        return log.map { list ->
            list.count {
                java.time.Instant.ofEpochMilli(it.timestampMillis)
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate() == today
            }
        }
    }

    override fun observeMissedCount(): Flow<Int> = log.map { list -> list.count { it.isMissed } }
}
