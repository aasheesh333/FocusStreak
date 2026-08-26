package com.focusstreak.app.util

import android.content.Context
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability

/**
 * Detects whether Google Play Services (GMS) is usable on this device.
 *
 * OPPO/realme/OnePlus devices sold outside the Google ecosystem (and all
 * Chinese-market devices) do NOT ship GMS. Calling GMS-backed SDKs on them
 * (Firebase init, AdMob, UMP consent, OneSignal/FCM) surfaces a system-level
 * "This app requires Google Play services" prompt, which store reviewers
 * flag as "Mandatorily update / download from Google Play".
 *
 * We check once and gate all GMS-dependent startup work on the result.
 */
object GmsAvailability {

    private const val TAG = "GmsAvailability"

    @Volatile
    private var cached: Boolean? = null

    /**
     * Returns true if Google Play Services is usable on this device.
     * Fails open (returns true) if the check itself throws, so behaviour on
     * Google devices is unchanged.
     */
    fun isAvailable(context: Context): Boolean {
        cached?.let { return it }
        return try {
            val code = GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(context.applicationContext)
            val ok = code == ConnectionResult.SUCCESS
            cached = ok
            Log.i(TAG, "Google Play Services availability code=$code available=$ok")
            ok
        } catch (t: Throwable) {
            Log.w(TAG, "GMS availability check failed; assuming available", t)
            cached = true
            true
        }
    }
}
