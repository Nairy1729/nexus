package com.nexus.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.nexus.core.di.AppContainer
import com.nexus.data.calls.CallLogRepository
import com.nexus.data.contacts.ContactRepository
import com.nexus.data.model.Contact
import com.nexus.data.model.ResolvedCall
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** Everything HOME shows, derived from data — greeting count, priority people, recents. */
class HomeViewModel(
    contactRepository: ContactRepository,
    callLogRepository: CallLogRepository,
) : ViewModel() {

    val priority: StateFlow<List<Contact>> = contactRepository
        .observePriority()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val recent: StateFlow<List<ResolvedCall>> = callLogRepository
        .observeCallLog()
        .map { it.take(5) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val todayCount: StateFlow<Int> = callLogRepository
        .observeTodayCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val missedCount: StateFlow<Int> = callLogRepository
        .observeMissedCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    companion object {
        fun factory(container: AppContainer) = viewModelFactory {
            initializer {
                HomeViewModel(container.contactRepository, container.callLogRepository)
            }
        }
    }
}
