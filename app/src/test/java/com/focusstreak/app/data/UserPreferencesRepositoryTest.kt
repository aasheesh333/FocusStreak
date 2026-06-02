package com.focusstreak.app.data

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.Calendar
import java.util.Date

/**
 * Unit tests for [StreakCalculator.calculate].
 *
 * The streak is computed from a set of "yyyy-MM-dd" date keys. To make the
 * tests deterministic we anchor "now" with the [now] parameter rather than
 * relying on the wall clock, and we use the same [StreakCalculator.DATE_KEY_FORMAT]
 * (Locale.ROOT) the production code uses.
 */
class UserPreferencesRepositoryTest {

    // Pin a fixed "now" so tests don't drift with the calendar.
    private val now: Calendar = Calendar.getInstance().apply {
        set(2025, Calendar.JANUARY, 10, 12, 0, 0)
        set(Calendar.MILLISECOND, 0)
    }

    private fun key(daysBefore: Int): String {
        val cal = now.clone() as Calendar
        cal.add(Calendar.DAY_OF_YEAR, -daysBefore)
        return StreakCalculator.DATE_KEY_FORMAT.format(cal.time)
    }

    @Test
    fun `empty set returns 0`() {
        assertThat(StreakCalculator.calculate(emptySet(), now)).isEqualTo(0)
    }

    @Test
    fun `only today completed returns 1`() {
        assertThat(StreakCalculator.calculate(setOf(key(0)), now)).isEqualTo(1)
    }

    @Test
    fun `only yesterday completed returns 1 (yesterday counts as the active streak)`() {
        // If today is not yet completed but yesterday is, the active
        // streak is still 1. It only drops to 0 once "today" passes
        // without a completion.
        assertThat(StreakCalculator.calculate(setOf(key(1)), now)).isEqualTo(1)
    }

    @Test
    fun `today and yesterday completed returns 2`() {
        assertThat(StreakCalculator.calculate(setOf(key(0), key(1)), now)).isEqualTo(2)
    }

    @Test
    fun `three consecutive days returns 3`() {
        assertThat(StreakCalculator.calculate(setOf(key(0), key(1), key(2)), now)).isEqualTo(3)
    }

    @Test
    fun `gap breaks the streak at the gap`() {
        // today, yesterday, then a 5-day-old completion -> streak is 2
        assertThat(
            StreakCalculator.calculate(setOf(key(0), key(1), key(5)), now)
        ).isEqualTo(2)
    }

    @Test
    fun `unparseable dates are ignored`() {
        assertThat(
            StreakCalculator.calculate(setOf(key(0), "not-a-date", "2025-13-99"), now)
        ).isEqualTo(1)
    }

    @Test
    fun `future dates do not extend the streak`() {
        // Today + a date 1 day in the future. Streak is anchored at today.
        val futureKey = StreakCalculator.DATE_KEY_FORMAT.format(
            (now.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }.time
        )
        assertThat(StreakCalculator.calculate(setOf(key(0), futureKey), now)).isEqualTo(1)
    }
}
