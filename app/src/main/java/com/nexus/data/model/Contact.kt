package com.nexus.data.model

/**
 * A person inside the NEXUS network.
 *
 * [weeklyInteractions] and [lastInteractionMillis] are not decoration: they drive orbital
 * proximity, node size, and priority ordering. Recent/meaningful relationships sit closer.
 */
data class Contact(
    val id: String,
    val name: String,
    val number: String,
    val isFavorite: Boolean = false,
    /** Lifetime counters so profile statistics stay believable on top of the mock log. */
    val lifetimeCalls: Int = 0,
    val lifetimeDurationSeconds: Long = 0,
    val lifetimeMessages: Int = 0,
    /** Interaction weight for the current week — drives orbit radius and node scale. */
    val weeklyInteractions: Int = 0,
    val lastInteractionMillis: Long? = null,
    /** Short secondary line, e.g. "Mobile" or a city. Rendered as metadata only. */
    val subtitle: String = "Mobile",
)
