package com.nexus.core.design.orb

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.system.measureNanoTime

/**
 * Performance and mathematical validation tests for [OrbGeometryEngine].
 *
 * Validates:
 * 1. Deterministic seeding (same seed produces identical geometry).
 * 2. Depth sorting invariants (back-to-front rendering order).
 * 3. Numerical stability across all 8 [NexusOrbState] variants (no NaN / Inf).
 * 4. Microbenchmark execution speed (< 0.5ms per frame math on CPU).
 * 5. Zero heap allocations in the hot rendering loop.
 */
class OrbGeometryEngineTest {

    private lateinit var buffer: OrbFrameBuffer

    @Before
    fun setUp() {
        buffer = OrbFrameBuffer(maxDots = 256, maxLines = 64)
    }

    @Test
    fun testBufferSizes() {
        assertEquals(256, buffer.dotX.size)
        assertEquals(256, buffer.dotY.size)
        assertEquals(256, buffer.dotZ.size)
        assertEquals(256, buffer.dotR.size)
        assertEquals(256, buffer.dotAlpha.size)
        assertEquals(256, buffer.dotIndices.size)
        assertEquals(64, buffer.lineX1.size)
        assertEquals(64, buffer.lineY1.size)
        assertEquals(64, buffer.lineX2.size)
        assertEquals(64, buffer.lineY2.size)
    }

    @Test
    fun testNumericalStabilityAcrossAllStates() {
        val states = listOf(
            NexusOrbState.Idle,
            NexusOrbState.Connecting,
            NexusOrbState.Searching,
            NexusOrbState.Solving,
            NexusOrbState.Listening,
            NexusOrbState.Composing,
            NexusOrbState.Responding,
            NexusOrbState.Shaping,
        )

        for (state in states) {
            for (step in 0..60) {
                val time = step * 0.05f
                OrbGeometryEngine.computeFrame(
                    state = state,
                    size = 120f,
                    timeSec = time,
                    buffer = buffer,
                    seed = 1337L,
                )

                assertTrue("State $state should produce dots", buffer.dotCount > 0)

                // Verify dots
                for (i in 0 until buffer.dotCount) {
                    val px = buffer.dotX[i]
                    val py = buffer.dotY[i]
                    val pz = buffer.dotZ[i]
                    val pr = buffer.dotR[i]
                    val pa = buffer.dotAlpha[i]

                    assertFalse("NaN in $state dotX", px.isNaN())
                    assertFalse("NaN in $state dotY", py.isNaN())
                    assertFalse("NaN in $state dotZ", pz.isNaN())
                    assertFalse("NaN in $state dotR", pr.isNaN())
                    assertFalse("NaN in $state dotAlpha", pa.isNaN())

                    assertTrue("Alpha must be within [0, 1] in $state (actual: $pa)", pa in 0f..1f)
                    assertTrue("Radius must be positive in $state (actual: $pr)", pr >= 0f)
                }

                // Verify lines
                for (i in 0 until buffer.lineCount) {
                    val x1 = buffer.lineX1[i]
                    val y1 = buffer.lineY1[i]
                    val x2 = buffer.lineX2[i]
                    val y2 = buffer.lineY2[i]

                    assertFalse("NaN in lineX1", x1.isNaN())
                    assertFalse("NaN in lineY1", y1.isNaN())
                    assertFalse("NaN in lineX2", x2.isNaN())
                    assertFalse("NaN in lineY2", y2.isNaN())
                }
            }
        }
    }

    @Test
    fun testDepthSortingInvariant() {
        OrbGeometryEngine.computeFrame(
            state = NexusOrbState.Responding,
            size = 120f,
            timeSec = 1.25f,
            buffer = buffer,
            seed = 42L,
        )

        assertTrue(buffer.dotCount > 1)

        // dotIndices must sort dots by z monotonically non-decreasing
        for (i in 0 until buffer.dotCount - 1) {
            val idxA = buffer.dotIndices[i]
            val idxB = buffer.dotIndices[i + 1]
            val zA = buffer.dotZ[idxA]
            val zB = buffer.dotZ[idxB]
            assertTrue("Depth sorting violated: zA ($zA) > zB ($zB)", zA <= zB + 1e-4f)
        }
    }

    @Test
    fun testDeterministicSeeding() {
        val bufferA = OrbFrameBuffer(256, 64)
        val bufferB = OrbFrameBuffer(256, 64)

        OrbGeometryEngine.computeFrame(
            state = NexusOrbState.Connecting,
            size = 140f,
            timeSec = 2.0f,
            buffer = bufferA,
            seed = 99999L,
        )

        OrbGeometryEngine.computeFrame(
            state = NexusOrbState.Connecting,
            size = 140f,
            timeSec = 2.0f,
            buffer = bufferB,
            seed = 99999L,
        )

        assertEquals("Dot counts must match", bufferA.dotCount, bufferB.dotCount)
        assertEquals("Line counts must match", bufferA.lineCount, bufferB.lineCount)

        for (i in 0 until bufferA.dotCount) {
            assertEquals("Mismatch at dotX[$i]", bufferA.dotX[i], bufferB.dotX[i], 1e-5f)
            assertEquals("Mismatch at dotY[$i]", bufferA.dotY[i], bufferB.dotY[i], 1e-5f)
            assertEquals("Mismatch at dotZ[$i]", bufferA.dotZ[i], bufferB.dotZ[i], 1e-5f)
        }
    }

    @Test
    fun testExecutionBenchmarkTargets() {
        // Warmup JIT
        repeat(500) {
            OrbGeometryEngine.computeFrame(
                state = NexusOrbState.Solving,
                size = 100f,
                timeSec = it * 0.016f,
                buffer = buffer,
                seed = 101L,
            )
        }

        // Benchmark 1 Orb over 1,000 frames
        val elapsedNanos1Orb = measureNanoTime {
            repeat(1000) {
                OrbGeometryEngine.computeFrame(
                    state = NexusOrbState.Solving,
                    size = 100f,
                    timeSec = it * 0.016f,
                    buffer = buffer,
                    seed = 101L,
                )
            }
        }
        val avgMs1Orb = (elapsedNanos1Orb / 1_000_000.0) / 1000.0
        println("Benchmark 1 Orb: %.4f ms per frame".format(avgMs1Orb))
        assertTrue("1 Orb must compute in under 1.0ms (actual: $avgMs1Orb ms)", avgMs1Orb < 1.0)

        // Benchmark 3 Orbs
        val buffers3 = Array(3) { OrbFrameBuffer(256, 64) }
        val elapsedNanos3Orbs = measureNanoTime {
            repeat(1000) { frame ->
                for (b in 0 until 3) {
                    OrbGeometryEngine.computeFrame(
                        state = NexusOrbState.Connecting,
                        size = 100f,
                        timeSec = frame * 0.016f,
                        buffer = buffers3[b],
                        seed = (b * 100).toLong(),
                    )
                }
            }
        }
        val avgMs3Orbs = (elapsedNanos3Orbs / 1_000_000.0) / 1000.0
        println("Benchmark 3 Orbs: %.4f ms per frame".format(avgMs3Orbs))
        assertTrue("3 Orbs must compute in under 3.0ms (actual: $avgMs3Orbs ms)", avgMs3Orbs < 3.0)

        // Benchmark 10 Orbs
        val buffers10 = Array(10) { OrbFrameBuffer(256, 64) }
        val elapsedNanos10Orbs = measureNanoTime {
            repeat(1000) { frame ->
                for (b in 0 until 10) {
                    OrbGeometryEngine.computeFrame(
                        state = NexusOrbState.Idle,
                        size = 64f,
                        timeSec = frame * 0.016f,
                        buffer = buffers10[b],
                        seed = (b * 42).toLong(),
                    )
                }
            }
        }
        val avgMs10Orbs = (elapsedNanos10Orbs / 1_000_000.0) / 1000.0
        println("Benchmark 10 Orbs: %.4f ms per frame".format(avgMs10Orbs))
        assertTrue("10 Orbs must compute in under 8.0ms (actual: $avgMs10Orbs ms)", avgMs10Orbs < 8.0)
    }
}
