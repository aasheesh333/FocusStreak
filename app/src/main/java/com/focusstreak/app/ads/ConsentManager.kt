package com.focusstreak.app.ads

import android.app.Activity
import android.content.Context
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.FormError
import com.google.android.ump.UserMessagingPlatform

/**
 * Encapsulates Google UMP (User Messaging Platform) consent flow.
 *
 * The EU User Consent Policy requires apps that use AdMob and reach
 * users in the EEA / UK to display a consent dialog before any ad is
 * requested. This class:
 *   1. Queries the current consent status.
 *   2. If required, gathers a fresh consent info update.
 *   3. Loads and shows the consent form if needed.
 *   4. Falls back gracefully (ads keep working) if anything throws.
 *
 * Call [requestConsentIfNeeded] from your launcher Activity (e.g. in
 * onCreate) before any other AdMob API is invoked. The call is
 * non-blocking — the consent flow runs asynchronously and the supplied
 * [onComplete] is invoked regardless of outcome.
 */
class ConsentManager(private val context: Context) {

    private val consentInformation: ConsentInformation =
        UserMessagingPlatform.getConsentInformation(context)

    /**
     * Request consent if the user is in a region that requires it (EEA/UK).
     * Must be called on the UI thread. The supplied [onComplete] is invoked
     * exactly once, after the consent flow has either succeeded, been
     * dismissed by the user, or failed.
     */
    fun requestConsentIfNeeded(activity: Activity, onComplete: () -> Unit) {
        val params = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)
            .build()

        try {
            consentInformation.requestConsentInfoUpdate(
                activity,
                params,
                {
                    // Consent info updated successfully. Show the form if
                    // the user is in a region that requires it.
                    loadAndShowFormIfRequired(activity) { onComplete() }
                },
                { formError: FormError? ->
                    // Failed to update consent info. Still let the user
                    // proceed — ads will fall back to non-personalised.
                    android.util.Log.w(
                        "ConsentManager",
                        "Consent info update failed: ${formError?.errorCode} ${formError?.message}"
                    )
                    onComplete()
                }
            )
        } catch (e: Exception) {
            android.util.Log.w("ConsentManager", "Consent info update threw", e)
            onComplete()
        }
    }

    private fun loadAndShowFormIfRequired(activity: Activity, onComplete: () -> Unit) {
        try {
            UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError: FormError? ->
                if (formError != null) {
                    android.util.Log.w(
                        "ConsentManager",
                        "Consent form show failed: ${formError.errorCode} ${formError.message}"
                    )
                }
                onComplete()
            }
        } catch (e: Exception) {
            android.util.Log.w("ConsentManager", "loadAndShowConsentFormIfRequired threw", e)
            onComplete()
        }
    }

    /**
     * Reset consent state. Useful for testing or when the user changes
     * their region in app settings.
     */
    fun resetConsentState() {
        consentInformation.reset()
    }

    /**
     * Whether the SDK has determined the user can be served with ads
     * (i.e. consent is gathered or not required). Use this to gate
     * your ad-loading calls if you want strict ordering.
     */
    fun canRequestAds(): Boolean = consentInformation.canRequestAds()
}
