package com.nexus.telephony

import com.nexus.data.contacts.ContactRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Phase 1 stand-in for a real Telecom implementation.
 *
 * It runs the same state machine a ConnectionService would drive (Ringing → Connecting →
 * Active → Ended) so every UI path, transition and timer can be built and reviewed before
 * real telephony lands in Phase 3. No fake "connected to network" behaviour is exposed.
 */
class MockCallSessionController(
    private val contacts: ContactRepository,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : CallSessionController {

    private val _session = MutableStateFlow<CallSession?>(null)
    private val _commands = MutableSharedFlow<CallCommand>(extraBufferCapacity = 8)
    private val _elapsed = MutableStateFlow(0L)

    private var phaseJob: Job? = null
    private var tickerJob: Job? = null

    override val session: StateFlow<CallSession?> = _session.asStateFlow()
    override val commands: Flow<CallCommand> = _commands.asSharedFlow()
    override val elapsedSeconds: StateFlow<Long> = _elapsed.asStateFlow()

    override fun simulateIncoming(contactId: String) {
        val contact = runCatching { contacts.contactsSnapshot().firstOrNull { it.id == contactId } }
            .getOrNull() ?: return
        phaseJob?.cancel()
        tickerJob?.cancel()
        _elapsed.value = 0
        _session.value = CallSession(
            contactId = contact.id,
            number = contact.number,
            displayName = contact.name,
            direction = CallDirection.Incoming,
            phase = CallPhase.Ringing,
        )
        _commands.tryEmit(CallCommand.ShowIncoming)
    }

    override fun startOutgoing(contactId: String?, number: String?, displayName: String?) {
        val contact = contactId?.let { id -> contacts.contactsSnapshot().firstOrNull { it.id == id } }
        phaseJob?.cancel()
        tickerJob?.cancel()
        _elapsed.value = 0
        _session.value = CallSession(
            contactId = contactId,
            number = number ?: contact?.number.orEmpty(),
            displayName = displayName ?: contact?.name ?: "Unknown",
            direction = CallDirection.Outgoing,
            phase = CallPhase.Connecting,
        )
        _commands.tryEmit(CallCommand.ShowActive)
        moveToActive(afterMillis = 2_200)
    }

    override fun accept() {
        val current = _session.value ?: return
        if (current.phase != CallPhase.Ringing) return
        _session.value = current.copy(phase = CallPhase.Connecting)
        _commands.tryEmit(CallCommand.ShowActive)
        moveToActive(afterMillis = 1_200)
    }

    override fun decline() {
        val current = _session.value ?: return
        if (current.phase != CallPhase.Ringing) return
        phaseJob?.cancel()
        _session.value =
            current.copy(phase = CallPhase.Ended, endedAtMillis = System.currentTimeMillis())
        _commands.tryEmit(CallCommand.Dismiss)
    }

    override fun end() {
        val current = _session.value ?: return
        if (current.phase == CallPhase.Ended) return
        phaseJob?.cancel()
        tickerJob?.cancel()
        _session.value =
            current.copy(phase = CallPhase.Ended, endedAtMillis = System.currentTimeMillis())
        _commands.tryEmit(CallCommand.Finish(current.contactId))
    }

    private fun moveToActive(afterMillis: Long) {
        phaseJob?.cancel()
        phaseJob = scope.launch {
            delay(afterMillis)
            val connectedAt = System.currentTimeMillis()
            _session.update { s ->
                if (s != null && s.phase == CallPhase.Connecting) {
                    s.copy(phase = CallPhase.Active, connectedAtMillis = connectedAt)
                } else {
                    s
                }
            }
            startTicker(connectedAt)
        }
    }

    private fun startTicker(connectedAt: Long) {
        tickerJob?.cancel()
        tickerJob = scope.launch {
            while (true) {
                _elapsed.value = (System.currentTimeMillis() - connectedAt) / 1000
                delay(1_000)
            }
        }
    }
}
