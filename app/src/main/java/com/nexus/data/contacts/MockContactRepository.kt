package com.nexus.data.contacts

import com.nexus.data.model.Contact
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * In-memory repository seeded with realistic people (Phase 1).
 * Kept as a cold-flow source so real implementations can be dropped in behind the interface.
 */
class MockContactRepository : ContactRepository {

    private val contacts = MutableStateFlow(MockData.contacts)

    override fun observeContacts(): Flow<List<Contact>> =
        contacts.map { list -> list.sortedBy { it.name } }

    override fun observeContact(id: String): Flow<Contact?> =
        contacts.map { list -> list.firstOrNull { it.id == id } }

    override fun observeFavorites(): Flow<List<Contact>> =
        contacts.map { list -> list.filter { it.isFavorite }.sortedByDescending { it.weeklyInteractions } }

    override suspend fun getContact(id: String): Contact? =
        contacts.value.firstOrNull { it.id == id }

    override fun contactsSnapshot(): List<Contact> = contacts.value

    override fun matchContacts(query: String): List<Contact> {
        val bare = query.filter { it.isDigit() }
        if (bare.isEmpty()) return emptyList()
        return contacts.value
            .filter { contact ->
                contact.number.filter { it.isDigit() }.contains(bare) ||
                    t9Prefix(contact.name).startsWith(bare)
            }
            .sortedByDescending { it.weeklyInteractions }
            .take(4)
    }

    override fun observePriority(): Flow<List<Contact>> = observeFavorites()

    override fun observeOrbit(): Flow<List<Contact>> =
        contacts.map { list -> list.sortedByDescending { it.weeklyInteractions }.take(6) }

    private fun t9Prefix(name: String): String = buildString {
        name.filter { it.isLetter() }.uppercase().forEach { append(t9(it)) }
    }

    private fun t9(c: Char): Char = when (c) {
        'A', 'B', 'C' -> '2'
        'D', 'E', 'F' -> '3'
        'G', 'H', 'I' -> '4'
        'J', 'K', 'L' -> '5'
        'M', 'N', 'O' -> '6'
        'P', 'Q', 'R', 'S' -> '7'
        'T', 'U', 'V' -> '8'
        'W', 'X', 'Y', 'Z' -> '9'
        else -> c
    }
}
