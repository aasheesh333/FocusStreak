package com.focusstreak.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.focusstreak.app.FocusStreakApplication
import com.focusstreak.app.data.FocusCategories
import com.focusstreak.app.data.FocusSession
import com.focusstreak.app.data.StreakCalculator
import com.focusstreak.app.data.UserPreferences
import com.focusstreak.app.data.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class ProgressViewModel(application: Application) : AndroidViewModel(application) {

    private val userPreferencesRepository: UserPreferencesRepository = (application as FocusStreakApplication).userPreferencesRepository

    private val _userPreferences = MutableStateFlow(UserPreferences.DEFAULT)
    val userPreferences: StateFlow<UserPreferences> = _userPreferences

    private val _weekDays = MutableStateFlow<List<Triple<String, Boolean, Boolean>>>(emptyList())
    val weekDays: StateFlow<List<Triple<String, Boolean, Boolean>>> = _weekDays

    private val _sessionStats = MutableStateFlow(SessionStats())
    val sessionStats: StateFlow<SessionStats> = _sessionStats

    private val _calendarDays = MutableStateFlow<List<HeatmapCell>>(emptyList())
    val calendarDays: StateFlow<List<HeatmapCell>> = _calendarDays

    // Locale.ROOT so the date key is stable across locale changes.
    private val dateKeyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).apply {
        timeZone = TimeZone.getDefault()
    }

    init {
        viewModelScope.launch {
            userPreferencesRepository.userPreferencesFlow.collect {
                _userPreferences.value = it
                updateWeekDays(it.completedDates)
                _calendarDays.value = buildHeatmap(it.completedDates)
                _sessionStats.value = _sessionStats.value.copy(
                    currentStreak = it.currentStreak,
                    totalSessions = it.totalSessions,
                    totalMinutes = it.totalFocusMinutes,
                    bestStreak = StreakCalculator.calculateBestStreak(it.completedDates)
                )
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.sessionHistoryFlow.collect { history ->
                _sessionStats.value = _sessionStats.value.copy(
                    weeklySessions = weeklySessions(history),
                    weeklyMinutes = weeklyMinutes(history),
                    topCategory = topCategory(history)
                )
            }
        }
    }

    private fun updateWeekDays(completedDates: Set<String>) {
        val calendar = Calendar.getInstance()
        val today = calendar.get(Calendar.DAY_OF_WEEK)
        _weekDays.value = (0..6).map {
            val dayCalendar = Calendar.getInstance()
            dayCalendar.add(Calendar.DAY_OF_YEAR, it - 6)
            val dayName = when (dayCalendar.get(Calendar.DAY_OF_WEEK)) {
                Calendar.SUNDAY -> "Su"
                Calendar.MONDAY -> "Mo"
                Calendar.TUESDAY -> "Tu"
                Calendar.WEDNESDAY -> "We"
                Calendar.THURSDAY -> "Th"
                Calendar.FRIDAY -> "Fr"
                Calendar.SATURDAY -> "Sa"
                else -> ""
            }
            val dateString = dateKeyFormat.format(dayCalendar.time)
            Triple(dayName, completedDates.contains(dateString), dayCalendar.get(Calendar.DAY_OF_WEEK) == today)
        }
    }

    private fun buildHeatmap(completedDates: Set<String>): List<HeatmapCell> {
        val now = Calendar.getInstance()
        val days = mutableListOf<HeatmapCell>()
        // Build 42 days (6 weeks) ending with today at the bottom-right.
        for (i in -41..0) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, i)
            val key = dateKeyFormat.format(cal.time)
            val isToday = i == 0
            days.add(HeatmapCell(isCompleted = completedDates.contains(key), isToday = isToday))
        }
        return days
    }

    private fun weeklySessions(history: List<FocusSession>): Int {
        val oneWeekAgo = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L
        return history.count { it.timestampMillis >= oneWeekAgo && it.completed }
    }

    private fun weeklyMinutes(history: List<FocusSession>): Int {
        val oneWeekAgo = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L
        return history.filter { it.timestampMillis >= oneWeekAgo && it.completed }
            .sumOf { it.durationMinutes }
    }

    private fun topCategory(history: List<FocusSession>): String {
        if (history.isEmpty()) return "—"
        val topId = history.filter { it.completed }
            .groupingBy { it.category }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key ?: return "—"
        return FocusCategories.find { it.id == topId }?.name ?: topId.replaceFirstChar { it.uppercase() }
    }
}

data class SessionStats(
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val totalSessions: Int = 0,
    val totalMinutes: Int = 0,
    val weeklySessions: Int = 0,
    val weeklyMinutes: Int = 0,
    val topCategory: String = "—"
)

data class HeatmapCell(
    val isCompleted: Boolean,
    val isToday: Boolean
)
