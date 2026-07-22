package com.focusstreak.app

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import com.focusstreak.app.ads.ConsentManager
import com.focusstreak.app.data.UserPreferences
import com.focusstreak.app.navigation.AppNavigation
import com.focusstreak.app.notification.OneSignalManager
import com.focusstreak.app.ui.theme.FocusStreakTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Required on Android 15+ and mandatory for apps targeting Android 16.
        // Makes the app draw behind the system bars; Compose screens apply
        // the necessary insets padding.
        enableEdgeToEdge()

        // Note: we previously used androidx.core:core-splashscreen here,
        // but its postSplashScreenTheme transition was racing with
        // setContent and throwing "Window couldn't find content container
        // view" on some devices. The Android 12+ native splash screen
        // (auto-applied for targetSdk >= 31 via windowSplashScreenBackground
        // in the activity theme) is sufficient and race-free. Pre-Android
        // 12 devices get a brief blank window of the splash color.

        // GDPR / EEA consent must be collected before any ad request.
        // The manager calls onComplete() exactly once, whether or not
        // consent is required. We use the callback to trigger the
        // first ad load for this session — this is what avoids the
        // "first session in EEA/UK = NO_FILL" race where a load
        // request beats the consent gather.
        ConsentManager(this).requestConsentIfNeeded(this) {
            (application as FocusStreakApplication).loadAllAds()
        }

        // Increment App Launch Count only if this is a fresh start (savedInstanceState is null)
        // This prevents incrementing on configuration changes like rotation.
        if (savedInstanceState == null) {
            lifecycleScope.launch {
                (application as FocusStreakApplication).userPreferencesRepository.incrementAppLaunchCount()
            }
        }

        // Note: POST_NOTIFICATIONS permission is requested at the point of
        // use (SettingsScreen, when the user toggles the daily reminder).
        // We deliberately do not request it on every cold start here.

        // Workaround for androidx.activity.compose 1.8.2's
        // ComponentActivity.setContent older behavior. On some OEM Android 12+
        // builds with a legacy framework theme the decor view's content frame
        // can be null at this point, so setContent's findViewById(...)
        // .getChildAt(0) would throw a NPE. Forcing installDecor() (via
        // setContentView with a transparent empty View) makes sure the
        // content frame exists before setContent runs. This has been kept
        // as a defensive measure even though activity-compose 1.9+ fixed
        // the unsafe cast.
        ensureContentFrame()

        // Diagnostic: catch any throwable from setContent / the initial
        // composition and show a fallback UI so the user sees a friendly
        // message instead of a black screen, and so we can capture the
        // full stack in logcat for the upcoming crash investigation.
        try {
            setContent {
                val userPreferences by (application as FocusStreakApplication).userPreferencesRepository.userPreferencesFlow.collectAsState(
                    initial = UserPreferences.DEFAULT
                )
                FocusStreakTheme(theme = userPreferences.theme) {
                    AppNavigation()
                }
            }

            // Set up the OneSignal push-subscription observer so we can show
            // the official integration-complete dialog once the device is
            // registered. This is gated to show only once.
            OneSignalManager.setupPushSubscriptionObserver(this)
        } catch (t: Throwable) {
            Log.e(TAG, "setContent / initial composition failed", t)
            showFallbackUi(t)
        }
    }

    /**
     * Forces the activity's content frame (android.R.id.content) to
     * exist. See the comment in onCreate for why this is needed.
     */
    private fun ensureContentFrame() {
        val decor = window.decorView
        if (decor.findViewById<ViewGroup>(android.R.id.content) == null) {
            setContentView(View(this))
        }
    }

    /**
     * Last-resort UI shown when the normal Compose tree cannot be built.
     * Plain framework TextView so we don't depend on Compose being healthy.
     * The full exception is already logged via [Log.e]; this is just a
     * user-visible message so the app doesn't appear frozen/black.
     */
    private fun showFallbackUi(throwable: Throwable) {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#0F0121"))
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setPadding(48, 96, 48, 48)
            gravity = Gravity.CENTER
        }
        val title = TextView(this).apply {
            text = getString(R.string.fallback_title)
            setTextColor(Color.WHITE)
            textSize = 22f
            gravity = Gravity.CENTER
        }
        val detail = TextView(this).apply {
            text = getString(
                R.string.fallback_detail_template,
                throwable.javaClass.simpleName,
                throwable.message ?: "(no message)"
            )
            setTextColor(Color.parseColor("#CCCCCC"))
            textSize = 14f
            gravity = Gravity.CENTER
            setPadding(0, 24, 0, 0)
        }
        val hint = TextView(this).apply {
            text = getString(R.string.fallback_logcat_hint)
            setTextColor(Color.parseColor("#888888"))
            textSize = 12f
            gravity = Gravity.CENTER
            setPadding(0, 48, 0, 0)
        }
        root.addView(title)
        root.addView(detail)
        root.addView(hint)
        setContentView(root)
    }

    private companion object {
        const val TAG = "MainActivity"
    }
}

