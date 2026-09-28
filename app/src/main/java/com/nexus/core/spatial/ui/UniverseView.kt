package com.nexus.core.spatial.ui

import android.content.Context
import android.opengl.GLSurfaceView
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.nexus.core.haptics.LocalNexusHaptics
import com.nexus.core.spatial.model.UniverseState
import com.nexus.core.spatial.renderer.UniverseGLRenderer

/**
 * Compose wrapper around OpenGL ES [UniverseGLRenderer].
 * Handles spatial touch interactions (drag to explore, pinch to zoom, tap to focus).
 */
@Composable
fun UniverseView(
    state: UniverseState,
    onSelectPlanet: (String) -> Unit,
    onResetFocus: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val haptics = LocalNexusHaptics.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val renderer = remember { UniverseGLRenderer() }
    val glSurfaceView = remember {
        GLSurfaceView(context).apply {
            setEGLContextClientVersion(2)
            setRenderer(renderer)
            renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        }
    }

    // Lifecycle sync
    DisposableEffect(lifecycleOwner, glSurfaceView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> glSurfaceView.onResume()
                Lifecycle.Event.ON_PAUSE -> glSurfaceView.onPause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            glSurfaceView.onPause()
        }
    }

    // Push universe state changes to renderer
    LaunchedEffect(state) {
        renderer.updateUniverseState(state)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    if (pan.x != 0f || pan.y != 0f) {
                        renderer.onDrag(pan.x, pan.y)
                    }
                    if (zoom != 1f) {
                        renderer.onPinch(zoom)
                    }
                }
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val hitId = renderer.pickPlanet(offset.x, offset.y)
                    if (hitId != null) {
                        haptics.confirm()
                        onSelectPlanet(hitId)
                    } else {
                        haptics.tick()
                        onResetFocus()
                    }
                }
            }
    ) {
        AndroidView(
            factory = { glSurfaceView },
            modifier = Modifier.fillMaxSize()
        )
    }
}
