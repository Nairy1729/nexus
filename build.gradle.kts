// NEXUS — root build file. Plugin and library versions live in gradle/libs.versions.toml.
//
// AGP 9 provides built-in Kotlin, so the `kotlin-android` plugin is intentionally absent.
// The buildscript classpath pins KGP to 2.3.20 (matching the Compose compiler plugin).
buildscript {
    dependencies {
        // Keep this in sync with `kotlin` in gradle/libs.versions.toml.
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.3.20")
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
}
