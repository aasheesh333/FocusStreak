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
import com.google.android.gms.ads.MobileAds

class FocusStreakApplication : Application() {

    val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(this)
    }

    // Process-scoped singletons: there should only be one in-flight ad
    // request of each type at a time. Both ViewModels consume these.
    val interstitialAdManager: InterstitialAdManager by lazy {
        InterstitialAdManager(this)
    }

    val rewardedAdManager: RewardedAdManager by lazy {
        RewardedAdManager(this)
    }

    override fun onCreate() {
        super.onCreate()

        // Warm up the Mobile Ads SDK. This is a no-op once initialised.
        // We register an OnInitializationCompleteListener so we can also
        // request the first ad load as soon as the SDK is ready. The
        // ConsentManager in MainActivity will additionally call
        // [loadAllAds] once UMP consent is gathered; if the user is
        // outside EEA/UK the ConsentManager invokes its onComplete
        // synchronously (well, on next dispatch) so this is a no-op
        // duplicate in the best case.
        MobileAds.initialize(this) {
            android.util.Log.i("FocusStreakApp", "MobileAds SDK initialized")
        }

        // Create notification channels once at process start so user-editable
        // channel settings (importance, sound, vibration) are stable.
        createNotificationChannels()
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
