package com.nexus.app

import android.app.Application
import com.nexus.core.di.AppContainer

/**
 * Application root.
 *
 * Phase 1 uses a lightweight manual container ([AppContainer]) so the UI prototype stays
 * buildable and dependency-free. The container hides behind interfaces, so swapping it for
 * Hilt in Phase 2/3 is a mechanical change (annotations + modules), not a rewrite.
 */
class NexusApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer()
    }
}
