val atlasReleaseStoreFile =
    providers
        .gradleProperty(
            "ATLAS_RELEASE_STORE_FILE"
        )
        .orNull

val atlasReleaseStorePassword =
    providers
        .gradleProperty(
            "ATLAS_RELEASE_STORE_PASSWORD"
        )
        .orNull

val atlasReleaseKeyAlias =
    providers
        .gradleProperty(
            "ATLAS_RELEASE_KEY_ALIAS"
        )
        .orNull

val atlasReleaseKeyPassword =
    providers
        .gradleProperty(
            "ATLAS_RELEASE_KEY_PASSWORD"
        )
        .orNull

val atlasReleaseSigningAvailable =
    !atlasReleaseStoreFile.isNullOrBlank() &&
            !atlasReleaseStorePassword.isNullOrBlank() &&
            !atlasReleaseKeyAlias.isNullOrBlank() &&
            !atlasReleaseKeyPassword.isNullOrBlank()

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.noahrose.pocketlab"

    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.noahrose.pocketlab"

        minSdk = 29
        targetSdk = 36

        versionCode = 13
        versionName = "0.13.0-alpha"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"
    }

    /*
     * Atlas release signing.
     *
     * Signing credentials are loaded from the user's
     * Gradle properties and are never stored in the
     * Atlas source repository.
     *
     * Contributors without signing credentials can
     * continue building debug versions normally.
     */
    signingConfigs {

        if (
            atlasReleaseSigningAvailable
        ) {

            create(
                "atlasRelease"
            ) {

                storeFile =
                    file(
                        atlasReleaseStoreFile!!
                    )

                storePassword =
                    atlasReleaseStorePassword

                keyAlias =
                    atlasReleaseKeyAlias

                keyPassword =
                    atlasReleaseKeyPassword
            }
        }
    }

    buildTypes {

        release {

            optimization {
                enable = false
            }

            if (
                atlasReleaseSigningAvailable
            ) {

                signingConfig =
                    signingConfigs
                        .getByName(
                            "atlasRelease"
                        )
            }
        }
    }

    compileOptions {
        sourceCompatibility =
            JavaVersion.VERSION_11

        targetCompatibility =
            JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
    }

    /*
     * Atlas native runtime packaging.
     *
     * PRoot and the external PRoot loader are
     * shipped as signed APK native code.
     *
     * Legacy native packaging is intentional:
     * Atlas requires real executable filesystem
     * paths through ApplicationInfo.nativeLibraryDir.
     */
    packaging {
        jniLibs {

            useLegacyPackaging =
                true

            /*
             * Preserve the exact verified Atlas
             * PRoot runtime binary.
             */
            keepDebugSymbols +=
                "**/libproot_atlas.so"

            /*
             * Preserve the exact verified external
             * ARM64 PRoot loader.
             *
             * Android permits execution from the
             * APK native-library directory, which
             * allows PRoot to load guest Ubuntu
             * ELF binaries without executing them
             * directly from writable app storage.
             */
            keepDebugSymbols +=
                "**/libproot_loader_atlas.so"
        }
    }
}

dependencies {

    implementation(
        platform(
            libs.androidx.compose.bom
        )
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

    implementation(
        "androidx.navigation:navigation-compose:2.9.4"
    )

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

    implementation(
        "org.apache.commons:commons-compress:1.28.0"
    )

    /*
     * Full terminal emulation for validated interactive
     * Ubuntu applications such as nano and vim.
     *
     * Atlas feeds raw PTY output into this terminal
     * renderer rather than attempting to display ANSI
     * control sequences as normal text lines.
     */
    implementation(
        "org.connectbot:termlib:0.1.0"
    )

    testImplementation(
        "junit:junit:4.13.2"
    )

    androidTestImplementation(
        platform(
            libs.androidx.compose.bom
        )
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

    debugImplementation(
        libs.androidx.compose.ui.test.manifest
    )

    debugImplementation(
        libs.androidx.compose.ui.tooling
    )
}