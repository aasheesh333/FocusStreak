package com.focusstreak.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.focusstreak.app.ads.InterstitialAdManager
import com.focusstreak.app.ads.RewardedAdManager
import com.focusstreak.app.data.UserPreferencesRepository
import com.focusstreak.app.notification.NotificationChannels
import com.focusstreak.app.notification.OneSignalManager
import com.focusstreak.app.util.GmsAvailability
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics

open class FocusStreakApplication : Application() {

    // Marked `open` so tests can substitute mocks via a Robolectric
    // @Config(application=...) subclass without needing to spin up the
    // real DataStore / AdMob managers. Production code is unchanged.
    open val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(this)
    }

    // Process-scoped singletons: there should only be one in-flight ad
    // request of each type at a time. Both ViewModels consume these.
    open val interstitialAdManager: InterstitialAdManager by lazy {
        InterstitialAdManager(this)
    }

    open val rewardedAdManager: RewardedAdManager by lazy {
        RewardedAdManager(this)
    }

    override fun onCreate() {
        super.onCreate()

        // Devices without Google Play Services (OPPO/Vivo outside Google's
        // ecosystem, Huawei, Amazon Fire, CN-market phones) must skip all
        // GMS-backed SDK init. Initializing Firebase/AdMob/OneSignal on them
        // surfaces a system "This app requires Google Play services" prompt
        // that store reviewers flag as "Mandatorily download/update from
        // Google Play". The check is device-based (not store-based), so the
        // same build behaves correctly on every store.
        val gmsAvailable = GmsAvailability.isAvailable(this)

        if (gmsAvailable) {
            // Initialise Firebase early. The Crashlytics SDK normally self-inits
            // via its ContentProvider, but calling FirebaseApp.initializeApp
            // explicitly ensures the default app is ready before any Firebase
            // API is touched during OneSignal push registration (FCM token
            // retrieval depends on a Firebase installation id).
            FirebaseApp.initializeApp(this)

            // Enable Crashlytics collection. The SDK is auto-initialised but
            // collection can be disabled at runtime; we explicitly turn it
            // on so crashes are reported from the very first session.
            val crashlytics = FirebaseCrashlytics.getInstance()
            crashlytics.isCrashlyticsCollectionEnabled = true

            // Warm up the Mobile Ads SDK. This is a no-op once initialised.
            // We register an OnInitializationCompleteListener so we can also
            // request the first ad load as soon as the SDK is ready. The
            // ConsentManager in MainActivity will additionally call
            // [loadAllAds] once UMP consent is gathered; if the user is
            // outside EEA/UK the ConsentManager invokes its onComplete
            // synchronously (well, on next dispatch) so this is a no-op
            // duplicate in the best case.
            //
            // Cap served creatives at "G". FocusStreak's own rejection was the
            // Play installer check, not ad content — but PicFix Pro and
            // GovPhoto were both rejected by OPPO for gambling creatives in
            // their ad slots, and G is the cap that cleared PicFix's review.
            // Applying it here pre-empts the same rejection on the next
            // submission. Per-app only; the AdMob account rating stays at MA.
            MobileAds.setRequestConfiguration(
                RequestConfiguration.Builder()
                    .setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_G)
                    .build()
            )
            MobileAds.initialize(this) {
                android.util.Log.i("FocusStreakApp", "MobileAds SDK initialized")
            }
        } else {
            android.util.Log.i(
                "FocusStreakApp",
                "GMS not available — skipping Firebase/AdMob/OneSignal init"
            )
        }

        // Create notification channels once at process start so user-editable
        // channel settings (importance, sound, vibration) are stable.
        createNotificationChannels()

        // Initialize OneSignal for push notifications and in-app messaging.
        // Uses ONESIGNAL_APP_ID from BuildConfig (provided by CI secrets for
        // release builds; blank-safe for debug builds).
        // OneSignal is FCM-backed and requires GMS — skip on non-GMS devices.
        if (gmsAvailable) {
            OneSignalManager.initialize(this, BuildConfig.ONESIGNAL_APP_ID)
        }
    }

    /**
     * Trigger an ad load for every ad unit we serve. Safe to call
     * multiple times — the SDK de-duplicates in-flight requests.
     *
     * Call this from the UMP consent flow's onComplete callback
     * (MainActivity), so that the first ad request of a session
     * doesn't race the consent gather and end up NO_FILL in EEA/UK.
     */
    fun loadAllAds() {
        interstitialAdManager.loadAd()
        rewardedAdManager.loadAd()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(
            NotificationChannel(
                NotificationChannels.DAILY_REMINDER,
                getString(R.string.daily_reminder),
                NotificationManager.IMPORTANCE_DEFAULT
            )
        )
    }
}
