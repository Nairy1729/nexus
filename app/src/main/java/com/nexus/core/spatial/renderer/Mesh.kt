package com.nexus.core.spatial.renderer

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Procedural 3D Sphere geometry generator.
 * Uses a latitude-longitude tessellation optimized for high visual quality at low polygon count.
 */
class SphereMesh(latSegments: Int = 20, lonSegments: Int = 20) {
    val vertexBuffer: FloatBuffer
    val normalBuffer: FloatBuffer
    val indexBuffer: ShortBuffer
    val indexCount: Int

    init {
        val numVertices = (latSegments + 1) * (lonSegments + 1)
        val vertices = FloatArray(numVertices * 3)
        val normals = FloatArray(numVertices * 3)

        var vIdx = 0
        for (lat in 0..latSegments) {
            val theta = lat * PI.toFloat() / latSegments
            val sinTheta = sin(theta)
            val cosTheta = cos(theta)

            for (lon in 0..lonSegments) {
                val phi = lon * 2f * PI.toFloat() / lonSegments
                val sinPhi = sin(phi)
                val cosPhi = cos(phi)

                val x = cosPhi * sinTheta
                val y = cosTheta
                val z = sinPhi * sinTheta

                normals[vIdx] = x
                normals[vIdx + 1] = y
                normals[vIdx + 2] = z

                vertices[vIdx] = x
                vertices[vIdx + 1] = y
                vertices[vIdx + 2] = z

                vIdx += 3
            }
        }

        indexCount = latSegments * lonSegments * 6
        val indices = ShortArray(indexCount)
        var iIdx = 0
        for (lat in 0 until latSegments) {
            for (lon in 0 until lonSegments) {
                val first = (lat * (lonSegments + 1) + lon).toShort()
                val second = (first + lonSegments + 1).toShort()

                indices[iIdx++] = first
                indices[iIdx++] = second
                indices[iIdx++] = (first + 1).toShort()

                indices[iIdx++] = (first + 1).toShort()
                indices[iIdx++] = second
                indices[iIdx++] = (second + 1).toShort()
            }
        }

        vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply { put(vertices); position(0) }

        normalBuffer = ByteBuffer.allocateDirect(normals.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply { put(normals); position(0) }

        indexBuffer = ByteBuffer.allocateDirect(indices.size * 2)
            .order(ByteOrder.nativeOrder())
            .asShortBuffer()
            .apply { put(indices); position(0) }
    }
}

/**
 * Procedural Circle / Orbital Ring geometry generator.
 */
class RingMesh(val segments: Int = 64) {
    val vertexBuffer: FloatBuffer

    init {
        val vertices = FloatArray(segments * 3)
        var vIdx = 0
        for (i in 0 until segments) {
            val angle = i * 2f * PI.toFloat() / segments
            vertices[vIdx++] = cos(angle)
            vertices[vIdx++] = 0f
            vertices[vIdx++] = sin(angle)
        }

        vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply { put(vertices); position(0) }
    }
}

/**
 * Procedural Starfield with depth, parallax, and twinkle attributes.
 */
class StarfieldMesh(count: Int = 300) {
    val vertexBuffer: FloatBuffer
    val starCount: Int = count

    init {
        // Each star: [x, y, z, baseSize, twinklePhase, twinkleSpeed]
        val data = FloatArray(count * 6)
        val rng = Random(42) // Stable deterministic starfield
        var idx = 0

        for (i in 0 until count) {
            // Distribute on a hollow sphere / deep volume
            val radius = 9.0f + rng.nextFloat() * 12.0f
            val theta = rng.nextFloat() * 2f * PI.toFloat()
            val phi = (rng.nextFloat() - 0.5f) * PI.toFloat()

            val x = (radius * cos(phi) * cos(theta))
            val y = (radius * sin(phi))
            val z = (radius * cos(phi) * sin(theta))

            val size = 2.0f + rng.nextFloat() * 4.0f
            val phase = rng.nextFloat() * 2f * PI.toFloat()
            val speed = 0.5f + rng.nextFloat() * 2.0f

            data[idx++] = x
            data[idx++] = y
            data[idx++] = z
            data[idx++] = size
            data[idx++] = phase
            data[idx++] = speed
        }

        vertexBuffer = ByteBuffer.allocateDirect(data.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply { put(data); position(0) }
    }
}
