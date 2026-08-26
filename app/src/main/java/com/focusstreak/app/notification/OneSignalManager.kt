package com.focusstreak.app.notification

import android.content.Context
import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Centralized wrapper for the OneSignal Android SDK.
 *
 * Responsibilities:
 * - Initialize OneSignal with the app ID at Application startup.
 * - Request push notification permission when needed.
 *
 * All OneSignal SDK calls are routed through this class so the app never
 * depends directly on OneSignal APIs outside this module.
 */
object OneSignalManager {

    /**
     * Initialize OneSignal. Should be called once from [Application.onCreate].
     * If [appId] is blank, OneSignal is not initialized (useful for debug
     * builds that do not have a OneSignal app configured yet).
     */
    fun initialize(context: Context, appId: String) {
        if (appId.isBlank()) {
            android.util.Log.i(
                "OneSignalManager",
                "ONESIGNAL_APP_ID is blank; skipping OneSignal initialization."
            )
            return
        }

        // OneSignal is FCM-backed and requires Google Play Services. On
        // non-GMS devices initializing it surfaces the system
        // "Google Play services required" prompt — skip instead.
        if (!com.focusstreak.app.util.GmsAvailability.isAvailable(context)) {
            android.util.Log.i(
                "OneSignalManager",
                "Google Play Services not available; skipping OneSignal initialization."
            )
            return
        }

        // Verbose logging in debug builds only.
        if (com.focusstreak.app.BuildConfig.DEBUG) {
            OneSignal.Debug.logLevel = LogLevel.VERBOSE
        }

        // Suppress OneSignal's built-in "To receive push notifications please
        // press 'Update' to enable 'Google Play services'" dialog. The SDK
        // shows it from PushRegistratorAbstractGoogle when FCM registration
        // returns OUTDATED_GOOGLE_PLAY_SERVICES_APP, with no user action and
        // no way to dismiss the underlying prompt — which is exactly what OPPO
        // rejected 2.3.0/2.3.1 for ("Mandatorily update / download from Google
        // Play"). Must be set BEFORE initWithContext to take effect on the
        // first registration attempt. Push simply stays unavailable on such
        // devices instead of nagging the user toward Play.
        OneSignal.disableGMSMissingPrompt = true

        OneSignal.initWithContext(context.applicationContext, appId)
        android.util.Log.i("OneSignalManager", "OneSignal initialized")
    }

    /**
     * Request the runtime push notification permission through OneSignal.
     * This is the only place in the app where push permission is requested.
     * No-op on devices without Google Play Services (push is unavailable).
     */
    fun requestPushPermission() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                OneSignal.Notifications.requestPermission(true)
            } catch (t: Throwable) {
                android.util.Log.w(
                    "OneSignalManager",
                    "Push permission request failed (non-GMS device?)", t
                )
            }
        }
    }
}
