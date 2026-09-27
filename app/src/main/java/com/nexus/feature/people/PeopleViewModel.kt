package com.nexus.feature.people

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.nexus.core.di.AppContainer
import com.nexus.data.contacts.ContactRepository
import com.nexus.data.model.Contact
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

enum class PeopleView { Orbit, List }

/** PEOPLE state: which view, the query, and the two derived contact sets. */
class PeopleViewModel(
    private val repository: ContactRepository,
) : ViewModel() {

    val view = MutableStateFlow(PeopleView.Orbit)
    val query = MutableStateFlow("")

    val orbit: StateFlow<List<Contact>> = repository
        .observeOrbit()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** List view: search results or the full alphabetical network. */
    val listed: StateFlow<List<Contact>> = combine(repository.observeContacts(), query) { all, q ->
        if (q.isBlank()) all else repository.matchContacts(q).ifEmpty {
            all.filter { it.name.contains(q, ignoreCase = true) || it.number.contains(q) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /**
     * Orbit view: the whole network ordered by relationship weight (weekly interactions,
     * then recency). Proximity is earned by the data — the UI only maps order to radius.
     */
    val weighted: StateFlow<List<Contact>> = repository.observeContacts()
        .map { list ->
            list.sortedWith(
                compareByDescending<Contact> { it.weeklyInteractions }
                    .thenByDescending { it.lastInteractionMillis ?: 0L },
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setView(value: PeopleView) {
        view.value = value
    }

    fun setQuery(value: String) {
        query.value = value
    }

    companion object {
        fun factory(container: AppContainer) = viewModelFactory {
            initializer { PeopleViewModel(container.contactRepository) }
        }
    }
}
