# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in the Android SDK.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# ----------------------------------------------------------------------
# Attributes required by reflection-based libraries (Kotlin, AdMob, etc.)
# ----------------------------------------------------------------------
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes InnerClasses
-keepattributes EnclosingMethod
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations
-keepattributes RuntimeVisibleTypeAnnotations

# ----------------------------------------------------------------------
# Kotlin metadata & coroutines
# ----------------------------------------------------------------------
-keep class kotlin.Metadata { *; }
-keep class kotlin.coroutines.Continuation
-keepclassmembers class kotlin.coroutines.jvm.internal.SuspendLambda { *; }
-dontwarn kotlin.**
-dontwarn kotlinx.**

# ----------------------------------------------------------------------
# Google Mobile Ads SDK
# ----------------------------------------------------------------------
-keep class com.google.android.gms.ads.** { *; }
-keep interface com.google.android.gms.ads.** { *; }
-keep class com.google.android.gms.ads.rewarded.** { *; }
-keep class com.google.android.gms.ads.interstitial.** { *; }
-keep class com.google.android.gms.ads.formats.** { *; }
-keep class com.google.android.gms.ads.mediation.** { *; }
-dontwarn com.google.android.gms.ads.**

# UMP (User Messaging Platform) consent SDK
-keep class com.google.android.ump.** { *; }
-dontwarn com.google.android.ump.**

# ----------------------------------------------------------------------
# OneSignal
# ----------------------------------------------------------------------
-keep class com.onesignal.** { *; }
-dontwarn com.onesignal.**
-keep class * implements com.onesignal.notifications.INotificationServiceExtension {
    void onNotificationReceived(com.onesignal.notifications.INotificationReceivedEvent);
}

# ----------------------------------------------------------------------
# AndroidX WorkManager - keep Worker subclasses by name; WorkManager
# instantiates workers via reflection on Class<*>.
# ----------------------------------------------------------------------
-keep public class * extends androidx.work.Worker
-keep public class * extends androidx.work.CoroutineWorker
-keep public class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep class androidx.work.OverwritingInputMerger {
    public <init>();
}
-keep class com.focusstreak.app.notification.ReminderWorker { *; }

# ----------------------------------------------------------------------
# AndroidX Compose runtime (keep stable Compose internals)
# ----------------------------------------------------------------------
-keep class androidx.compose.runtime.** { *; }
-keepclassmembers class * { @androidx.compose.runtime.Composable *; }
-dontwarn androidx.compose.**

# ----------------------------------------------------------------------
# App's own BuildConfig
# ----------------------------------------------------------------------
-keep class com.focusstreak.app.BuildConfig { *; }

# ----------------------------------------------------------------------
# Strip verbose/debug log calls in release
# ----------------------------------------------------------------------
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}
