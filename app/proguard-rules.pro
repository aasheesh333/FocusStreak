# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in the Android SDK.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If you are using Kotlin, you might want to add the following line to your ProGuard configuration file:
-keep class kotlin.coroutines.jvm.internal.SuspendLambda { *; }

# You might also want to preserve annotations for reflection:
-keepattributes Signature
-keepattributes *Annotation*
