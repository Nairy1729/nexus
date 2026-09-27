package com.nexus.feature.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.nexus.core.di.AppContainer
import com.nexus.data.calls.CallLogRepository
import com.nexus.data.model.ResolvedCall
import com.nexus.core.ui.dayGroupKey
import com.nexus.core.ui.dayLabel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

enum class ActivityFilter(val label: String) {
    All("ALL"),
    Missed("MISSED"),
    Incoming("INCOMING"),
    Outgoing("OUTGOING"),
    Unknown("UNKNOWN"),
    Favorites("FAVORITES"),
}

data class ActivityGroup(
    val label: String,
    val calls: List<ResolvedCall>,
)

data class ActivityUiState(
    val groups: List<ActivityGroup> = emptyList(),
    val filter: ActivityFilter = ActivityFilter.All,
    val query: String = "",
    val visibleCount: Int = 0,
    val totalCount: Int = 0,
)

/**
 * ACTIVITY — the communication timeline. Filtering, search and day grouping happen here;
 * the screen only lays out what it is given.
 */
class ActivityViewModel(
    callLogRepository: CallLogRepository,
) : ViewModel() {

    val filter = MutableStateFlow(ActivityFilter.All)
    val query = MutableStateFlow("")

    val uiState: StateFlow<ActivityUiState> = combine(
        callLogRepository.observeCallLog(),
        query,
        filter,
    ) { log, q, f ->
        val filtered = log.filter { call ->
            val matchesFilter = when (f) {
                ActivityFilter.All -> true
                ActivityFilter.Missed -> call.isMissed
                ActivityFilter.Incoming -> call.type == com.nexus.data.model.CallType.Incoming
                ActivityFilter.Outgoing -> call.type == com.nexus.data.model.CallType.Outgoing
                ActivityFilter.Unknown -> call.isUnknown
                ActivityFilter.Favorites -> call.isFavorite
            }
            val matchesQuery = q.isBlank() ||
                call.displayName.contains(q, ignoreCase = true) ||
                call.number.contains(q)
            matchesFilter && matchesQuery
        }
        val groups = filtered
            .groupBy { dayGroupKey(it.timestampMillis) }
            .toSortedMap(compareByDescending { it })
            .map { (date, calls) ->
                ActivityGroup(
                    label = dayLabel(calls.first().timestampMillis),
                    calls = calls,
                )
            }
        ActivityUiState(
            groups = groups,
            filter = f,
            query = q,
            visibleCount = filtered.size,
            totalCount = log.size,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ActivityUiState())

    fun setFilter(value: ActivityFilter) {
        filter.value = value
    }

    fun setQuery(value: String) {
        query.value = value
    }

    companion object {
        fun factory(container: AppContainer) = viewModelFactory {
            initializer { ActivityViewModel(container.callLogRepository) }
        }
    }
}
