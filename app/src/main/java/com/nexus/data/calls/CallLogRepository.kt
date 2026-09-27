package com.nexus.data.calls

import com.nexus.data.model.CommunicationStats
import com.nexus.data.model.Contact
import com.nexus.data.model.MessageRecord
import com.nexus.data.model.ResolvedCall
import kotlinx.coroutines.flow.Flow

/** Call history as a communication timeline. */
interface CallLogRepository {

    /** Full history, newest first. */
    fun observeCallLog(): Flow<List<ResolvedCall>>

    /** Interactions for one person (calls merged with messages is handled by [observeInteractions]). */
    fun observeForContact(contactId: String): Flow<List<ResolvedCall>>

    fun observeMessagesForContact(contactId: String): Flow<List<MessageRecord>>

    fun observeStats(contactId: String): Flow<CommunicationStats>

    /** Today's interaction count for the HOME greeting line. */
    fun observeTodayCount(): Flow<Int>

    fun observeMissedCount(): Flow<Int>
}

/** What HOME needs in one shot: totals plus the freshest events. */
data class HomeSummary(
    val todayCount: Int,
    val missedCount: Int,
    val recent: List<ResolvedCall>,
)

/** Profile statistics bound to a person, resolved once. */
data class ContactStats(
    val contact: Contact,
    val stats: CommunicationStats,
)
