
val atlasReleaseStoreFile =
    providers.gradleProperty("ATLAS_RELEASE_STORE_FILE").orNull

val atlasReleaseStorePassword =
    providers.gradleProperty("ATLAS_RELEASE_STORE_PASSWORD").orNull

val atlasReleaseKeyAlias =
    providers.gradleProperty("ATLAS_RELEASE_KEY_ALIAS").orNull

val atlasReleaseKeyPassword =
    providers.gradleProperty("ATLAS_RELEASE_KEY_PASSWORD").orNull

val atlasReleaseSigningAvailable =
    !atlasReleaseStoreFile.isNullOrBlank() &&
            !atlasReleaseStorePassword.isNullOrBlank() &&
            !atlasReleaseKeyAlias.isNullOrBlank() &&
            !atlasReleaseKeyPassword.isNullOrBlank()

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)

    id("org.jetbrains.kotlin.plugin.serialization") version "2.4.10"
}

android {

    namespace = "com.noahrose.pocketlab"

    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }


    defaultConfig {
        applicationId = "com.noahrose.atlascyberdeck"

        minSdk = 29
        targetSdk = 36

        // Phase 3: Build native AI for the Armor 33 Pro
        ndk {
            abiFilters += listOf("arm64-v8a")
        }

        versionCode = 13
        versionName = "0.13.0-alpha"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"
    }


    /*
     * Atlas release signing.
     *
     * Signing credentials are loaded from Gradle
     * properties and are never stored in source control.
     */
    signingConfigs {
        if (atlasReleaseSigningAvailable) {
            create("atlasRelease") {
                storeFile = file(atlasReleaseStoreFile!!)
                storePassword = atlasReleaseStorePassword
                keyAlias = atlasReleaseKeyAlias
                keyPassword = atlasReleaseKeyPassword
            }
        }
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }

            if (atlasReleaseSigningAvailable) {
                signingConfig =
                    signingConfigs.getByName("atlasRelease")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    /*
     * Phase 3: Standalone Atlas AI native build.
     *
     * Uses CMake and Android NDK.
     * This is the native foundation for llama.cpp.
     */
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
        }
    }

    /*
     * Atlas native runtime packaging.
     *
     * PRoot and the external PRoot loader are
     * shipped as signed APK native code.
     *
     * Legacy packaging is intentional.
     */
    packaging {
        jniLibs {
            useLegacyPackaging = true

            /*
             * Preserve the verified Atlas PRoot binary.
             */
            keepDebugSymbols +=
                "**/libproot_atlas.so"

            /*
             * Preserve the verified ARM64 PRoot loader.
             *
             * Android permits execution from the
             * APK native-library directory.
             */
            keepDebugSymbols +=
                "**/libproot_loader_atlas.so"
        }
    }
}

val ktorVersion = "3.5.2"

dependencies {

    /*
     * Jetpack Compose
     */
    implementation(
        platform(libs.androidx.compose.bom)
    )

    implementation(
        libs.androidx.activity.compose
    )

    implementation(
        libs.androidx.compose.material3
    )

    implementation(
        libs.androidx.compose.ui
    )

    implementation(
        libs.androidx.compose.ui.graphics
    )

    implementation(
        libs.androidx.compose.ui.tooling.preview
    )

    /*
     * Navigation
     */
    implementation(
        "androidx.navigation:navigation-compose:2.9.4"
    )

    /*
     * Android Core and Lifecycle
     */
    implementation(
        libs.androidx.core.ktx
    )

    implementation(
        libs.androidx.lifecycle.runtime.ktx
    )

    implementation(
        "androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7"
    )

    implementation(
        "androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7"
    )

    /*
     * Ubuntu rootfs extraction
     */
    implementation(
        "org.apache.commons:commons-compress:1.28.0"
    )

    /*
     * AtlasBridge HTTP client
     */
    implementation(
        "io.ktor:ktor-client-core:$ktorVersion"
    )

    implementation(
        "io.ktor:ktor-client-okhttp:$ktorVersion"
    )

    implementation(
        "io.ktor:ktor-client-content-negotiation:$ktorVersion"
    )

    implementation(
        "io.ktor:ktor-serialization-kotlinx-json:$ktorVersion"
    )

    /*
     * Interactive Ubuntu terminal emulation
     */
    implementation(
        "org.connectbot:termlib:0.1.0"
    )

    /*
     * Unit testing
     */
    testImplementation(
        "junit:junit:4.13.2"
    )

    /*
     * Android instrumentation testing
     */
    androidTestImplementation(
        platform(libs.androidx.compose.bom)
    )

    androidTestImplementation(
        libs.androidx.compose.ui.test.junit4
    )

    androidTestImplementation(
        libs.androidx.espresso.core
    )

    androidTestImplementation(
        libs.androidx.junit
    )

    /*
     * Debugging
     */
    debugImplementation(
        libs.androidx.compose.ui.test.manifest
    )

    debugImplementation(
        libs.androidx.compose.ui.tooling
    )
}
