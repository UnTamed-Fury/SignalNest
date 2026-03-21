plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    kotlin("plugin.serialization")
}

android {
    namespace         = "com.signalnest.app"
    compileSdk        = 34
    buildToolsVersion = "34.0.0"

    defaultConfig {
        applicationId = "com.signalnest.app"
        minSdk        = 29
        targetSdk     = 34
        versionCode   = 1
        versionName   = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }
    }

    signingConfigs {
        create("release") {
            val ks = System.getenv("KEYSTORE_FILE")
            if (ks != null) {
                storeFile     = file(ks)
                storePassword = System.getenv("KEYSTORE_PASSWORD") ?: ""
                keyAlias      = System.getenv("KEY_ALIAS")         ?: ""
                keyPassword   = System.getenv("KEY_PASSWORD")      ?: ""
            }
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled     = false
            applicationIdSuffix = ".debug"
        }
        release {
            isMinifyEnabled   = false
            isShrinkResources = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (System.getenv("KEYSTORE_FILE") != null)
                signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions { jvmTarget = "17" }

    buildFeatures {
        compose     = true
        buildConfig = true
    }

    // FIX 1: composeOptions was missing — this causes a blank/crash build with Kotlin 1.9.20
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }

    packaging { resources { excludes += setOf("/META-INF/{AL2.0,LGPL2.1}") } }
}

dependencies {
    // Compose BOM
    val composeBom = platform("androidx.compose:compose-bom:2024.02.00")
    implementation(composeBom); androidTestImplementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.foundation:foundation")

    // Core
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Network
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // JSON (kotlinx-serialization — matches Kotlin 1.9.20)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

    // FIX 2: rome:2.1.0 requires Java 11 desktop env — use rometools for Android
    // The correct Android-compatible RSS library is com.rometools:rome:1.18.0
    // which works on Android without Java 11 module issues.
    implementation("com.rometools:rome:1.18.0")

    // LAN server
    implementation("org.nanohttpd:nanohttpd:2.3.1")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Tests
    testImplementation("junit:junit:4.13.2")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
