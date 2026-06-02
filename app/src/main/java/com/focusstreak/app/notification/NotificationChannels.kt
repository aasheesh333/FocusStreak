package com.focusstreak.app.notification

/**
 * Centralised notification channel IDs. Keep these constants in one place
 * so the scheduler, the worker, and the application class all agree.
 */
object NotificationChannels {
    const val DAILY_REMINDER = "focus_streak_reminder"

    const val DAILY_REMINDER_WORK_NAME = "focus_streak_reminder_work"
}
