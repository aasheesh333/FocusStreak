package com.focusstreak.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.focusstreak.app.FocusStreakApplication
import com.focusstreak.app.data.UserPreferences
import com.focusstreak.app.data.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ProgressViewModel(application: Application) : AndroidViewModel(application) {

    private val userPreferencesRepository: UserPreferencesRepository = (application as FocusStreakApplication).userPreferencesRepository

    private val _userPreferences = MutableStateFlow(UserPreferences(emptySet(), 0, 0, 0, 25, "System", 9, 0, false, false, true, 0))
    val userPreferences: StateFlow<UserPreferences> = _userPreferences

    private val _weekDays = MutableStateFlow<List<Triple<String, Boolean, Boolean>>>(emptyList())
    val weekDays: StateFlow<List<Triple<String, Boolean, Boolean>>> = _weekDays

    init {
        viewModelScope.launch {
            userPreferencesRepository.userPreferencesFlow.collect {
                _userPreferences.value = it
                updateWeekDays(it.completedDates)
            }
        }
    }

    private fun updateWeekDays(completedDates: Set<String>) {
        val calendar = java.util.Calendar.getInstance()
        val today = calendar.get(java.util.Calendar.DAY_OF_WEEK)
        _weekDays.value = (0..6).map {
            val dayCalendar = java.util.Calendar.getInstance()
            dayCalendar.add(java.util.Calendar.DAY_OF_YEAR, it - 6)
            val dayName = when (dayCalendar.get(java.util.Calendar.DAY_OF_WEEK)) {
                1 -> "S"
                2 -> "M"
                3 -> "T"
                4 -> "W"
                5 -> "T"
                6 -> "F"
                7 -> "S"
                else -> ""
            }
            val dateString = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(dayCalendar.time)
            Triple(dayName, completedDates.contains(dateString), dayCalendar.get(java.util.Calendar.DAY_OF_WEEK) == today)
        }
    }
}
