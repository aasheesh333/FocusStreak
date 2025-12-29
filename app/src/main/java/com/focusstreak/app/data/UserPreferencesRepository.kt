package com.focusstreak.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.*

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val COMPLETED_DATES = stringSetPreferencesKey("completed_dates")
        val TOTAL_SESSIONS = intPreferencesKey("total_sessions")
        val TOTAL_FOCUS_MINUTES = intPreferencesKey("total_focus_minutes")
        val FOCUS_DURATION = intPreferencesKey("focus_duration")
        val THEME = stringPreferencesKey("theme")
        val REMINDER_HOUR = intPreferencesKey("reminder_hour")
        val REMINDER_MINUTE = intPreferencesKey("reminder_minute")
        val AUTO_START_BREAK = booleanPreferencesKey("auto_start_break")
        val DAILY_REMINDER_ENABLED = booleanPreferencesKey("daily_reminder_enabled")
        val SOUND_EFFECTS_ENABLED = booleanPreferencesKey("sound_effects_enabled")
        val APP_LAUNCH_COUNT = intPreferencesKey("app_launch_count")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .map { preferences ->
            val completedDates = preferences[PreferencesKeys.COMPLETED_DATES] ?: emptySet()
            val totalSessions = preferences[PreferencesKeys.TOTAL_SESSIONS] ?: 0
            val totalFocusMinutes = preferences[PreferencesKeys.TOTAL_FOCUS_MINUTES] ?: 0
            val focusDuration = preferences[PreferencesKeys.FOCUS_DURATION] ?: 25
            val theme = preferences[PreferencesKeys.THEME] ?: "System"
            val reminderHour = preferences[PreferencesKeys.REMINDER_HOUR] ?: 9
            val reminderMinute = preferences[PreferencesKeys.REMINDER_MINUTE] ?: 0
            val autoStartBreak = preferences[PreferencesKeys.AUTO_START_BREAK] ?: false
            val dailyReminderEnabled = preferences[PreferencesKeys.DAILY_REMINDER_ENABLED] ?: false
            val soundEffectsEnabled = preferences[PreferencesKeys.SOUND_EFFECTS_ENABLED] ?: true
            val appLaunchCount = preferences[PreferencesKeys.APP_LAUNCH_COUNT] ?: 0
            UserPreferences(
                completedDates,
                calculateStreak(completedDates),
                totalSessions,
                totalFocusMinutes,
                focusDuration,
                theme,
                reminderHour,
                reminderMinute,
                autoStartBreak,
                dailyReminderEnabled,
                soundEffectsEnabled,
                appLaunchCount
            )
        }

    suspend fun updateOnSessionCompleted(focusDurationMinutes: Int) {
        context.dataStore.edit { preferences ->
            val completedDates = preferences[PreferencesKeys.COMPLETED_DATES] ?: emptySet()
            val newCompletedDates = completedDates.toMutableSet()
            newCompletedDates.add(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
            preferences[PreferencesKeys.COMPLETED_DATES] = newCompletedDates

            val totalSessions = (preferences[PreferencesKeys.TOTAL_SESSIONS] ?: 0) + 1
            val totalFocusMinutes = (preferences[PreferencesKeys.TOTAL_FOCUS_MINUTES] ?: 0) + focusDurationMinutes
            preferences[PreferencesKeys.TOTAL_SESSIONS] = totalSessions
            preferences[PreferencesKeys.TOTAL_FOCUS_MINUTES] = totalFocusMinutes
        }
    }

    private fun calculateStreak(completedDates: Set<String>): Int {
        if (completedDates.isEmpty()) return 0
        val sortedDates = completedDates.map {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it)
        }.sortedDescending()

        var streak = 0
        val calendar = Calendar.getInstance()
        val today = calendar.clone() as Calendar

        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(today.time)
        var isTodayCompleted = completedDates.contains(todayStr)

        if (isTodayCompleted) {
            streak = 1
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            for (i in 1 until sortedDates.size) {
                if (isSameDay(calendar, Calendar.getInstance().apply { time = sortedDates[i] })) {
                    streak++
                    calendar.add(Calendar.DAY_OF_YEAR, -1)
                } else {
                    break
                }
            }
        } else {
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            for (date in sortedDates) {
                if (isSameDay(calendar, Calendar.getInstance().apply { time = date })) {
                    streak++
                    calendar.add(Calendar.DAY_OF_YEAR, -1)
                } else {
                    break
                }
            }
        }
        return streak
    }


    suspend fun updateFocusDuration(duration: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FOCUS_DURATION] = duration
        }
    }

    suspend fun updateTheme(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME] = theme
        }
    }

    suspend fun updateReminderTime(hour: Int, minute: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.REMINDER_HOUR] = hour
            preferences[PreferencesKeys.REMINDER_MINUTE] = minute
        }
    }

    suspend fun updateAutoStartBreak(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_START_BREAK] = enabled
        }
    }

    suspend fun updateDailyReminderEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DAILY_REMINDER_ENABLED] = enabled
        }
    }

    suspend fun updateSoundEffectsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SOUND_EFFECTS_ENABLED] = enabled
        }
    }

    suspend fun incrementAppLaunchCount() {
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.APP_LAUNCH_COUNT] ?: 0
            preferences[PreferencesKeys.APP_LAUNCH_COUNT] = current + 1
        }
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    suspend fun resetProgress() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}

data class UserPreferences(
    val completedDates: Set<String>,
    val currentStreak: Int,
    val totalSessions: Int,
    val totalFocusMinutes: Int,
    val focusDuration: Int,
    val theme: String,
    val reminderHour: Int,
    val reminderMinute: Int,
    val autoStartBreak: Boolean,
    val dailyReminderEnabled: Boolean,
    val soundEffectsEnabled: Boolean,
    val appLaunchCount: Int
)
