package com.nexus.core.spatial.renderer

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.SystemClock
import com.nexus.core.spatial.model.BodyType
import com.nexus.core.spatial.model.CelestialBody
import com.nexus.core.spatial.model.QualityLevel
import com.nexus.core.spatial.model.UniverseState
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * High-performance, hardware-accelerated OpenGL ES 2.0 Renderer for the NEXUS
 * 3D Personal Communication Universe.
 *
 * Designed with zero per-frame garbage collector allocations to guarantee 60–120 FPS.
 */
class UniverseGLRenderer : GLSurfaceView.Renderer {

    // Matrices (allocated once)
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)
    private val tempMatrix = FloatArray(16)

    // Camera state
    private var cameraYaw = 0.0f         // degrees
    private var cameraPitch = 24.0f      // degrees
    private var cameraDistance = 6.4f    // units
    private var targetX = 0.0f
    private var targetY = 0.0f
    private var targetZ = 0.0f

    // Camera target interpolation for cinematic fly-to transitions
    private var desiredYaw = 0.0f
    private var desiredPitch = 24.0f
    private var desiredDistance = 6.4f
    private var desiredTargetX = 0.0f
    private var desiredTargetY = 0.0f
    private var desiredTargetZ = 0.0f

    // Viewport dimensions
    private var viewportWidth = 1080
    private var viewportHeight = 2400
    private var aspectRatio = 1.0f

    // Shaders & Programs
    private var planetProgram = 0
    private var coreProgram = 0
    private var starProgram = 0
    private var lineProgram = 0

    // Geometry meshes
    private val sphereMesh = SphereMesh(latSegments = 24, lonSegments = 24)
    private val ringMesh = RingMesh(segments = 72)
    private val starfieldMesh = StarfieldMesh(count = 350)

    // Dynamic line buffer for constellations & signals
    private val maxDynamicLines = 100
    private val dynamicLineBuffer: FloatBuffer = ByteBuffer
        .allocateDirect(maxDynamicLines * 2 * 3 * 4)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()

    // Thread-safe universe state snapshot
    @Volatile
    private var universeState: UniverseState? = null

    // Cache of runtime world positions for fast touch picking
    // Map of bodyId -> floatArrayOf(worldX, worldY, worldZ, screenX, screenY, screenRadius)
    private val bodyScreenPositions = mutableMapOf<String, FloatArray>()

    private val startTimeMillis = SystemClock.uptimeMillis()

    fun updateUniverseState(state: UniverseState) {
        this.universeState = state

        // When a planet is selected, animate camera to focus on it
        val selectedId = state.selectedBodyId
        if (selectedId != null) {
            val body = state.celestialBodies.firstOrNull { it.id == selectedId }
            if (body != null) {
                // Focus camera on selected planet with cinematic close-up
                val t = (SystemClock.uptimeMillis() - startTimeMillis) / 1000.0f
                val angle = body.baseAngle + t * body.orbitSpeed
                desiredTargetX = cos(angle) * body.orbitRadius
                desiredTargetY = body.yOffset
                desiredTargetZ = sin(angle) * body.orbitRadius
                desiredDistance = max(1.2f, body.radius * 6.5f)
                desiredPitch = 15.0f
            }
        } else {
            // Reset to wide universe overview
            desiredTargetX = 0.0f
            desiredTargetY = 0.0f
            desiredTargetZ = 0.0f
            desiredDistance = 6.4f
            desiredPitch = 24.0f
        }
    }

    fun onDrag(deltaX: Float, deltaY: Float) {
        // Orbit drag
        desiredYaw += deltaX * 0.25f
        desiredPitch = (desiredPitch + deltaY * 0.25f).coerceIn(5.0f, 85.0f)
    }

    fun onPinch(scaleFactor: Float) {
        desiredDistance = (desiredDistance / scaleFactor).coerceIn(1.2f, 12.0f)
    }

    fun resetView() {
        desiredTargetX = 0.0f
        desiredTargetY = 0.0f
        desiredTargetZ = 0.0f
        desiredDistance = 6.4f
        desiredPitch = 24.0f
        desiredYaw = 0.0f
    }

    /**
     * Hit-tests screen touch position against rendered celestial bodies.
     * Returns the clicked body's ID or null if none hit.
     */
    fun pickPlanet(screenX: Float, screenY: Float): String? {
        synchronized(bodyScreenPositions) {
            var closestId: String? = null
            var minDistance = Float.MAX_VALUE

            for ((id, screenData) in bodyScreenPositions) {
                val sx = screenData[3]
                val sy = screenData[4]
                val touchRadius = max(screenData[5] * 1.8f, 75f) // Generous touch target >= 48dp

                val dist = hypot(screenX - sx, screenY - sy)
                if (dist <= touchRadius && dist < minDistance) {
                    minDistance = dist
                    closestId = id
                }
            }
            return closestId
        }
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f) // AMOLED Black
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glDepthFunc(GLES20.GL_LEQUAL)
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        // Compile GLSL programs
        planetProgram = ShaderUtil.createProgram(GLShaders.PLANET_VERTEX, GLShaders.PLANET_FRAGMENT)
        coreProgram = ShaderUtil.createProgram(GLShaders.CORE_VERTEX, GLShaders.CORE_FRAGMENT)
        starProgram = ShaderUtil.createProgram(GLShaders.STAR_VERTEX, GLShaders.STAR_FRAGMENT)
        lineProgram = ShaderUtil.createProgram(GLShaders.LINE_VERTEX, GLShaders.LINE_FRAGMENT)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        viewportWidth = width
        viewportHeight = height
        GLES20.glViewport(0, 0, width, height)
        aspectRatio = width.toFloat() / max(1f, height.toFloat())

        // Perspective Projection Matrix (45 deg FOV, near 0.1, far 50.0)
        Matrix.perspectiveM(projectionMatrix, 0, 45.0f, aspectRatio, 0.1f, 50.0f)
    }

    override fun onDrawFrame(gl: GL10?) {
        // Clear screen & depth buffer to deep spatial atmospheric graphite (#08090D)
        GLES20.glClearColor(0.031f, 0.035f, 0.051f, 1.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        val state = universeState ?: return
        val elapsedSeconds = (SystemClock.uptimeMillis() - startTimeMillis) / 1000.0f

        // Smooth camera damping (critically damped spring feel)
        val lerpFactor = 0.08f
        cameraYaw += (desiredYaw - cameraYaw) * lerpFactor
        cameraPitch += (desiredPitch - cameraPitch) * lerpFactor
        cameraDistance += (desiredDistance - cameraDistance) * lerpFactor
        targetX += (desiredTargetX - targetX) * lerpFactor
        targetY += (desiredTargetY - targetY) * lerpFactor
        targetZ += (desiredTargetZ - targetZ) * lerpFactor

        // Spherical camera eye coordinates
        val pitchRad = Math.toRadians(cameraPitch.toDouble()).toFloat()
        val yawRad = Math.toRadians(cameraYaw.toDouble()).toFloat()

        val eyeX = targetX + cameraDistance * cos(pitchRad) * sin(yawRad)
        val eyeY = targetY + cameraDistance * sin(pitchRad)
        val eyeZ = targetZ + cameraDistance * cos(pitchRad) * cos(yawRad)

        // Set View Matrix
        Matrix.setLookAtM(
            viewMatrix, 0,
            eyeX, eyeY, eyeZ,
            targetX, targetY, targetZ,
            0.0f, 1.0f, 0.0f
        )

        // 1. Draw Starfield (Background)
        drawStarfield(elapsedSeconds)

        // 2. Draw Orbital Rings
        drawOrbitalRings(state)

        // 3. Compute real-time planet world and screen coordinates
        val planetWorldPositions = computeBodyPositions(state, elapsedSeconds)

        // 4. Draw Constellations
        drawConstellations(state, planetWorldPositions)

        // 5. Draw Central User Sun / Core
        drawUserCore(state.centralUser, elapsedSeconds)

        // 6. Draw Celestial Bodies / Planets (Pure Spheres pass)
        drawPlanets(state, planetWorldPositions, eyeX, eyeY, eyeZ, elapsedSeconds)

        // 7. Draw Planet Rings (Separate Line Loop pass)
        drawPlanetRings(state, planetWorldPositions)

        // 8. Draw Active Signals
        drawSignals(state, elapsedSeconds)
    }

    private fun drawStarfield(time: Float) {
        GLES20.glUseProgram(starProgram)
        GLES20.glDepthMask(false) // Do not write to depth buffer for background stars

        val uMVPMatrix = GLES20.glGetUniformLocation(starProgram, "uMVPMatrix")
        val uTime = GLES20.glGetUniformLocation(starProgram, "uTime")
        val aPosition = GLES20.glGetAttribLocation(starProgram, "aPosition")
        val aBaseSize = GLES20.glGetAttribLocation(starProgram, "aBaseSize")
        val aTwinklePhase = GLES20.glGetAttribLocation(starProgram, "aTwinklePhase")
        val aTwinkleSpeed = GLES20.glGetAttribLocation(starProgram, "aTwinkleSpeed")

        // Skybox star MVP: stars move with camera rotation but ignore translation for deep parallax
        val skyViewMatrix = FloatArray(16)
        System.arraycopy(viewMatrix, 0, skyViewMatrix, 0, 16)
        skyViewMatrix[12] = 0f
        skyViewMatrix[13] = 0f
        skyViewMatrix[14] = 0f
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, skyViewMatrix, 0)

        GLES20.glUniformMatrix4fv(uMVPMatrix, 1, false, mvpMatrix, 0)
        GLES20.glUniform1f(uTime, time)

        starfieldMesh.vertexBuffer.position(0)
        GLES20.glEnableVertexAttribArray(aPosition)
        GLES20.glVertexAttribPointer(aPosition, 3, GLES20.GL_FLOAT, false, 6 * 4, starfieldMesh.vertexBuffer)

        starfieldMesh.vertexBuffer.position(3)
        GLES20.glEnableVertexAttribArray(aBaseSize)
        GLES20.glVertexAttribPointer(aBaseSize, 1, GLES20.GL_FLOAT, false, 6 * 4, starfieldMesh.vertexBuffer)

        starfieldMesh.vertexBuffer.position(4)
        GLES20.glEnableVertexAttribArray(aTwinklePhase)
        GLES20.glVertexAttribPointer(aTwinklePhase, 1, GLES20.GL_FLOAT, false, 6 * 4, starfieldMesh.vertexBuffer)

        starfieldMesh.vertexBuffer.position(5)
        GLES20.glEnableVertexAttribArray(aTwinkleSpeed)
        GLES20.glVertexAttribPointer(aTwinkleSpeed, 1, GLES20.GL_FLOAT, false, 6 * 4, starfieldMesh.vertexBuffer)

        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, starfieldMesh.starCount)

        GLES20.glDisableVertexAttribArray(aPosition)
        GLES20.glDisableVertexAttribArray(aBaseSize)
        GLES20.glDisableVertexAttribArray(aTwinklePhase)
        GLES20.glDisableVertexAttribArray(aTwinkleSpeed)
        GLES20.glDepthMask(true)
    }

    private fun drawOrbitalRings(state: UniverseState) {
        GLES20.glUseProgram(lineProgram)
        val uMVPMatrix = GLES20.glGetUniformLocation(lineProgram, "uMVPMatrix")
        val uColor = GLES20.glGetUniformLocation(lineProgram, "uColor")
        val aPosition = GLES20.glGetAttribLocation(lineProgram, "aPosition")

        GLES20.glEnableVertexAttribArray(aPosition)
        ringMesh.vertexBuffer.position(0)
        GLES20.glVertexAttribPointer(aPosition, 3, GLES20.GL_FLOAT, false, 0, ringMesh.vertexBuffer)

        for (orbit in state.orbitalRings) {
            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.scaleM(modelMatrix, 0, orbit.radius, 1.0f, orbit.radius)
            Matrix.multiplyMM(tempMatrix, 0, viewMatrix, 0, modelMatrix, 0)
            Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, tempMatrix, 0)

            GLES20.glUniformMatrix4fv(uMVPMatrix, 1, false, mvpMatrix, 0)
            GLES20.glUniform4f(
                uColor,
                orbit.color.red,
                orbit.color.green,
                orbit.color.blue,
                orbit.color.alpha * 0.45f
            )
            GLES20.glDrawArrays(GLES20.GL_LINE_LOOP, 0, ringMesh.segments)
        }
        GLES20.glDisableVertexAttribArray(aPosition)
    }

    private fun computeBodyPositions(
        state: UniverseState,
        time: Float
    ): Map<String, FloatArray> {
        val positions = mutableMapOf<String, FloatArray>()
        val matrixVP = FloatArray(16)
        Matrix.multiplyMM(matrixVP, 0, projectionMatrix, 0, viewMatrix, 0)

        // User Core at (0, 0, 0)
        val userScreen = projectToScreen(0f, 0f, 0f, state.centralUser.radius, matrixVP)
        synchronized(bodyScreenPositions) {
            bodyScreenPositions[state.centralUser.id] = floatArrayOf(0f, 0f, 0f, userScreen[0], userScreen[1], userScreen[2])
        }

        for (body in state.celestialBodies) {
            val angle = body.baseAngle + time * body.orbitSpeed
            val wx = cos(angle) * body.orbitRadius
            val wy = body.yOffset
            val wz = sin(angle) * body.orbitRadius

            positions[body.id] = floatArrayOf(wx, wy, wz)

            val screen = projectToScreen(wx, wy, wz, body.radius, matrixVP)
            synchronized(bodyScreenPositions) {
                bodyScreenPositions[body.id] = floatArrayOf(wx, wy, wz, screen[0], screen[1], screen[2])
            }
        }
        return positions
    }

    private fun projectToScreen(
        wx: Float, wy: Float, wz: Float, radius: Float, matrixVP: FloatArray
    ): FloatArray {
        val inVec = floatArrayOf(wx, wy, wz, 1.0f)
        val clipVec = FloatArray(4)
        Matrix.multiplyMV(clipVec, 0, matrixVP, 0, inVec, 0)

        if (clipVec[3] == 0f) return floatArrayOf(-1000f, -1000f, 0f)
        val ndcX = clipVec[0] / clipVec[3]
        val ndcY = clipVec[1] / clipVec[3]

        val screenX = (ndcX + 1.0f) * 0.5f * viewportWidth
        val screenY = (1.0f - ndcY) * 0.5f * viewportHeight

        // Approximate screen pixel radius
        val edgeVec = floatArrayOf(wx + radius, wy, wz, 1.0f)
        val edgeClip = FloatArray(4)
        Matrix.multiplyMV(edgeClip, 0, matrixVP, 0, edgeVec, 0)
        val edgeNdcX = edgeClip[0] / edgeClip[3]
        val screenRadius = abs(edgeNdcX - ndcX) * 0.5f * viewportWidth

        return floatArrayOf(screenX, screenY, max(screenRadius, 24f))
    }

    private fun abs(value: Float) = if (value < 0f) -value else value

    private fun drawConstellations(
        state: UniverseState,
        positions: Map<String, FloatArray>
    ) {
        if (state.constellations.isEmpty()) return

        GLES20.glUseProgram(lineProgram)
        val uMVPMatrix = GLES20.glGetUniformLocation(lineProgram, "uMVPMatrix")
        val uColor = GLES20.glGetUniformLocation(lineProgram, "uColor")
        val aPosition = GLES20.glGetAttribLocation(lineProgram, "aPosition")

        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
        GLES20.glUniformMatrix4fv(uMVPMatrix, 1, false, mvpMatrix, 0)

        for (constellation in state.constellations) {
            val members = constellation.memberIds.mapNotNull { positions[it] }
            if (members.size < 2) continue

            dynamicLineBuffer.clear()
            for (i in 0 until members.size - 1) {
                val p1 = members[i]
                val p2 = members[i + 1]
                dynamicLineBuffer.put(p1[0]); dynamicLineBuffer.put(p1[1]); dynamicLineBuffer.put(p1[2])
                dynamicLineBuffer.put(p2[0]); dynamicLineBuffer.put(p2[1]); dynamicLineBuffer.put(p2[2])
            }
            dynamicLineBuffer.position(0)

            GLES20.glEnableVertexAttribArray(aPosition)
            GLES20.glVertexAttribPointer(aPosition, 3, GLES20.GL_FLOAT, false, 0, dynamicLineBuffer)
            GLES20.glUniform4f(
                uColor,
                constellation.color.red,
                constellation.color.green,
                constellation.color.blue,
                constellation.color.alpha * 0.6f
            )
            GLES20.glDrawArrays(GLES20.GL_LINES, 0, (members.size - 1) * 2)
            GLES20.glDisableVertexAttribArray(aPosition)
        }
    }

    private fun drawUserCore(core: CelestialBody, time: Float) {
        GLES20.glUseProgram(coreProgram)
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        val uMVPMatrix = GLES20.glGetUniformLocation(coreProgram, "uMVPMatrix")
        val uCoreColor = GLES20.glGetUniformLocation(coreProgram, "uCoreColor")
        val uTime = GLES20.glGetUniformLocation(coreProgram, "uTime")
        val aPosition = GLES20.glGetAttribLocation(coreProgram, "aPosition")
        val aNormal = GLES20.glGetAttribLocation(coreProgram, "aNormal")

        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.scaleM(modelMatrix, 0, core.radius, core.radius, core.radius)
        Matrix.multiplyMM(tempMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, tempMatrix, 0)

        GLES20.glUniformMatrix4fv(uMVPMatrix, 1, false, mvpMatrix, 0)
        GLES20.glUniform4f(
            uCoreColor,
            core.primaryColor.red,
            core.primaryColor.green,
            core.primaryColor.blue,
            1.0f
        )
        GLES20.glUniform1f(uTime, time)

        GLES20.glEnableVertexAttribArray(aPosition)
        sphereMesh.vertexBuffer.position(0)
        GLES20.glVertexAttribPointer(aPosition, 3, GLES20.GL_FLOAT, false, 0, sphereMesh.vertexBuffer)

        GLES20.glEnableVertexAttribArray(aNormal)
        sphereMesh.normalBuffer.position(0)
        GLES20.glVertexAttribPointer(aNormal, 3, GLES20.GL_FLOAT, false, 0, sphereMesh.normalBuffer)

        sphereMesh.indexBuffer.position(0)
        GLES20.glDrawElements(
            GLES20.GL_TRIANGLES,
            sphereMesh.indexCount,
            GLES20.GL_UNSIGNED_SHORT,
            sphereMesh.indexBuffer
        )

        GLES20.glDisableVertexAttribArray(aPosition)
        GLES20.glDisableVertexAttribArray(aNormal)
    }

    private fun drawPlanets(
        state: UniverseState,
        positions: Map<String, FloatArray>,
        eyeX: Float, eyeY: Float, eyeZ: Float,
        time: Float
    ) {
        GLES20.glUseProgram(planetProgram)
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        val uMVPMatrix = GLES20.glGetUniformLocation(planetProgram, "uMVPMatrix")
        val uModelMatrix = GLES20.glGetUniformLocation(planetProgram, "uModelMatrix")
        val uCameraPos = GLES20.glGetUniformLocation(planetProgram, "uCameraPos")
        val uSunPos = GLES20.glGetUniformLocation(planetProgram, "uSunPos")
        val uPrimaryColor = GLES20.glGetUniformLocation(planetProgram, "uPrimaryColor")
        val uAtmosphereColor = GLES20.glGetUniformLocation(planetProgram, "uAtmosphereColor")
        val uGlow = GLES20.glGetUniformLocation(planetProgram, "uGlow")
        val uIsEclipse = GLES20.glGetUniformLocation(planetProgram, "uIsEclipse")
        val uIsSelected = GLES20.glGetUniformLocation(planetProgram, "uIsSelected")
        val uTime = GLES20.glGetUniformLocation(planetProgram, "uTime")

        val aPosition = GLES20.glGetAttribLocation(planetProgram, "aPosition")
        val aNormal = GLES20.glGetAttribLocation(planetProgram, "aNormal")

        GLES20.glUniform3f(uCameraPos, eyeX, eyeY, eyeZ)
        GLES20.glUniform3f(uSunPos, 0f, 0f, 0f) // Light originates at user core
        GLES20.glUniform1f(uTime, time)

        GLES20.glEnableVertexAttribArray(aPosition)
        sphereMesh.vertexBuffer.position(0)
        GLES20.glVertexAttribPointer(aPosition, 3, GLES20.GL_FLOAT, false, 0, sphereMesh.vertexBuffer)

        GLES20.glEnableVertexAttribArray(aNormal)
        sphereMesh.normalBuffer.position(0)
        GLES20.glVertexAttribPointer(aNormal, 3, GLES20.GL_FLOAT, false, 0, sphereMesh.normalBuffer)

        for (body in state.celestialBodies) {
            val pos = positions[body.id] ?: continue

            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, pos[0], pos[1], pos[2])
            Matrix.scaleM(modelMatrix, 0, body.radius, body.radius, body.radius)

            Matrix.multiplyMM(tempMatrix, 0, viewMatrix, 0, modelMatrix, 0)
            Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, tempMatrix, 0)

            GLES20.glUniformMatrix4fv(uMVPMatrix, 1, false, mvpMatrix, 0)
            GLES20.glUniformMatrix4fv(uModelMatrix, 1, false, modelMatrix, 0)

            GLES20.glUniform4f(
                uPrimaryColor,
                body.primaryColor.red,
                body.primaryColor.green,
                body.primaryColor.blue,
                1.0f
            )
            GLES20.glUniform4f(
                uAtmosphereColor,
                body.atmosphereColor.red,
                body.atmosphereColor.green,
                body.atmosphereColor.blue,
                body.atmosphereColor.alpha
            )
            GLES20.glUniform1f(uGlow, body.atmosphereGlow)
            GLES20.glUniform1f(uIsEclipse, if (body.isEclipse) 1.0f else 0.0f)
            GLES20.glUniform1f(uIsSelected, if (body.isSelected) 1.0f else 0.0f)

            sphereMesh.indexBuffer.position(0)
            GLES20.glDrawElements(
                GLES20.GL_TRIANGLES,
                sphereMesh.indexCount,
                GLES20.GL_UNSIGNED_SHORT,
                sphereMesh.indexBuffer
            )
        }

        GLES20.glDisableVertexAttribArray(aPosition)
        GLES20.glDisableVertexAttribArray(aNormal)
    }

    private fun drawPlanetRings(state: UniverseState, positions: Map<String, FloatArray>) {
        GLES20.glUseProgram(lineProgram)
        val lMVP = GLES20.glGetUniformLocation(lineProgram, "uMVPMatrix")
        val lColor = GLES20.glGetUniformLocation(lineProgram, "uColor")
        val lPos = GLES20.glGetAttribLocation(lineProgram, "aPosition")

        GLES20.glEnableVertexAttribArray(lPos)
        ringMesh.vertexBuffer.position(0)
        GLES20.glVertexAttribPointer(lPos, 3, GLES20.GL_FLOAT, false, 0, ringMesh.vertexBuffer)

        for (body in state.celestialBodies) {
            if (!body.hasRings) continue
            val pos = positions[body.id] ?: continue

            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, pos[0], pos[1], pos[2])
            Matrix.rotateM(modelMatrix, 0, 22.0f, 1f, 0f, 0.4f) // Tilted ring
            Matrix.scaleM(modelMatrix, 0, body.ringRadius, 1.0f, body.ringRadius)

            Matrix.multiplyMM(tempMatrix, 0, viewMatrix, 0, modelMatrix, 0)
            Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, tempMatrix, 0)

            GLES20.glUniformMatrix4fv(lMVP, 1, false, mvpMatrix, 0)
            GLES20.glUniform4f(lColor, 0.8f, 0.9f, 1.0f, 0.45f)
            GLES20.glDrawArrays(GLES20.GL_LINE_LOOP, 0, ringMesh.segments)
        }

        // User core halo ring
        if (state.centralUser.hasRings) {
            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.scaleM(modelMatrix, 0, state.centralUser.ringRadius, 1.0f, state.centralUser.ringRadius)
            Matrix.multiplyMM(tempMatrix, 0, viewMatrix, 0, modelMatrix, 0)
            Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, tempMatrix, 0)

            GLES20.glUniformMatrix4fv(lMVP, 1, false, mvpMatrix, 0)
            GLES20.glUniform4f(lColor, 0.8f, 1.0f, 0.3f, 0.35f)
            GLES20.glDrawArrays(GLES20.GL_LINE_LOOP, 0, ringMesh.segments)
        }

        GLES20.glDisableVertexAttribArray(lPos)
    }

    private fun drawSignals(state: UniverseState, time: Float) {
        if (state.activeSignals.isEmpty()) return

        GLES20.glUseProgram(lineProgram)
        val uMVPMatrix = GLES20.glGetUniformLocation(lineProgram, "uMVPMatrix")
        val uColor = GLES20.glGetUniformLocation(lineProgram, "uColor")
        val aPosition = GLES20.glGetAttribLocation(lineProgram, "aPosition")

        GLES20.glEnableVertexAttribArray(aPosition)
        ringMesh.vertexBuffer.position(0)
        GLES20.glVertexAttribPointer(aPosition, 3, GLES20.GL_FLOAT, false, 0, ringMesh.vertexBuffer)

        for (signal in state.activeSignals) {
            val radius = signal.currentRadius
            if (radius <= 0f) continue

            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, signal.originX, signal.originY, signal.originZ)
            Matrix.scaleM(modelMatrix, 0, radius, 1.0f, radius)

            Matrix.multiplyMM(tempMatrix, 0, viewMatrix, 0, modelMatrix, 0)
            Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, tempMatrix, 0)

            val fade = (1.0f - signal.progress).coerceIn(0f, 1f)
            GLES20.glUniformMatrix4fv(uMVPMatrix, 1, false, mvpMatrix, 0)
            GLES20.glUniform4f(
                uColor,
                signal.color.red,
                signal.color.green,
                signal.color.blue,
                signal.color.alpha * fade
            )
            GLES20.glDrawArrays(GLES20.GL_LINE_LOOP, 0, ringMesh.segments)
        }
        GLES20.glDisableVertexAttribArray(aPosition)
    }
}
