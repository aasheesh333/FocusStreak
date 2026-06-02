package com.focusstreak.app.data

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

/**
 * Pure date-arithmetic for the streak feature. Kept separate from
 * [UserPreferencesRepository] so it can be unit-tested without Android
 * dependencies (no Context, no DataStore).
 */
object StreakCalculator {

    /**
     * Single source of truth for the "yyyy-MM-dd" date string used to key
     * completed-session days. We use [Locale.ROOT] (not [Locale.getDefault])
     * so that formatting and parsing are symmetric across all locales and
     * a user who changes their device locale cannot lose their streak.
     */
    val DATE_KEY_FORMAT: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).apply {
        timeZone = TimeZone.getDefault()
    }

    /**
     * Compute the user's current consecutive-day streak from a set of
     * completed-date keys. The walk starts from "today" (or "yesterday" if
     * today isn't completed) and counts back day-by-day.
     */
    fun calculate(completedDates: Set<String>, now: Calendar = Calendar.getInstance()): Int {
        if (completedDates.isEmpty()) return 0
        val sortedDates = completedDates.mapNotNull {
            try {
                DATE_KEY_FORMAT.parse(it)
            } catch (_: Exception) {
                null
            }
        }.sortedDescending()

        if (sortedDates.isEmpty()) return 0

        var streak = 0
        val today = now.clone() as Calendar
        val todayStr = DATE_KEY_FORMAT.format(today.time)
        val isTodayCompleted = completedDates.contains(todayStr)

        if (isTodayCompleted) {
            streak = 1
            today.add(Calendar.DAY_OF_YEAR, -1)
            for (i in 1 until sortedDates.size) {
                val candidate = Calendar.getInstance().apply { time = sortedDates[i] }
                if (isSameDay(today, candidate)) {
                    streak++
                    today.add(Calendar.DAY_OF_YEAR, -1)
                } else {
                    break
                }
            }
        } else {
            today.add(Calendar.DAY_OF_YEAR, -1)
            for (date in sortedDates) {
                val candidate = Calendar.getInstance().apply { time = date }
                if (isSameDay(today, candidate)) {
                    streak++
                    today.add(Calendar.DAY_OF_YEAR, -1)
                } else {
                    break
                }
            }
        }
        return streak
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
}
