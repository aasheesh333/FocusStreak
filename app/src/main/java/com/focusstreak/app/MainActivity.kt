package com.focusstreak.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.focusstreak.app.data.UserPreferences
import com.focusstreak.app.navigation.AppNavigation
import com.focusstreak.app.ui.theme.FocusStreakTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Handle the permission result here
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Increment App Launch Count only if this is a fresh start (savedInstanceState is null)
        // This prevents incrementing on configuration changes like rotation.
        if (savedInstanceState == null) {
            lifecycleScope.launch {
                (application as FocusStreakApplication).userPreferencesRepository.incrementAppLaunchCount()
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        setContent {
            val userPreferences by (application as FocusStreakApplication).userPreferencesRepository.userPreferencesFlow.collectAsState(
                initial = UserPreferences(emptySet(), 0, 0, 0, 25, "System", 9, 0, false, false, true, 0)
            )
            FocusStreakTheme(theme = userPreferences.theme) {
                AppNavigation()
            }
        }
    }
}
