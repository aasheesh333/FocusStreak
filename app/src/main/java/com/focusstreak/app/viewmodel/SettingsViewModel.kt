package com.focusstreak.app.viewmodel

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.focusstreak.app.FocusStreakApplication
import com.focusstreak.app.ads.InterstitialAdManager
import com.focusstreak.app.ads.RewardedAdManager
import com.focusstreak.app.data.UserPreferencesRepository
import com.focusstreak.app.notification.NotificationScheduler
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * One-off UI events that the ViewModel needs the screen to handle
 * (Toasts, etc). The ViewModel no longer calls Toast directly so it
 * stays free of Android-Activity coupling.
 */
sealed interface SettingsUiEvent {
    object AdNotReady : SettingsUiEvent
}

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val userPreferencesRepository: UserPreferencesRepository = (application as FocusStreakApplication).userPreferencesRepository
    private val notificationScheduler = NotificationScheduler(application)
    private val rewardedAdManager: RewardedAdManager = (application as FocusStreakApplication).rewardedAdManager
    private val interstitialAdManager: InterstitialAdManager = (application as FocusStreakApplication).interstitialAdManager

    val userPreferencesFlow = userPreferencesRepository.userPreferencesFlow

    // Diagnostic toggle: when true, both ad managers swap to Google's
    // always-fills test ad units. Useful for distinguishing "ad flow is
    // broken" from "real ad unit has no fill in this region". Not
    // persisted — diagnostic only.
    private val _useTestAds = MutableStateFlow(false)
    val useTestAds: StateFlow<Boolean> = _useTestAds

    private val _events = Channel<SettingsUiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun setUseTestAds(enabled: Boolean) {
        _useTestAds.value = enabled
        rewardedAdManager.useTestAdUnit = enabled
        interstitialAdManager.useTestAdUnit = enabled
        // Re-request an ad with the new unit so the change takes effect
        // immediately on the next show.
        rewardedAdManager.loadAd()
        interstitialAdManager.loadAd()
    }

    fun resetAllProgress() {
        viewModelScope.launch {
            userPreferencesRepository.resetProgress()
        }
    }

    fun scheduleDailyReminder(hour: Int, minute: Int) {
        notificationScheduler.scheduleDailyReminder(hour, minute)
        viewModelScope.launch {
            userPreferencesRepository.updateReminderTime(hour, minute)
        }
    }

    fun cancelDailyReminder() {
        notificationScheduler.cancelDailyReminder()
    }

    fun updateFocusDuration(duration: Int) {
        viewModelScope.launch {
            userPreferencesRepository.updateFocusDuration(duration)
        }
    }

    fun showThemeAd(activity: Activity, theme: String) {
        rewardedAdManager.showAd(
            activity = activity,
            onAdNotReady = {
                rewardedAdManager.loadAd()
                viewModelScope.launch { _events.send(SettingsUiEvent.AdNotReady) }
            },
            onRewardEarned = {
                viewModelScope.launch {
                    userPreferencesRepository.updateTheme(theme)
                }
            }
        )
    }

    fun updateAutoStartBreak(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.updateAutoStartBreak(enabled)
        }
    }

    fun updateDailyReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.updateDailyReminderEnabled(enabled)
        }
    }

    fun updateSoundEffectsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.updateSoundEffectsEnabled(enabled)
        }
    }
}
