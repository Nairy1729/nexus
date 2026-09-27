package com.nexus.feature.contact

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.nexus.core.di.AppContainer
import com.nexus.data.calls.CallLogRepository
import com.nexus.data.contacts.ContactRepository
import com.nexus.data.model.CommunicationStats
import com.nexus.data.model.Contact
import com.nexus.data.model.MessageRecord
import com.nexus.data.model.ResolvedCall
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

enum class DnaKind { Incoming, Outgoing, Missed, Message }

data class DnaEntry(
    val id: String,
    val kind: DnaKind,
    val timestampMillis: Long,
    val durationSeconds: Int = 0,
    val outgoing: Boolean = false,
)

data class ContactUiState(
    val contact: Contact? = null,
    val stats: CommunicationStats? = null,
    val dna: List<DnaEntry> = emptyList(),
)

/** Contact profile state — the Communication DNA is assembled here, never in the UI. */
class ContactViewModel(
    contactId: String,
    contactRepository: ContactRepository,
    callLogRepository: CallLogRepository,
) : ViewModel() {

    val uiState: StateFlow<ContactUiState> = combine(
        contactRepository.observeContact(contactId),
        callLogRepository.observeStats(contactId),
        callLogRepository.observeForContact(contactId),
        callLogRepository.observeMessagesForContact(contactId),
    ) { contact, stats, calls, messages ->
        val callEntries = calls.map { it.toDnaEntry() }
        val messageEntries = messages.map { it.toDnaEntry() }
        ContactUiState(
            contact = contact,
            stats = stats,
            dna = (callEntries + messageEntries)
                .sortedByDescending { it.timestampMillis }
                .take(9),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ContactUiState())

    private fun ResolvedCall.toDnaEntry() = DnaEntry(
        id = record.id,
        kind = when (type) {
            com.nexus.data.model.CallType.Incoming -> DnaKind.Incoming
            com.nexus.data.model.CallType.Outgoing -> DnaKind.Outgoing
            com.nexus.data.model.CallType.Missed -> DnaKind.Missed
        },
        timestampMillis = timestampMillis,
        durationSeconds = durationSeconds,
    )

    private fun MessageRecord.toDnaEntry() = DnaEntry(
        id = id,
        kind = DnaKind.Message,
        timestampMillis = timestampMillis,
        outgoing = outgoing,
    )

    companion object {
        fun factory(contactId: String, container: AppContainer) = viewModelFactory {
            initializer {
                ContactViewModel(
                    contactId = contactId,
                    contactRepository = container.contactRepository,
                    callLogRepository = container.callLogRepository,
                )
            }
        }
    }
}
