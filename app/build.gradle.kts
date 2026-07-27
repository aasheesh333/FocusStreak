plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.focusstreak.app"
    compileSdk = 37

    defaultConfig {
        applicationId = System.getenv("PACKAGE_NAME") ?: "com.focusstreak.app"
        minSdk = 24
        targetSdk = 36
        versionCode = System.getenv("VERSION_CODE")?.toIntOrNull() ?: 2
        versionName = System.getenv("VERSION_NAME") ?: "2.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("release") {
            storeFile = file("keystore.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = "mykey"
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }

    // Helper: read env var with required/default semantics
    fun envOrNull(name: String): String? = System.getenv(name)?.takeIf { it.isNotBlank() }

    // Required env vars for release builds. The CI workflow provides these;
    // a missing value should fail the build rather than silently shipping
    // a placeholder or test value.
    val requiredReleaseEnvVars = listOf(
        "PACKAGE_NAME",
        "KEYSTORE_PASSWORD",
        "KEY_PASSWORD",
        "VERSION_CODE",
        "VERSION_NAME",
        "ADMOB_APP_ID",
        "ADMOB_INTERSTITIAL_ID",
        "ADMOB_REWARDED_ID",
        "ONESIGNAL_APP_ID"
    )

    // Defer the env-var check to task-execution time so that running
    // debug-only tasks (lint, testDebugUnitTest, assembleDebug) doesn't
    // require the release secrets. The release block still needs the
    // values at configuration time to populate buildConfigField /
    // manifestPlaceholders, so we also wire the check into a `whenReady`
    // hook on the task graph.
    gradle.taskGraph.whenReady {
        val hasReleaseTask = allTasks.any { task ->
            task.name.startsWith("assembleRelease") ||
                task.name.startsWith("bundleRelease") ||
                task.name.startsWith("lintRelease") ||
                task.name == "packageRelease"
        }
        if (hasReleaseTask) {
            val missing = requiredReleaseEnvVars.filter { envOrNull(it) == null }
            if (missing.isNotEmpty()) {
                throw GradleException(
                    "Missing required env vars for release build: $missing " +
                        "(check CI secrets)."
                )
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            val admobAppId = envOrNull("ADMOB_APP_ID")
                ?: "ca-app-pub-3940256099942544~3347511713" // debug fallback
            val admobInterstitial = envOrNull("ADMOB_INTERSTITIAL_ID")
                ?: "ca-app-pub-3940256099942544/1033173712"
            val admobRewarded = envOrNull("ADMOB_REWARDED_ID")
                ?: "ca-app-pub-3940256099942544/5224354917"
            val oneSignalAppId = envOrNull("ONESIGNAL_APP_ID") ?: ""

            manifestPlaceholders["adMobAppId"] = admobAppId
            buildConfigField("String", "ADMOB_INTERSTITIAL_ID", "\"$admobInterstitial\"")
            buildConfigField("String", "ADMOB_REWARDED_ID", "\"$admobRewarded\"")
            buildConfigField("String", "ONESIGNAL_APP_ID", "\"$oneSignalAppId\"")
        }
        debug {
            isMinifyEnabled = false
            // Google-provided sample IDs are safe to use in debug builds.
            manifestPlaceholders["adMobAppId"] = "ca-app-pub-3940256099942544~3940256099942544"
            buildConfigField("String", "ADMOB_INTERSTITIAL_ID", "\"ca-app-pub-3940256099942544/1033173712\"")
            buildConfigField("String", "ADMOB_REWARDED_ID", "\"ca-app-pub-3940256099942544/5224354917\"")
            // Debug builds can run without the OneSignal secret; init is skipped if blank.
            buildConfigField("String", "ONESIGNAL_APP_ID", "\"\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        compilerOptions {
            jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.19.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.11.0")
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation(platform("androidx.compose:compose-bom:2026.06.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.fragment:fragment-ktx:1.8.9")
    implementation("androidx.compose.foundation:foundation-layout")
    implementation("androidx.navigation:navigation-compose:2.9.8")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.11.0")
    implementation("androidx.datastore:datastore-preferences:1.2.1")
    implementation("com.google.android.gms:play-services-ads:25.4.0")
    implementation("com.google.android.ump:user-messaging-platform:4.0.0")
    implementation("androidx.work:work-runtime-ktx:2.11.2")
    implementation("com.onesignal:OneSignal:5.9.7")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.11.0")
    testImplementation("app.cash.turbine:turbine:1.2.1")
    testImplementation("io.mockk:mockk:1.14.11")
    testImplementation("com.google.truth:truth:1.4.5")
    testImplementation("org.robolectric:robolectric:4.16.1")
    testImplementation("androidx.test:core:1.7.0")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    androidTestImplementation(platform("androidx.compose:compose-bom:2026.06.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
