package com.focusstreak.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
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
        val BONUS_MINUTES = intPreferencesKey("bonus_minutes")
        val CURRENT_CATEGORY = stringPreferencesKey("current_category")
        val SESSION_HISTORY = stringPreferencesKey("session_history")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .catch { exception ->
            // DataStore throws IOException on disk errors. Emit an empty
            // Preferences so downstream operators can keep working; rethrow
            // any other exception type.
            if (exception is IOException) {
                emit(androidx.datastore.preferences.core.emptyPreferences())
            } else {
                throw exception
            }
        }
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
            val bonusMinutes = preferences[PreferencesKeys.BONUS_MINUTES] ?: 0
            val currentCategory = preferences[PreferencesKeys.CURRENT_CATEGORY] ?: FocusCategories.first().id
            UserPreferences(
                completedDates,
                StreakCalculator.calculate(completedDates),
                totalSessions,
                totalFocusMinutes,
                focusDuration,
                theme,
                reminderHour,
                reminderMinute,
                autoStartBreak,
                dailyReminderEnabled,
                soundEffectsEnabled,
                appLaunchCount,
                bonusMinutes,
                currentCategory
            )
        }

    suspend fun updateOnSessionCompleted(focusDurationMinutes: Int, category: String = FocusCategories.first().id) {
        context.dataStore.edit { preferences ->
            val completedDates = preferences[PreferencesKeys.COMPLETED_DATES] ?: emptySet()
            val newCompletedDates = completedDates.toMutableSet()
            newCompletedDates.add(StreakCalculator.DATE_KEY_FORMAT.format(Date()))
            // Prune to last ~1 year to keep the set bounded and the
            // read/write performance stable for long-term users.
            val oneYearAgoMs = System.currentTimeMillis() - 365L * 24 * 60 * 60 * 1000
            val oneYearAgoKey = StreakCalculator.DATE_KEY_FORMAT.format(Date(oneYearAgoMs))
            newCompletedDates.removeAll { it < oneYearAgoKey }
            preferences[PreferencesKeys.COMPLETED_DATES] = newCompletedDates

            val totalSessions = (preferences[PreferencesKeys.TOTAL_SESSIONS] ?: 0) + 1
            val totalFocusMinutes = (preferences[PreferencesKeys.TOTAL_FOCUS_MINUTES] ?: 0) + focusDurationMinutes
            preferences[PreferencesKeys.TOTAL_SESSIONS] = totalSessions
            preferences[PreferencesKeys.TOTAL_FOCUS_MINUTES] = totalFocusMinutes

            // Append session to history for rich stats.
            val session = FocusSession(
                timestampMillis = System.currentTimeMillis(),
                durationMinutes = focusDurationMinutes,
                category = category
            )
            val history = (preferences[PreferencesKeys.SESSION_HISTORY] ?: "").decodeSessionHistory()
                .toMutableList()
                .apply { add(session) }
                .takeLast(500) // Keep the history bounded.
            preferences[PreferencesKeys.SESSION_HISTORY] = history.encodeToJson()
        }
    }

    /**
     * Compute the user's current consecutive-day streak from a set of
     * completed-date keys. The walk starts from "today" (or "yesterday" if
     * today isn't completed) and counts back day-by-day.
     *
     * Pure function — visible for testing.
     */
    @VisibleForTesting
    fun calculateStreak(completedDates: Set<String>): Int =
        StreakCalculator.calculate(completedDates)

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

    suspend fun updateCategory(category: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CURRENT_CATEGORY] = category
        }
    }

    val sessionHistoryFlow: Flow<List<FocusSession>> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(androidx.datastore.preferences.core.emptyPreferences()) else throw exception
        }
        .map { preferences ->
            (preferences[PreferencesKeys.SESSION_HISTORY] ?: "").decodeSessionHistory()
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

    suspend fun setBonusMinutes(minutes: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.BONUS_MINUTES] = minutes.coerceAtLeast(0)
        }
    }

    suspend fun consumeBonusMinutes(): Int {
        var consumed = 0
        context.dataStore.edit { preferences ->
            consumed = preferences[PreferencesKeys.BONUS_MINUTES] ?: 0
            preferences[PreferencesKeys.BONUS_MINUTES] = 0
        }
        return consumed
    }

    /**
     * Reset only the user-progress-related preferences, leaving the user's
     * personal settings (theme, focus duration, reminder, etc.) intact.
     */
    suspend fun resetProgress() {
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.COMPLETED_DATES)
            preferences.remove(PreferencesKeys.TOTAL_SESSIONS)
            preferences.remove(PreferencesKeys.TOTAL_FOCUS_MINUTES)
            preferences.remove(PreferencesKeys.APP_LAUNCH_COUNT)
            preferences.remove(PreferencesKeys.BONUS_MINUTES)
            preferences.remove(PreferencesKeys.SESSION_HISTORY)
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
    val appLaunchCount: Int,
    val bonusMinutes: Int = 0,
    val focusCategory: String = FocusCategories.first().id
) {
    companion object {
        val DEFAULT = UserPreferences(
            completedDates = emptySet(),
            currentStreak = 0,
            totalSessions = 0,
            totalFocusMinutes = 0,
            focusDuration = 25,
            theme = "System",
            reminderHour = 9,
            reminderMinute = 0,
            autoStartBreak = false,
            dailyReminderEnabled = false,
            soundEffectsEnabled = true,
            appLaunchCount = 0,
            bonusMinutes = 0,
            focusCategory = FocusCategories.first().id
        )
    }
}
