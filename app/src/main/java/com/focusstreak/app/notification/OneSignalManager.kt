package com.focusstreak.app.notification

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel
import com.onesignal.user.subscriptions.IPushSubscriptionObserver
import com.onesignal.user.subscriptions.PushSubscriptionChangedState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Centralized wrapper for the OneSignal Android SDK.
 *
 * Responsibilities:
 * - Initialize OneSignal with the app ID at Application startup.
 * - Set up a push-subscription observer that confirms registration.
 * - Gate the push-permission request behind the official verification dialog.
 *
 * All OneSignal SDK calls are routed through this class so the app never
 * depends directly on OneSignal APIs outside this module.
 */
object OneSignalManager {

    private const val LOCAL_ID_PREFIX = "local-"

    private val dialogShown = AtomicBoolean(false)

    // OneSignal stores observers weakly, so we keep a strong reference here
    // for the lifetime of the process.
    private var pushObserver: IPushSubscriptionObserver? = null

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

        // Verbose logging in debug builds only.
        if (com.focusstreak.app.BuildConfig.DEBUG) {
            OneSignal.Debug.logLevel = LogLevel.VERBOSE
        }

        OneSignal.initWithContext(context.applicationContext, appId)
        android.util.Log.i("OneSignalManager", "OneSignal initialized")
    }

    /**
     * Watch for a real, server-assigned push subscription ID and show the
     * official OneSignal verification dialog exactly once when it appears.
     *
     * Should be called from the launcher Activity after the UI is ready.
     */
    fun setupPushSubscriptionObserver(activity: Activity) {
        val observer = object : IPushSubscriptionObserver {
            override fun onPushSubscriptionChange(state: PushSubscriptionChangedState) {
                maybeShowVerificationDialog(activity, state.current.id)
            }
        }
        pushObserver = observer
        OneSignal.User.pushSubscription.addObserver(observer)

        // The subscription ID may already be server-assigned by the time the
        // observer is attached, so evaluate the current value immediately.
        maybeShowVerificationDialog(activity, OneSignal.User.pushSubscription.id)
    }

    private fun isRegistered(subscriptionId: String?): Boolean {
        return !subscriptionId.isNullOrEmpty() && !subscriptionId.startsWith(LOCAL_ID_PREFIX)
    }

    private fun maybeShowVerificationDialog(activity: Activity, subscriptionId: String?) {
        if (isRegistered(subscriptionId) && dialogShown.compareAndSet(false, true)) {
            Handler(Looper.getMainLooper()).post {
                showIntegrationCompleteDialog(activity)
            }
        }
    }

    private fun showIntegrationCompleteDialog(activity: Activity) {
        AlertDialog.Builder(activity)
            .setTitle("Your OneSignal SDK integration is complete!")
            .setMessage(
                "You can now send Push Notifications & In-App Messages through OneSignal. " +
                    "Tap below to enable push notifications."
            )
            .setPositiveButton("Got it") { _, _ ->
                requestPushPermission()
            }
            .setCancelable(false)
            .show()
    }

    /**
     * Request the runtime push notification permission through OneSignal.
     * This is the only place in the app where push permission is requested.
     */
    fun requestPushPermission() {
        CoroutineScope(Dispatchers.Main).launch {
            OneSignal.Notifications.requestPermission(true)
        }
    }
}
