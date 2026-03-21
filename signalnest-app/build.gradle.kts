// Gradle 8.4 + AGP 8.2.0 + Kotlin 1.9.20 + KSP 1.9.20-1.0.14
plugins {
    id("com.android.application")          version "8.2.0"        apply false
    id("org.jetbrains.kotlin.android")     version "1.9.20"       apply false
    id("com.google.devtools.ksp")          version "1.9.20-1.0.14" apply false
    kotlin("plugin.serialization")         version "1.9.20"       apply false
}
