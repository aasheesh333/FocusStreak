package com.focusstreak.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.focusstreak.app.FocusStreakApplication
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

    // Locale.ROOT so the date key is stable across locale changes.
    private val dateKeyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).apply {
        timeZone = TimeZone.getDefault()
    }

    init {
        viewModelScope.launch {
            userPreferencesRepository.userPreferencesFlow.collect {
                _userPreferences.value = it
                updateWeekDays(it.completedDates)
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
}
