package com.nexus.core.di

import com.nexus.data.calls.CallLogRepository
import com.nexus.data.calls.MockCallLogRepository
import com.nexus.data.contacts.ContactRepository
import com.nexus.data.contacts.MockContactRepository
import com.nexus.telephony.CallSessionController
import com.nexus.telephony.MockCallSessionController

/**
 * Manual dependency container (Phase 1).
 *
 * Every dependency is a constructor-injected interface, so feature ViewModels can later be
 * created by Hilt without touching their signatures.
 */
class AppContainer {

    val contactRepository: ContactRepository by lazy { MockContactRepository() }

    val callLogRepository: CallLogRepository by lazy { MockCallLogRepository(contactRepository) }

    /** Owns the call session state machine. Mock implementation until Phase 3 (telecom). */
    val callSessionController: CallSessionController by lazy {
        MockCallSessionController(contactRepository)
    }
}
