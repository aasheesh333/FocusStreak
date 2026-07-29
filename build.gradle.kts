// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "9.3.1" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.10" apply false

    // Firebase: apply the google-services plugin (parses google-services.json)
    // and the Crashlytics Gradle plugin (uploads mapping.txt for deobfuscation).
    // Pinned to versions compatible with AGP 9.3.1 / Gradle 9.x.
    id("com.google.gms.google-services") version "4.5.0" apply false
    id("com.google.firebase.crashlytics") version "3.0.7" apply false
}
