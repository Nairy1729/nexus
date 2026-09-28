package com.nexus.core.theme

import android.content.Context
import android.content.SharedPreferences

/**
 * Persists the user's selected NEXUS visual identity across app launches.
 */
object NexusThemePreferences {
    private const val PREFS_NAME = "nexus_theme_prefs"
    private const val KEY_SELECTED_THEME = "selected_theme_mode"

    private fun getPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getThemeMode(context: Context): NexusThemeMode {
        val savedId = getPrefs(context).getString(KEY_SELECTED_THEME, null)
        return NexusThemeMode.fromId(savedId)
    }

    fun setThemeMode(context: Context, mode: NexusThemeMode) {
        getPrefs(context)
            .edit()
            .putString(KEY_SELECTED_THEME, mode.id)
            .apply()
    }
}
