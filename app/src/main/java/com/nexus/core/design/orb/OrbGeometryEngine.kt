package com.nexus.core.design.orb

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/*
 * Portions of the mathematical models and geometry generators in this file
 * are ported from thinking-orbs by Jakub Antalik (https://github.com/Jakubantalik/thinking-orbs)
 *
 * MIT License
 * Copyright (c) 2026 Jakub Antalik
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

/**
 * High-performance zero-allocation frame buffer for [NexusOrb].
 * Reuses primitive arrays across frames to completely eliminate heap allocations in the draw loop.
 */
class OrbFrameBuffer(
    val maxDots: Int = 750,
    val maxLines: Int = 260,
) {
    var dotCount = 0
    val dotX = FloatArray(maxDots)
    val dotY = FloatArray(maxDots)
    val dotZ = FloatArray(maxDots)
    val dotR = FloatArray(maxDots)
    val dotWhite = FloatArray(maxDots)
    val dotAlpha = FloatArray(maxDots)
    val dotIndices = IntArray(maxDots)

    var lineCount = 0
    val lineX1 = FloatArray(maxLines)
    val lineY1 = FloatArray(maxLines)
    val lineX2 = FloatArray(maxLines)
    val lineY2 = FloatArray(maxLines)
    val lineWhite = FloatArray(maxLines)
    val lineAlpha = FloatArray(maxLines)
    val lineW = FloatArray(maxLines)

    fun clear() {
        dotCount = 0
        lineCount = 0
    }

    fun addDot(x: Float, y: Float, z: Float, r: Float, white: Float, alpha: Float, rMin: Float = 0.3f) {
        if (alpha < 0.02f || dotCount >= maxDots) return
        val i = dotCount++
        dotX[i] = x
        dotY[i] = y
        dotZ[i] = z
        dotR[i] = max(rMin, r)
        dotWhite[i] = white.coerceIn(0f, 1f)
        dotAlpha[i] = alpha.coerceIn(0f, 1f)
        dotIndices[i] = i
    }

    fun addLine(
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        white: Float,
        alpha: Float,
        w: Float,
    ) {
        if (alpha < 0.02f || lineCount >= maxLines) return
        val i = lineCount++
        lineX1[i] = x1
        lineY1[i] = y1
        lineX2[i] = x2
        lineY2[i] = y2
        lineWhite[i] = white.coerceIn(0f, 1f)
        lineAlpha[i] = alpha.coerceIn(0f, 1f)
        lineW[i] = w
    }

    /**
     * In-place insertion sort of dot indices by Z-depth ascending (far to near).
     * Extremely fast for partially sorted collections with zero memory allocation.
     */
    fun sortDotsByDepth() {
        for (i in 1 until dotCount) {
            val key = dotIndices[i]
            val keyZ = dotZ[key]
            var j = i - 1
            while (j >= 0 && dotZ[dotIndices[j]] > keyZ) {
                dotIndices[j + 1] = dotIndices[j]
                j--
            }
            dotIndices[j + 1] = key
        }
    }
}

/**
 * Pure math geometry engine ported from Jakub Antalik's thinking-orbs.
 */
object OrbGeometryEngine {

    private const val TWO_PI = (PI * 2.0).toFloat()
    private const val HALF_PI = (PI * 0.5).toFloat()
    private const val GOLDEN_RATIO = (PI * (3.0 - 2.23606797749979)).toFloat() // PI * (3 - sqrt(5))

    fun lerp(a: Float, b: Float, f: Float): Float = a + (b - a) * f

    fun frac(x: Float): Float = x - floor(x)

    fun hashD(a: Float, b: Float): Float {
        val h = sin(a * 12.9898f + b * 78.233f) * 43758.5453f
        return h - floor(h)
    }

    fun vnoise(x: Float, y: Float): Float {
        val xi = floor(x)
        val yi = floor(y)
        var fx = x - xi
        var fy = y - yi
        fx = fx * fx * (3f - 2f * fx)
        fy = fy * fy * (3f - 2f * fy)
        val a = hashD(xi, yi)
        val b = hashD(xi + 1f, yi)
        val c = hashD(xi, yi + 1f)
        val d = hashD(xi + 1f, yi + 1f)
        return a + (b - a) * fx + (c - a) * fy + (a - b - c + d) * fx * fy
    }

    fun fibDir(i: Int, n: Int, out: FloatArray) {
        val y = 1f - (2f * (i + 0.5f)) / n
        val rad = sqrt(max(0f, 1f - y * y))
        val a = i * GOLDEN_RATIO
        out[0] = rad * cos(a)
        out[1] = y
        out[2] = rad * sin(a)
    }

    fun angleDelta(a: Float, b: Float): Float {
        return atan2(sin(a - b), cos(a - b))
    }

    fun radiusScale(size: Float, pow: Float = 0.6f): Float {
        return Math.pow((size / 300.0), pow.toDouble()).toFloat()
    }

    // --- Mode Presets & Scaling Helpers ---
    private fun scaleCount(base: Int, scale: Float): Int = max(1, (base * scale).toInt())
    private fun scaleCountPair(base: Int, scale: Float): Int = max(2, (base * sqrt(scale)).toInt())

    /**
     * Evaluates a frame for the specified [state] and renders into [buffer].
     */
    fun computeFrame(
        state: NexusOrbState,
        size: Float,
        timeSec: Float,
        buffer: OrbFrameBuffer,
        seed: Long = 0L,
    ) {
        buffer.clear()
        val seedOffset = if (seed == 0L) 0f else (seed % 1000).toFloat() * 0.037f
        val t = timeSec + seedOffset

        when (state) {
            NexusOrbState.Idle -> computeRing(size, t * 3.24f, buffer, seedOffset)
            NexusOrbState.Connecting -> computeWeb(size, t * 3.315f, buffer, seedOffset)
            NexusOrbState.Searching -> computeGlobe(size, t * 2.015f, buffer, seedOffset)
            NexusOrbState.Solving -> computeRubik(size, t * 1.82f, buffer, seedOffset)
            NexusOrbState.Listening -> computeWave(size, t * 4.388f, buffer, seedOffset)
            NexusOrbState.Composing -> computeRibbon(size, t * 2.34f, buffer, seedOffset)
            NexusOrbState.Responding -> computeOrbits(size, t * 1.885f, buffer, seedOffset)
            NexusOrbState.Shaping -> computeMorph(size, t * 2.405f, buffer, seedOffset)
        }

        buffer.sortDotsByDepth()
    }

    // --- 1. Orbits (Responding / Active Communication) ---
    private fun computeOrbits(size: Float, t: Float, buffer: OrbFrameBuffer, seedOffset: Float) {
        val cx = size * 0.5f
        val cy = size * 0.5f
        val radius = size * 0.5f * 0.82f
        val rs = radiusScale(size, 0.6f)

        // Interpolate count for size (between 64px scale 1.0 and 20px scale 0.238)
        val sizeFactor = (size / 64f).coerceIn(0.3f, 1.5f)
        val orbitN = max(4, (12 * sizeFactor).toInt())
        val ghostN = max(12, (40 * sizeFactor).toInt())
        val particles = 3

        val yaw = t * 0.12f + seedOffset * 0.2f
        val tilt = 0.3f
        val st = sin(tilt)
        val ct = cos(tilt)
        val sy = sin(yaw)
        val cyw = cos(yaw)

        for (orb in 0 until orbitN) {
            val h1 = hashD(orb.toFloat() + seedOffset, 1.7f)
            val h2 = hashD(orb.toFloat() + seedOffset, 5.2f)
            val h3 = hashD(orb.toFloat() + seedOffset, 8.9f)
            val ro = radius * (0.45f + 0.52f * h1)
            val th = h1 * TWO_PI
            val phi = acos((2f * h2 - 1f).coerceIn(-1f, 1f))

            val nx = sin(phi) * cos(th)
            val ny = cos(phi)
            val nz = sin(phi) * sin(th)
            var ux = -ny
            var uy = nx
            val uz = 0f
            val ul = max(1e-6f, sqrt(ux * ux + uy * uy))
            ux /= ul
            uy /= ul
            val vx = ny * uz - nz * uy
            val vy = nz * ux - nx * uz
            val vz = nx * uy - ny * ux
            val speed = (0.25f + 0.55f * h3) * if (h3 > 0.5f) 1f else -1f

            // Ghost orbit rail dots
            val ghostStep = max(1, (ghostN / (24 * sizeFactor)).toInt())
            var k = 0
            while (k < ghostN) {
                val a = (k.toFloat() / ghostN) * TWO_PI
                val x = (ux * cos(a) + vx * sin(a)) * ro
                val y = (uy * cos(a) + vy * sin(a)) * ro
                val z = (uz * cos(a) + vz * sin(a)) * ro

                // 3D projection
                val x1 = x * cyw + z * sy
                val z1 = -x * sy + z * cyw
                val y1 = y * ct - z1 * st
                val z2 = y * st + z1 * ct
                val px = cx + x1
                val py = cy - y1
                val depth = (z2 / ro + 1f) * 0.5f

                buffer.addDot(
                    x = px,
                    y = py,
                    z = z2,
                    r = 0.9f * rs,
                    white = 0.72f,
                    alpha = 0.5f * (0.4f + 0.6f * depth),
                )
                k += ghostStep
            }

            // High-energy active particle heads
            for (m in 0 until particles) {
                val a = t * speed + (m.toFloat() / particles) * TWO_PI + h2 * 6f
                val x = (ux * cos(a) + vx * sin(a)) * ro
                val y = (uy * cos(a) + vy * sin(a)) * ro
                val z = (uz * cos(a) + vz * sin(a)) * ro

                val x1 = x * cyw + z * sy
                val z1 = -x * sy + z * cyw
                val y1 = y * ct - z1 * st
                val z2 = y * st + z1 * ct
                val px = cx + x1
                val py = cy - y1
                val depth = (z2 / ro + 1f) * 0.5f

                buffer.addDot(
                    x = px,
                    y = py,
                    z = z2,
                    r = (1.2f + 1.6f * depth) * rs,
                    white = 0.3f - 0.22f * depth,
                    alpha = 0.85f + 0.15f * depth,
                )
            }
        }
    }

    // --- 2. Web (Connecting / Signal Establishing) ---
    private fun computeWeb(size: Float, t: Float, buffer: OrbFrameBuffer, seedOffset: Float) {
        val cx = size * 0.5f
        val cy = size * 0.5f
        val radius = size * 0.5f * 0.8f
        val rs = radiusScale(size, 0.6f)

        val sizeFactor = (size / 64f).coerceIn(0.4f, 1.4f)
        val nodeN = max(10, (30 * sizeFactor).toInt())
        val thr = 0.72f
        val nodeR = 1.4f
        val nodeRDepth = 1.8f
        val signals = max(2, (5 * sizeFactor).toInt())

        val yaw = t * 0.12f
        val tilt = 0.32f
        val st = sin(tilt)
        val ct = cos(tilt)
        val sy = sin(yaw)
        val cyw = cos(yaw)

        // Nodes wandering on unit sphere
        val nodePos = Array(nodeN) { FloatArray(3) }
        val projPos = Array(nodeN) { FloatArray(3) }
        val tempFib = FloatArray(3)

        for (i in 0 until nodeN) {
            fibDir(i, nodeN, tempFib)
            val wx = tempFib[0] + 0.3f * (vnoise(i * 0.31f + 9f + seedOffset, t * 0.24f) - 0.5f) * 2f
            val wy = tempFib[1] + 0.3f * (vnoise(i * 0.53f + 27f + seedOffset, t * 0.21f) - 0.5f) * 2f
            val wz = tempFib[2] + 0.3f * (vnoise(i * 0.77f + 55f + seedOffset, t * 0.27f) - 0.5f) * 2f
            val len = max(1e-6f, sqrt(wx * wx + wy * wy + wz * wz))
            nodePos[i][0] = wx / len
            nodePos[i][1] = wy / len
            nodePos[i][2] = wz / len

            // Project node
            val x = nodePos[i][0] * radius
            val y = nodePos[i][1] * radius
            val z = nodePos[i][2] * radius
            val x1 = x * cyw + z * sy
            val z1 = -x * sy + z * cyw
            val y1 = y * ct - z1 * st
            val z2 = y * st + z1 * ct
            projPos[i][0] = cx + x1
            projPos[i][1] = cy - y1
            projPos[i][2] = z2
        }

        // Draw proximity edges
        for (i in 0 until nodeN) {
            for (j in i + 1 until nodeN) {
                val dx = nodePos[i][0] - nodePos[j][0]
                val dy = nodePos[i][1] - nodePos[j][1]
                val dz = nodePos[i][2] - nodePos[j][2]
                val dist = sqrt(dx * dx + dy * dy + dz * dz)
                if (dist < thr) {
                    val depth = (((projPos[i][2] + projPos[j][2]) * 0.5f) / radius + 1f) * 0.5f
                    buffer.addLine(
                        x1 = projPos[i][0],
                        y1 = projPos[i][1],
                        x2 = projPos[j][0],
                        y2 = projPos[j][1],
                        white = 0.42f,
                        alpha = (1f - dist / thr) * (0.3f + 0.55f * depth),
                        w = max(0.6f, 0.8f * rs),
                    )
                }
            }
        }

        // Draw nodes
        for (i in 0 until nodeN) {
            val depth = (projPos[i][2] / radius + 1f) * 0.5f
            val pulse = 1f + 0.25f * sin(t * 1.4f + i * 2.7f)
            buffer.addDot(
                x = projPos[i][0],
                y = projPos[i][1],
                z = projPos[i][2],
                r = (nodeR + nodeRDepth * depth) * pulse * rs,
                white = 0.55f - 0.45f * depth,
                alpha = 0.6f + 0.4f * depth,
            )
        }

        // Signal packets running across nodes
        for (s in 0 until signals) {
            val seg = floor(t * 0.55f + s * 7.31f + seedOffset)
            val a = (hashD(seg, s * 3.1f + 1.7f) * nodeN).toInt().coerceIn(0, nodeN - 1)
            val b = (hashD(seg, s * 5.7f + 4.2f) * nodeN).toInt().coerceIn(0, nodeN - 1)
            if (a != b) {
                val f = frac(t * 0.55f + s * 7.31f + seedOffset)
                val nx = lerp(nodePos[a][0], nodePos[b][0], f)
                val ny = lerp(nodePos[a][1], nodePos[b][1], f)
                val nz = lerp(nodePos[a][2], nodePos[b][2], f)
                val nl = max(1e-6f, sqrt(nx * nx + ny * ny + nz * nz))
                val x = (nx / nl) * radius
                val y = (ny / nl) * radius
                val z = (nz / nl) * radius

                val x1 = x * cyw + z * sy
                val z1 = -x * sy + z * cyw
                val y1 = y * ct - z1 * st
                val z2 = y * st + z1 * ct
                val depth = (z2 / radius + 1f) * 0.5f

                buffer.addDot(
                    x = cx + x1,
                    y = cy - y1,
                    z = z2,
                    r = (nodeR * 1.5f + nodeRDepth * depth) * rs,
                    white = 0.05f,
                    alpha = 0.5f + 0.5f * depth,
                )
            }
        }
    }

    // --- 3. Globe (Searching / Contact Resolution) ---
    private fun computeGlobe(size: Float, t: Float, buffer: OrbFrameBuffer, seedOffset: Float) {
        val spin = 0.5f
        val cx = size * 0.5f
        val cy = size * 0.5f
        val radius = size * 0.5f * 0.82f
        val tilt = 0.4f + 0.06f * sin(t * 0.35f)
        val scan = t * (spin + (1.7f - spin) * 4.08f)
        val rs = radiusScale(size, 0.6f)

        val sizeFactor = (size / 64f).coerceIn(0.4f, 1.4f)
        val latRings = max(6, (17 * sqrt(sizeFactor * 0.42f)).toInt())
        val lonDensity = max(12, (44 * sqrt(sizeFactor * 0.42f)).toInt())

        val st = sin(tilt)
        val ct = cos(tilt)
        val yaw = t * spin + seedOffset
        val sy = sin(yaw)
        val cyw = cos(yaw)

        for (li in 0..latRings) {
            val lat = -HALF_PI + (li.toFloat() / latRings) * PI.toFloat()
            val cosLat = cos(lat)
            val sinLat = sin(lat)
            val lonCount = max(1, (abs(cosLat) * lonDensity).toInt())
            for (lj in 0 until lonCount) {
                val lon = (lj.toFloat() / lonCount) * TWO_PI
                val lx = cosLat * cos(lon) * radius
                val ly = sinLat * radius
                val lz = cosLat * sin(lon) * radius

                val x1 = lx * cyw + lz * sy
                val z1 = -lx * sy + lz * cyw
                val y1 = ly * ct - z1 * st
                val z2 = ly * st + z1 * ct
                val depth = (z2 / radius + 1f) * 0.5f

                val d = angleDelta(lon + t * spin, scan)
                val boost = exp(-(d * d) / 0.18f) * max(0f, z2 / radius)
                buffer.addDot(
                    x = cx + x1,
                    y = cy - y1,
                    z = z2,
                    r = (0.6f + 1.7f * depth + 1.0f * boost) * rs,
                    white = 0.62f - 0.54f * depth,
                    alpha = 0.45f + 0.55f * min(1f, boost),
                )
            }
        }
    }

    // --- 4. Rubik (Solving / Graph Analysis) ---
    private fun computeRubik(size: Float, t: Float, buffer: OrbFrameBuffer, seedOffset: Float) {
        val cx = size * 0.5f
        val cy = size * 0.5f
        val radius = size * 0.5f * 0.82f
        val rs = radiusScale(size, 0.6f)
        val moveCount = 14

        // Scramble / solve cycle
        val slotDur = 0.42f
        val rest = 1.2f
        val cyc = 2 * moveCount * slotDur + rest
        val tc = (t + seedOffset) % cyc
        val amounts = FloatArray(moveCount)
        var activeMove = -1
        if (tc < 2 * moveCount * slotDur) {
            val slot = (tc / slotDur).toInt()
            val p = (tc - slot * slotDur) / slotDur
            val cl = min(1f, p / 0.7f)
            val ep = 1f - (1f - cl) * (1f - cl) * (1f - cl)
            if (slot < moveCount) {
                for (i in 0 until slot) amounts[i] = 1f
                amounts[slot] = ep
                activeMove = slot
            } else {
                val u = 2 * moveCount - 1 - slot
                for (i in 0 until u) amounts[i] = 1f
                amounts[u] = 1f - ep
                activeMove = u
            }
        }

        val yaw = t * 0.55f
        val tilt = 0.35f + 0.1f * sin(t * 0.9f)
        val st = sin(tilt)
        val ct = cos(tilt)
        val sy = sin(yaw)
        val cyw = cos(yaw)

        val sizeFactor = (size / 64f).coerceIn(0.4f, 1.4f)
        val latRings = max(6, (15 * sqrt(sizeFactor * 0.35f)).toInt())
        val lonDensity = max(12, (40 * sqrt(sizeFactor * 0.35f)).toInt())

        for (li in 0..latRings) {
            val lat = -HALF_PI + (li.toFloat() / latRings) * PI.toFloat()
            val cosLat = cos(lat)
            val sinLat = sin(lat)
            val lonCount = max(1, (abs(cosLat) * lonDensity).toInt())
            for (lj in 0 until lonCount) {
                val lon = (lj.toFloat() / lonCount) * TWO_PI
                var x = cosLat * cos(lon)
                var y = sinLat
                var z = cosLat * sin(lon)
                var inActive = false

                // Apply quarter turns
                for (i in 0 until moveCount) {
                    if (amounts[i] <= 0f) continue
                    val axis = min(2, (hashD(i.toFloat(), 2.3f) * 3f).toInt())
                    val lo = -1.0f + 0.5f * min(3, (hashD(i.toFloat(), 5.9f) * 4f).toInt())
                    val hi = lo + 0.5f
                    val dir = if (hashD(i.toFloat(), 7.7f) < 0.5f) 1f else -1f
                    val coord = if (axis == 0) x else if (axis == 1) y else z
                    if (coord >= lo && coord < hi) {
                        if (i == activeMove) inActive = true
                        val a = (dir * HALF_PI) * amounts[i]
                        val ca = cos(a)
                        val sa = sin(a)
                        if (axis == 0) {
                            val y2 = y * ca - z * sa
                            z = y * sa + z * ca
                            y = y2
                        } else if (axis == 1) {
                            val x2 = x * ca + z * sa
                            z = -x * sa + z * ca
                            x = x2
                        } else {
                            val x2 = x * ca - y * sa
                            y = x * sa + y * ca
                            x = x2
                        }
                    }
                }

                val sx = x * radius
                val syPos = y * radius
                val sz = z * radius
                val x1 = sx * cyw + sz * sy
                val z1 = -sx * sy + sz * cyw
                val y1 = syPos * ct - z1 * st
                val z2 = syPos * st + z1 * ct
                val depth = (z2 / radius + 1f) * 0.5f

                buffer.addDot(
                    x = cx + x1,
                    y = cy - y1,
                    z = z2,
                    r = (0.6f + 1.7f * depth + (if (inActive) 0.3f else 0f)) * rs,
                    white = 0.62f - 0.54f * depth - (if (inActive) 0.14f else 0f),
                    alpha = 0.6f + 0.4f * depth,
                )
            }
        }
    }

    // --- 5. Wave (Listening / Audio Transmissions) ---
    private fun computeWave(size: Float, t: Float, buffer: OrbFrameBuffer, seedOffset: Float) {
        val cx = size * 0.5f
        val cy = size * 0.5f
        val radius = size * 0.5f * 0.874f
        val rs = radiusScale(size, 0.6f)

        val yaw = t * 0.18f + seedOffset
        val tilt = 0.38f
        val st = sin(tilt)
        val ct = cos(tilt)
        val sy = sin(yaw)
        val cyw = cos(yaw)

        val sizeFactor = (size / 64f).coerceIn(0.4f, 1.4f)
        val rings = max(6, (15 * sqrt(sizeFactor * 0.341f)).toInt())
        val lonDensity = max(12, (40 * sqrt(sizeFactor * 0.341f)).toInt())

        for (ri in 0..rings) {
            val lat = -HALF_PI + (ri.toFloat() / rings) * PI.toFloat()
            val cosLat = cos(lat)
            val sinLat = sin(lat)
            val w = 0.62f * sin(t * 2.1f - ri * 0.52f) + 0.38f * sin(t * 1.27f + ri * 0.83f)
            val rr = radius * (0.88f + 0.105f * w)
            val lonCount = max(1, (abs(cosLat) * lonDensity).toInt())
            for (lj in 0 until lonCount) {
                val lon = (lj.toFloat() / lonCount) * TWO_PI
                val wx = cosLat * cos(lon) * rr
                val wy = sinLat * rr
                val wz = cosLat * sin(lon) * rr

                val x1 = wx * cyw + wz * sy
                val z1 = -wx * sy + wz * cyw
                val y1 = wy * ct - z1 * st
                val z2 = wy * st + z1 * ct
                val depth = (z2 / radius + 1f) * 0.5f
                val crest = max(0f, w)

                buffer.addDot(
                    x = cx + x1,
                    y = cy - y1,
                    z = z2,
                    r = (0.6f + 1.7f * depth) * (1f + 0.4f * crest) * rs,
                    white = 0.66f - 0.56f * depth - 0.1f * crest,
                    alpha = 0.5f + 0.5f * depth,
                )
            }
        }
    }

    // --- 6. Ribbon (Composing) & Ring (Idle / Breathing) ---
    private fun computeRibbon(size: Float, t: Float, buffer: OrbFrameBuffer, seedOffset: Float) {
        computeRibbonInternal(size, t, buffer, seedOffset, faceOn = false, wobMul = 1.0f, bandMul = 3.9f)
    }

    private fun computeRing(size: Float, t: Float, buffer: OrbFrameBuffer, seedOffset: Float) {
        computeRibbonInternal(size, t, buffer, seedOffset, faceOn = true, wobMul = 0.368f, bandMul = 3.627f)
    }

    private fun computeRibbonInternal(
        size: Float,
        t: Float,
        buffer: OrbFrameBuffer,
        seedOffset: Float,
        faceOn: Boolean,
        wobMul: Float,
        bandMul: Float,
    ) {
        val cx = size * 0.5f
        val cy = size * 0.5f
        val radius = size * 0.5f * 0.78f
        val rs = radiusScale(size, 0.6f)
        val camTilt = 0.3f

        val ya = 0f
        val ta = if (faceOn) -camTilt else (0.55f + 0.3f * sin(t * 0.18f))
        val ux = cos(ya)
        val uy = 0f
        val uz = sin(ya)
        val vx = -uz * sin(ta)
        val vy = cos(ta)
        val vz = ux * sin(ta)
        val nx = uy * vz - uz * vy
        val ny = uz * vx - ux * vz
        val nz = ux * vy - uy * vx

        val wobAmp = 0.23f * wobMul
        val baseR = if (faceOn) radius / (1f + 0.85f * wobAmp) else radius

        val sizeFactor = (size / 64f).coerceIn(0.4f, 1.4f)
        val lanes = max(1, (5 * bandMul * sqrt(sizeFactor * 0.25f)).toInt())
        val segs = max(24, (88 * sqrt(sizeFactor * 0.25f)).toInt())

        val st = sin(camTilt)
        val ct = cos(camTilt)

        for (w in 0 until lanes) {
            val laneOff = (w - (lanes - 1) * 0.5f) * 0.075f
            val edge = abs(w - (lanes - 1) * 0.5f) / max(1f, (lanes - 1) * 0.5f)
            for (k in 0 until segs) {
                val a = (k.toFloat() / segs) * TWO_PI
                val wob = (0.16f * sin(a * 3f - t * 1.7f + w * 0.22f + seedOffset) +
                    0.07f * sin(a * 5f + t * 1.1f)) * wobMul
                val radial = if (faceOn) 1f + wob else 1f
                val off = if (faceOn) laneOff else laneOff + wob

                val x = ux * cos(a) + vx * sin(a) + nx * off
                val y = uy * cos(a) + vy * sin(a) + ny * off
                val z = uz * cos(a) + vz * sin(a) + nz * off
                val len = sqrt(x * x + y * y + z * z)
                val rr = baseR * radial

                val sx = (x / len) * rr
                val sy = (y / len) * rr
                val sz = (z / len) * rr

                val x1 = sx
                val z1 = sz
                val y1 = sy * ct - z1 * st
                val z2 = sy * st + z1 * ct
                val depth = (z2 / radius + 1f) * 0.5f

                buffer.addDot(
                    x = cx + x1,
                    y = cy - y1,
                    z = z2,
                    r = (1.1f + 1.7f * depth) * (1f - 0.25f * edge) * rs,
                    white = 0.52f - 0.44f * depth + 0.18f * edge,
                    alpha = (0.4f + 0.6f * depth).coerceIn(0f, 1f),
                )
            }
        }
    }

    // --- 7. Morph (Shaping / UI Transitions) ---
    private fun computeMorph(size: Float, t: Float, buffer: OrbFrameBuffer, seedOffset: Float) {
        val hold = 1.4f
        val morphDur = 0.9f
        val segDur = hold + morphDur
        val totalDur = segDur * 3f
        val tc = (t + seedOffset) % totalDur
        val k = (tc / segDur).toInt().coerceIn(0, 2)
        val local = tc - k * segDur
        val m = if (local > hold) {
            val p = ((local - hold) / morphDur).coerceIn(0f, 1f)
            p * p * (3f - 2f * p)
        } else {
            0f
        }

        val spread = 1.45f
        val c2 = size * 0.5f
        val n = max(12, (34 * (size / 64f)).toInt())
        val re = 0.021f * 1.35f * spread
        val pulse = 1f + 0.02f * sin(local * 3.1f)

        for (i in 0 until n) {
            val f = i.toFloat() / n
            // Shape 0: Circle, Shape 1: Triangle, Shape 2: Square
            val pA = sampleShape(k, f)
            val pB = sampleShape((k + 1) % 3, f)
            val x = lerp(pA[0], pB[0], m) * spread * pulse
            val y = lerp(pA[1], pB[1], m) * spread * pulse

            buffer.addDot(
                x = c2 + x * size,
                y = c2 + y * size,
                z = 0f,
                r = max(0.4f, re * size),
                white = 0.1f,
                alpha = 0.88f,
            )
        }
    }

    private fun sampleShape(shape: Int, f: Float): FloatArray {
        val out = FloatArray(2)
        when (shape) {
            0 -> {
                // Circle
                val a = -HALF_PI + f * TWO_PI
                out[0] = cos(a) * 0.24f
                out[1] = sin(a) * 0.24f
            }
            1 -> {
                // Triangle
                val p = f * 3f
                val seg = p.toInt()
                val sub = p - seg
                when (seg) {
                    0 -> {
                        out[0] = lerp(0f, 0.24f, sub)
                        out[1] = lerp(-0.26f, 0.16f, sub)
                    }
                    1 -> {
                        out[0] = lerp(0.24f, -0.24f, sub)
                        out[1] = 0.16f
                    }
                    else -> {
                        out[0] = lerp(-0.24f, 0f, sub)
                        out[1] = lerp(0.16f, -0.26f, sub)
                    }
                }
            }
            else -> {
                // Square
                val p = f * 4f
                val seg = p.toInt()
                val sub = p - seg
                when (seg) {
                    0 -> {
                        out[0] = lerp(0f, 0.2f, sub)
                        out[1] = -0.2f
                    }
                    1 -> {
                        out[0] = 0.2f
                        out[1] = lerp(-0.2f, 0.2f, sub)
                    }
                    2 -> {
                        out[0] = lerp(0.2f, -0.2f, sub)
                        out[1] = 0.2f
                    }
                    else -> {
                        out[0] = -0.2f
                        out[1] = lerp(0.2f, -0.2f, sub)
                    }
                }
            }
        }
        return out
    }
}
