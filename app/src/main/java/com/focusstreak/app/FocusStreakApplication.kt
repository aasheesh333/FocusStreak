package com.focusstreak.app

import android.app.Application
import com.focusstreak.app.data.UserPreferencesRepository

class FocusStreakApplication : Application() {
    lateinit var userPreferencesRepository: UserPreferencesRepository

    override fun onCreate() {
        super.onCreate()
        userPreferencesRepository = UserPreferencesRepository(this)
    }
}
