package com.nexus.core.spatial.model

import androidx.compose.ui.graphics.Color
import com.nexus.data.model.Contact
import kotlin.math.PI
import kotlin.math.abs

/**
 * Deterministically maps domain [Contact]s into [CelestialBody] planets and [SpatialConstellation]s.
 * The same contact ID always produces the exact same planetary visual identity across sessions.
 */
object SpatialContactMapper {

    // Sophisticated cinematic celestial palette conforming to NEXUS AMOLED theme
    private val CelestialPalettes = listOf(
        // Lime Primary (NEXUS Signature)
        Pair(Color(0xFFCBFF4D), Color(0x66CBFF4D)),
        // Cyan / Cold Ice
        Pair(Color(0xFF4DF0FF), Color(0x664DF0FF)),
        // Solar Warm Amber
        Pair(Color(0xFFFFB24D), Color(0x66FFB24D)),
        // Aurora Emerald
        Pair(Color(0xFF4DFF91), Color(0x664DFF91)),
        // Deep Stellar Violet
        Pair(Color(0xFFB072FF), Color(0x66B072FF)),
        // Electric Azure
        Pair(Color(0xFF4D9BFF), Color(0x664D9BFF)),
    )

    fun createUniverse(
        contacts: List<Contact>,
        selectedId: String? = null,
        missedContactIds: Set<String> = emptySet(),
        visitorContact: Contact? = null,
    ): UniverseState {
        val userCore = CelestialBody(
            id = "user_core",
            name = "YOU",
            number = "",
            initials = "U",
            type = BodyType.UserCore,
            orbitRadius = 0f,
            orbitSpeed = 0.05f,
            radius = 0.22f,
            primaryColor = Color(0xFFCBFF4D),
            secondaryColor = Color(0xFF141418),
            atmosphereColor = Color(0x88CBFF4D),
            atmosphereGlow = 0.85f,
            hasRings = true,
            ringRadius = 0.38f,
        )

        // Tiered orbits: Inner (frequent), Mid, Outer (distant)
        val orbitalRings = listOf(
            SpatialOrbit(0, radius = 1.45f, color = Color(0x33CBFF4D), name = "Inner Orbit"),
            SpatialOrbit(1, radius = 2.45f, color = Color(0x22FFFFFF), name = "Mid Orbit"),
            SpatialOrbit(2, radius = 3.55f, color = Color(0x18FFFFFF), name = "Outer Orbit"),
        )

        val bodies = mutableListOf<CelestialBody>()

        val innerCount = minOf(3, contacts.size)
        val midCount = minOf(6, contacts.size) - innerCount
        val outerCount = maxOf(0, contacts.size - innerCount - midCount)
        val tierCounts = intArrayOf(innerCount, midCount, outerCount)

        var rankIndex = 0
        for (tier in 0..2) {
            val count = tierCounts[tier]
            if (count <= 0) continue
            val ringRadius = orbitalRings[tier].radius
            val startAngle = (tier * 0.75f) * (PI.toFloat() / 3f)

            for (i in 0 until count) {
                if (rankIndex >= contacts.size) break
                val contact = contacts[rankIndex]
                val seed = abs(contact.id.hashCode())
                val paletteIndex = seed % CelestialPalettes.size
                val (primary, atmosphere) = CelestialPalettes[paletteIndex]

                val angle = startAngle + (2f * PI.toFloat() * i / count)
                val speed = when (tier) {
                    0 -> 0.12f + (seed % 5) * 0.01f
                    1 -> 0.07f + (seed % 4) * 0.01f
                    else -> 0.04f + (seed % 3) * 0.008f
                }
                val yOffset = ((seed % 7) - 3) * 0.05f

                val planetRadius = when (tier) {
                    0 -> 0.17f
                    1 -> 0.14f
                    else -> 0.11f
                }

                val hasRings = (seed % 3 == 0) || contact.isFavorite
                val isEclipse = missedContactIds.contains(contact.id)

                val initials = contact.name.trim().split(" ")
                    .mapNotNull { it.firstOrNull()?.toString() }
                    .take(2).joinToString("").uppercase()

                // Mock Communication DNA (orbital events around this planet)
                val dna = listOf(
                    SpatialEvent(
                        id = "${contact.id}_ev1",
                        label = "Incoming Call",
                        detail = "4m 12s",
                        isIncoming = true,
                        isMissed = false,
                        timestampMillis = contact.lastInteractionMillis ?: System.currentTimeMillis(),
                        angle = 0.4f,
                        distance = planetRadius * 1.8f
                    ),
                    SpatialEvent(
                        id = "${contact.id}_ev2",
                        label = "Outgoing Call",
                        detail = "12m 45s",
                        isIncoming = false,
                        isMissed = false,
                        timestampMillis = (contact.lastInteractionMillis ?: System.currentTimeMillis()) - 86400000L,
                        angle = 2.1f,
                        distance = planetRadius * 2.3f
                    ),
                    SpatialEvent(
                        id = "${contact.id}_ev3",
                        label = if (isEclipse) "Missed Call (Eclipse)" else "Signal",
                        detail = if (isEclipse) "Unanswered" else "1m 30s",
                        isIncoming = true,
                        isMissed = isEclipse,
                        timestampMillis = (contact.lastInteractionMillis ?: System.currentTimeMillis()) - 172800000L,
                        angle = 4.2f,
                        distance = planetRadius * 2.8f
                    )
                )

                bodies.add(
                    CelestialBody(
                        id = contact.id,
                        name = contact.name,
                        number = contact.number,
                        initials = initials.ifEmpty { "C" },
                        type = BodyType.Planet,
                        rank = rankIndex,
                        orbitRadius = ringRadius,
                        orbitSpeed = speed,
                        baseAngle = angle,
                        yOffset = yOffset,
                        radius = planetRadius,
                        primaryColor = if (isEclipse) Color(0xFF6E4040) else primary,
                        secondaryColor = Color(0xFF0F1115),
                        atmosphereColor = if (isEclipse) Color(0x33FF5A4D) else atmosphere,
                        atmosphereGlow = if (isEclipse) 0.2f else 0.6f,
                        hasRings = hasRings,
                        ringRadius = planetRadius * 2.2f,
                        isFavorite = contact.isFavorite,
                        isEclipse = isEclipse,
                        isSelected = contact.id == selectedId,
                        recentActivityText = contact.subtitle,
                        communicationDna = dna,
                    )
                )
                rankIndex++
            }
        }

        // Add visitor planet if present (e.g. unknown caller)
        if (visitorContact != null) {
            bodies.add(
                CelestialBody(
                    id = visitorContact.id,
                    name = visitorContact.name.ifEmpty { "VISITOR" },
                    number = visitorContact.number,
                    initials = "?",
                    type = BodyType.Visitor,
                    rank = 99,
                    orbitRadius = 4.4f,
                    orbitSpeed = 0.09f,
                    baseAngle = 1.2f,
                    yOffset = 0.4f,
                    radius = 0.13f,
                    primaryColor = Color(0xFFFF994D),
                    secondaryColor = Color(0xFF221100),
                    atmosphereColor = Color(0x66FF994D),
                    atmosphereGlow = 0.8f,
                    hasRings = false,
                    isVisitor = true,
                    isSelected = visitorContact.id == selectedId
                )
            )
        }

        // Deterministic Constellations (e.g. Family & Core Work groups)
        val constellations = mutableListOf<SpatialConstellation>()
        val familyMembers = listOf("mom", "dad", "sister").filter { fid -> contacts.any { it.id == fid } }
        if (familyMembers.size >= 2) {
            constellations.add(
                SpatialConstellation(
                    id = "constellation_family",
                    name = "FAMILY",
                    memberIds = familyMembers,
                    color = Color(0x55CBFF4D)
                )
            )
        }

        val workMembers = listOf("rahul", "amit", "priya").filter { wid -> contacts.any { it.id == wid } }
        if (workMembers.size >= 2) {
            constellations.add(
                SpatialConstellation(
                    id = "constellation_core",
                    name = "CORE",
                    memberIds = workMembers,
                    color = Color(0x554DF0FF)
                )
            )
        }

        return UniverseState(
            centralUser = userCore,
            celestialBodies = bodies,
            orbitalRings = orbitalRings,
            constellations = constellations,
            selectedBodyId = selectedId,
            isFocusedOnPlanet = selectedId != null,
        )
    }
}
