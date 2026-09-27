package com.nexus.data.contacts

import com.nexus.data.model.Contact
import kotlinx.coroutines.flow.Flow

/**
 * Single source of truth for people. Phase 2 swaps [MockContactRepository] for a
 * ContactsProvider-backed implementation; call sites do not change.
 */
interface ContactRepository {

    fun observeContacts(): Flow<List<Contact>>

    fun observeContact(id: String): Flow<Contact?>

    fun observeFavorites(): Flow<List<Contact>>

    suspend fun getContact(id: String): Contact?

    /** Synchronous snapshot — used by repositories that need to resolve ids off the main thread. */
    fun contactsSnapshot(): List<Contact>

    /**
     * Dialer matching: digits match a substring of the phone number, or a T9 prefix of the
     * name — so typing [query] "9876" surfaces Rahul, Raj and Rakesh as the user types.
     */
    fun matchContacts(query: String): List<Contact>

    /** Priority people for HOME — favorites ordered by recent interaction weight. */
    fun observePriority(): Flow<List<Contact>>

    /** Top people by weight for the orbital system. */
    fun observeOrbit(): Flow<List<Contact>>
}
