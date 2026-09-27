package com.nexus.feature.dialer

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
import kotlinx.coroutines.flow.stateIn

/**
 * Dialer state. Matching runs against the repository as digits arrive, so the list of
 * people under the display is never a stale snapshot.
 */
class DialerViewModel(
    private val repository: ContactRepository,
) : ViewModel() {

    val digits = MutableStateFlow("")

    val matches: StateFlow<List<Contact>> = combine(digits) { (input) ->
        repository.matchContacts(input)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun append(value: Char) {
        if (digits.value.length < 18) digits.value += value
    }

    fun backspace() {
        digits.value = digits.value.dropLast(1)
    }

    fun clear() {
        digits.value = ""
    }

    fun formatted(): String = digits.value

    companion object {
        fun factory(container: AppContainer) = viewModelFactory {
            initializer { DialerViewModel(container.contactRepository) }
        }
    }
}
