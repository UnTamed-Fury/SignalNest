import com.android.build.api.dsl.ApplicationExtension

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
            val kp = System.getenv("KEYSTORE_PASSWORD")
            val ka = System.getenv("KEY_ALIAS")
            val kpa = System.getenv("KEY_PASSWORD")
            if (!ks.isNullOrEmpty() && !kp.isNullOrEmpty() && !ka.isNullOrEmpty() && !kpa.isNullOrEmpty()) {
                storeFile     = file(ks)
                storePassword = kp
                keyAlias      = ka
                keyPassword   = kpa
            }
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled     = false
            applicationIdSuffix = ".debug"
        }
        release {
            isMinifyEnabled   = true   // R8 + ProGuard enabled for release
            isShrinkResources = true   // strip unused resources too
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Only apply signing if all keystore env vars are set
            val ks = System.getenv("KEYSTORE_FILE")
            val kp = System.getenv("KEYSTORE_PASSWORD")
            val ka = System.getenv("KEY_ALIAS")
            val kpa = System.getenv("KEY_PASSWORD")
            if (!ks.isNullOrEmpty() && !kp.isNullOrEmpty() && !ka.isNullOrEmpty() && !kpa.isNullOrEmpty()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    // ── ABI splits — produces: arm64-v8a, armeabi-v7a, x86_64, x86, universal
    splits {
        abi {
            isEnable = true
            reset()
            include("arm64-v8a", "armeabi-v7a", "x86_64", "x86")
            isUniversalApk = true   // also build the fat universal APK
        }
    }

    // Give each split a unique versionCode so stores accept all of them
    // Using applicationVariants.all with proper AGP 8.x casting
    applicationVariants.all {
        val variant = this
        variant.outputs
            .map { it as com.android.build.gradle.internal.api.BaseVariantOutputImpl }
            .forEach { output ->
                val abiFilter = output.getFilter("ABI")
                val abiCode = when (abiFilter) {
                    "arm64-v8a"   -> 4
                    "armeabi-v7a" -> 2
                    "x86_64"      -> 3
                    "x86"         -> 1
                    else          -> 0   // universal
                }
                // Use outputCode property which is writable
                output.outputFileName = "signalnest-${variant.versionName}-${abiFilter ?: "universal"}.apk"
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

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }

    packaging { resources { excludes += setOf("/META-INF/{AL2.0,LGPL2.1}") } }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.02.00")
    implementation(composeBom); androidTestImplementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.foundation:foundation")

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    implementation("com.rometools:rome:1.18.0")
    implementation("org.nanohttpd:nanohttpd:2.3.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    testImplementation("junit:junit:4.13.2")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
