package com.nexus.core.design.orb

/**
 * Visual states for [NexusOrb].
 *
 * Adapted from Jakub Antalik's open-source thinking-orbs animation principles (MIT License)
 * and tailored specifically to the NEXUS Spatial Glass OS communication system.
 */
enum class NexusOrbState(val label: String) {
    /**
     * Calm breathing motion.
     * Used when NEXUS is in standby or waiting for input.
     */
    Idle("Standby"),

    /**
     * Distributed particle field connecting and establishing pathways.
     * Used when a signal is acquiring, carrier locking, or a call is establishing.
     */
    Connecting("Connecting"),

    /**
     * Scanning meridian sweep across a sphere lattice.
     * Used when searching contacts or resolving dialed digits.
     */
    Searching("Searching"),

    /**
     * Interlocking dimensional shift and solve cycles.
     * Reserved for intelligent contact graph analysis and contextual resolution.
     */
    Solving("Resolving"),

    /**
     * Undulating harmonic audio waveform rings.
     * Used for voice activity, audio input, or live transmission.
     */
    Listening("Listening"),

    /**
     * Continuous flowing ribbon sash.
     * Used for message composition and outgoing transmission prep.
     */
    Composing("Composing"),

    /**
     * High-energy multi-tilt particle orbits.
     * Used during active two-way voice communication.
     */
    Responding("Active Session"),

    /**
     * Continuous arc-length morphing geometry.
     * Used during UI transitions, contact selection, and expansion.
     */
    Shaping("Shaping"),
}
