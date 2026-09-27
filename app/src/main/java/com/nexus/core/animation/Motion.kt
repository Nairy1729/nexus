package com.nexus.core.animation

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import android.provider.Settings
import androidx.compose.runtime.remember

/**
 * Motion language of NEXUS.
 *
 * Fast, purposeful, bounded. Motion explains spatial relationships (where something came
 * from, what it became) — it never entertains. All durations are short by design; anything
 * longer than 480ms exists only for the two cinematic moments (call screens).
 */
object NexusMotion {

    // Durations (ms). Named `...Ms` so they can never collide with the easings below —
    // a duration and an easing want the same words ("standard", "quick") and Kotlin
    // resolves them as one ambiguous overload.
    const val press = 100
    const val quick = 160
    const val standardMs = 240
    const val relaxed = 320
    const val cinematic = 480

    /** Stagger steps */
    const val listStagger = 26
    const val radialStagger = 34

    // Easings
    val emphasized = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    val standard = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)
    val decelerate = CubicBezierEasing(0f, 0f, 0.2f, 1f)
    val accelerate = CubicBezierEasing(0.3f, 0f, 1f, 1f)

    // Springs — restrained on purpose. No cartoon bounce.
    val snappy: SpringSpec<Float> = spring(dampingRatio = 0.82f, stiffness = 380f)
    val gentle: SpringSpec<Float> = spring(dampingRatio = 0.90f, stiffness = 220f)
    val settle: SpringSpec<Float> = spring(dampingRatio = 1.0f, stiffness = 180f)

    fun quickSpec(): FiniteAnimationSpec<Float> = tween(press + quick, easing = standard)
    fun standardSpec(delay: Int = 0): FiniteAnimationSpec<Float> =
        tween(standardMs, delayMillis = delay, easing = emphasized)

    fun enter(delay: Int = 0): FiniteAnimationSpec<Float> =
        tween(relaxed, delayMillis = delay, easing = decelerate)
}

/** True when the user asked the system for reduced motion (or animator scale is 0). */
val LocalReducedMotion = staticCompositionLocalOf { false }

@Composable
@ReadOnlyComposable
fun reducedMotion(): Boolean = LocalReducedMotion.current

/**
 * Reads the global animator duration scale once per composition host.
 * Compose scales most animations automatically; this flag additionally parks the
 * always-on ambient effects (orbit pulse, incoming-call breathing).
 */
@Composable
fun rememberReducedMotion(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        runCatching {
            Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f,
            ) == 0f
        }.getOrDefault(false)
    }
}
