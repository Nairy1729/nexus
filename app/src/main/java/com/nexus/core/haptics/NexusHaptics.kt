package com.nexus.core.haptics

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalView

/**
 * Haptic vocabulary. Vibration is a language, not a texture — a pattern exists for each
 * meaningful event and nothing else ever buzzes.
 */
class NexusHaptics(private val view: View) {

    /** Keypad press — light and dry. */
    fun key() = perform(HapticFeedbackConstants.KEYBOARD_TAP)

    /** Selection: contact node, chip, dock item. */
    fun select() = perform(HapticFeedbackConstants.CONTEXT_CLICK)

    /** Threshold crossed (drag-to-call arming). */
    fun tick() = perform(HapticFeedbackConstants.CLOCK_TICK)

    /** A committed action: call placed, answered, ended. */
    fun confirm() = perform(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            HapticFeedbackConstants.CONFIRM
        } else {
            HapticFeedbackConstants.LONG_PRESS
        }
    )

    /** Something important changed state (call connected, screen took over). */
    fun heavy() = perform(HapticFeedbackConstants.LONG_PRESS)

    private fun perform(constant: Int) {
        view.performHapticFeedback(constant)
    }
}

val LocalNexusHaptics = staticCompositionLocalOf<NexusHaptics> {
    error("LocalNexusHaptics not provided. Wrap your app in NexusHapticsProvider.")
}

@Composable
fun rememberHaptics(): NexusHaptics = LocalNexusHaptics.current

/** Convenience: platform view so callers can use plain [View.performHapticFeedback]. */
@Composable
fun localView(): View = LocalView.current
