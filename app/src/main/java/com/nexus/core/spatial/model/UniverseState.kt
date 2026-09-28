package com.nexus.core.spatial.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Quality levels for adaptive rendering in NEXUS Universe.
 */
enum class QualityLevel {
    High,
    Medium,
    Low
}

/**
 * Celestial body types in the NEXUS communication universe.
 */
enum class BodyType {
    UserCore,    // The central luminous core: YOU
    Planet,      // Known contact
    Visitor,     // Unknown caller / unmapped celestial object
    Satellite    // Moon / communication event node
}

/**
 * Representation of a celestial body (contact, visitor, or user core) in 3D world space.
 */
@Immutable
data class CelestialBody(
    val id: String,
    val name: String,
    val number: String,
    val initials: String,
    val type: BodyType = BodyType.Planet,
    val rank: Int = 0,
    // Orbital coordinates (polar in X-Z plane, with slight Y variance)
    val orbitRadius: Float = 1.0f,
    val orbitSpeed: Float = 0.2f,       // radians per second
    val baseAngle: Float = 0.0f,        // initial angular position
    val yOffset: Float = 0.0f,          // slight vertical tilt for 3D depth
    val radius: Float = 0.18f,          // planet visual scale
    // Visual identity (deterministic per contact)
    val primaryColor: Color = Color(0xFFCBFF4D),
    val secondaryColor: Color = Color(0xFF141418),
    val atmosphereColor: Color = Color(0x66CBFF4D),
    val atmosphereGlow: Float = 0.5f,
    val hasRings: Boolean = false,
    val ringRadius: Float = 0.35f,
    // State flags
    val isFavorite: Boolean = false,
    val isEclipse: Boolean = false,     // Missed call state: obscured / dimmed
    val isVisitor: Boolean = false,     // Unknown caller
    val isSelected: Boolean = false,
    val isIncoming: Boolean = false,
    val isActiveCall: Boolean = false,
    val recentActivityText: String? = null,
    val communicationDna: List<SpatialEvent> = emptyList(),
)

/**
 * Spatial communication event representing history nodes around a planet.
 */
@Immutable
data class SpatialEvent(
    val id: String,
    val label: String,
    val detail: String,
    val isIncoming: Boolean,
    val isMissed: Boolean,
    val timestampMillis: Long,
    val angle: Float,
    val distance: Float,
)

/**
 * An orbital shell defining depth tiers in the communication universe.
 */
@Immutable
data class SpatialOrbit(
    val ringIndex: Int,
    val radius: Float,
    val color: Color = Color(0x26FFFFFF),
    val strokeWidth: Float = 1.0f,
    val name: String = "Tier $ringIndex",
)

/**
 * Constellation grouping connected celestial bodies (e.g. Family, Work).
 */
@Immutable
data class SpatialConstellation(
    val id: String,
    val name: String,
    val memberIds: List<String>,
    val color: Color = Color(0x4DCBFF4D),
)

/**
 * Energy signal wave propagating outward (e.g. from dialing, or incoming signal).
 */
@Immutable
data class SpatialSignal(
    val id: String,
    val originX: Float = 0f,
    val originY: Float = 0f,
    val originZ: Float = 0f,
    val currentRadius: Float = 0f,
    val maxRadius: Float = 5f,
    val progress: Float = 0f, // 0f..1f
    val color: Color = Color(0xFFCBFF4D),
)

/**
 * Camera 3D state for smooth cinematic orbital navigation and planet focus.
 */
@Immutable
data class CameraState(
    val yaw: Float = 0.0f,              // horizontal orbit angle around origin (degrees)
    val pitch: Float = 25.0f,           // vertical tilt (degrees, 0 = edge on, 90 = top down)
    val distance: Float = 6.2f,         // camera distance from target
    val targetX: Float = 0.0f,          // look-at target position
    val targetY: Float = 0.0f,
    val targetZ: Float = 0.0f,
    val fov: Float = 45.0f,             // field of view
)

/**
 * Immutable universe snapshot driving the 3D renderer and Compose HUD.
 */
@Immutable
data class UniverseState(
    val centralUser: CelestialBody,
    val celestialBodies: List<CelestialBody> = emptyList(),
    val orbitalRings: List<SpatialOrbit> = emptyList(),
    val constellations: List<SpatialConstellation> = emptyList(),
    val activeSignals: List<SpatialSignal> = emptyList(),
    val selectedBodyId: String? = null,
    val cameraState: CameraState = CameraState(),
    val qualityLevel: QualityLevel = QualityLevel.High,
    val isFocusedOnPlanet: Boolean = false,
)
