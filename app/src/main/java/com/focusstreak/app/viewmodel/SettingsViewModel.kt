package com.focusstreak.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.focusstreak.app.FocusStreakApplication
import com.focusstreak.app.data.UserPreferencesRepository
import com.focusstreak.app.notification.NotificationScheduler
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val userPreferencesRepository: UserPreferencesRepository = (application as FocusStreakApplication).userPreferencesRepository
    private val notificationScheduler = NotificationScheduler(application)

    val userPreferencesFlow = userPreferencesRepository.userPreferencesFlow

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

    suspend fun getReminderTime(): Pair<Int, Int> {
        val preferences = userPreferencesRepository.userPreferencesFlow.first()
        return Pair(preferences.reminderHour, preferences.reminderMinute)
    }

    fun updateFocusDuration(duration: Int) {
        viewModelScope.launch {
            userPreferencesRepository.updateFocusDuration(duration)
        }
    }

    fun updateTheme(theme: String) {
        viewModelScope.launch {
            userPreferencesRepository.updateTheme(theme)
        }
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
