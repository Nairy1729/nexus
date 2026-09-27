package com.nexus.telephony

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

enum class CallDirection { Incoming, Outgoing }

enum class CallPhase { Ringing, Connecting, Active, Ended }

/**
 * Immutable snapshot of the current call.
 *
 * Phase 3 swaps [CallSessionController] for a Telecom/ConnectionService-backed implementation;
 * the UI only ever reads this state.
 */
data class CallSession(
    val contactId: String?,
    val number: String,
    val displayName: String,
    val direction: CallDirection,
    val phase: CallPhase,
    /** Set when media is up; drives the duration counter. */
    val connectedAtMillis: Long? = null,
    val endedAtMillis: Long? = null,
) {
    val isActive: Boolean get() = phase == CallPhase.Active
}

/** Navigation intents produced by the call session — the UI never decides call flow. */
sealed interface CallCommand {
    /** Full-screen incoming call experience. */
    data object ShowIncoming : CallCommand

    /** Outgoing/active call screen (entering from profile, dialer, or after answering). */
    data object ShowActive : CallCommand

    /** Ringing was declined or the call ended before it was shown — leave call UI. */
    data object Dismiss : CallCommand

    /**
     * Call finished: land on the person's communication history (or simply dismiss when the
     * caller already sits on that profile — the navigator decides using its back stack).
     */
    data class Finish(val contactId: String?) : CallCommand
}

/**
 * Telephony boundary for the whole app. UI observes [session] and reacts to [commands];
 * it never talks to a phone radio directly.
 */
interface CallSessionController {
    val session: StateFlow<CallSession?>
    val commands: Flow<CallCommand>

    fun simulateIncoming(contactId: String)
    fun startOutgoing(contactId: String?, number: String?, displayName: String?)
    fun accept()
    fun decline()
    fun end()

    /** Seconds since media went up (0 while connecting). */
    val elapsedSeconds: StateFlow<Long>
}
