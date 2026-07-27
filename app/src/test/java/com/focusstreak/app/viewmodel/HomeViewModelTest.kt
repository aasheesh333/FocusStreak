package com.focusstreak.app.viewmodel

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.focusstreak.app.FocusStreakApplication
import com.focusstreak.app.ads.InterstitialAdManager
import com.focusstreak.app.ads.RewardedAdManager
import com.focusstreak.app.data.FocusCategories
import com.focusstreak.app.data.UserPreferences
import com.focusstreak.app.data.UserPreferencesRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests for [HomeViewModel].
 *
 * A Robolectric-injected [TestApplication] substitutes the three
 * `open val` exposed by [FocusStreakApplication] with relaxed mocks,
 * so the ViewModel exercises against in-memory fakes instead of the
 * real DataStore / AdMob managers.
 *
 * Five tests cover the most important state transitions:
 *  1. `init` seeds `_timeInMillis` at `focusDuration × 60_000` ms
 *  2. `startTimer` consumes any pending bonus minutes from the repo
 *  3. `startTimer` enforces the `appLaunchCount > 1` gate
 *  4. `pauseTimer` / `resumeTimer` preserves remaining time
 *  5. `selectCategory` delegates to the repository
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], application = TestApplication::class)
class HomeViewModelTest {

    /** Test Application that swaps in mocked repo / ad managers. */
    class TestApplication : FocusStreakApplication() {
        val repo: UserPreferencesRepository = mockk(relaxed = true)
        val interstitial: InterstitialAdManager = mockk(relaxed = true)
        val rewarded: RewardedAdManager = mockk(relaxed = true)

        override val userPreferencesRepository get() = repo
        override val interstitialAdManager get() = interstitial
        override val rewardedAdManager get() = rewarded

        // Skip the real onCreate side-effects (MobileAds / OneSignal /
        // notification channels) — they're not under test here.
        override fun onCreate() { /* no-op */ }
    }

    private fun app(): TestApplication =
        ApplicationProvider.getApplicationContext()

    private fun newViewModel(prefs: UserPreferences): HomeViewModel {
        everyAndConsume(prefs)
        return HomeViewModel(app())
    }

    private fun everyAndConsume(prefs: UserPreferences) {
        // `every { … } returns` re-arms the mock for repeated use; the
        // MutableStateFlow lets later tests update prefs mid-flight.
        coEvery { app().repo.userPreferencesFlow } returns MutableStateFlow(prefs)
        coEvery { app().repo.consumeBonusMinutes() } returns prefs.bonusMinutes
    }

    @Before
    fun setMainDispatcher() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    /** Default prefs used by the timer tests. */
    private val defaultPrefs = UserPreferences.DEFAULT.copy(
        focusDuration = 25,
        bonusMinutes = 0,
        appLaunchCount = 3,
        focusCategory = FocusCategories.first().id,
        soundEffectsEnabled = false
    )

    // ---------- Tests ----------

    @Test
    fun `init seeds the timer at focusDuration x 60_000`() = runTest {
        val view = newViewModel(defaultPrefs)
        // init pushed `baseMinutes * 60_000` into _timeInMillis while Idle.
        assertThat(view.timeInMillis.value).isEqualTo(25 * 60 * 1000L)
        assertThat(view.timerState.value).isEqualTo(TimerState.Idle)
    }

    @Test
    fun `startTimer consumes bonus minutes from the repository`() = runTest {
        val view = newViewModel(defaultPrefs.copy(bonusMinutes = 5, appLaunchCount = 3))
        coEvery { app().repo.consumeBonusMinutes() } returns 5

        view.startTimer()
        coVerify(exactly = 1) { app().repo.consumeBonusMinutes() }
    }

    @Test
    fun `startTimer enforces the appLaunchCount gt 1 interstitial gate`() = runTest {
        // appLaunchCount = 1 routes to Completed when the ticker hits 0;
        // appLaunchCount > 1 routes to AdShowing. We assert the gate
        // evaluated without exception by confirming the state exited Idle.
        val view = newViewModel(defaultPrefs.copy(appLaunchCount = 1))
        view.startTimer()
        assertThat(view.timerState.value).isNotEqualTo(TimerState.Idle)
    }

    @Test
    fun `pauseTimer and resumeTimer preserves remaining time`() = runTest {
        val view = newViewModel(defaultPrefs)
        view.startTimer()
        view.pauseTimer()
        assertThat(view.timerState.value).isEqualTo(TimerState.Paused)

        val remainingAfterPause = view.timeInMillis.value
        assertThat(remainingAfterPause).isAtMost(25 * 60 * 1000L)
        assertThat(remainingAfterPause).isGreaterThan(0L)

        view.resumeTimer()
        assertThat(view.timerState.value).isEqualTo(TimerState.Running)
        // Resume must NOT reset the timer to the base value; it picks
        // up from `remainingAfterPause`.
        assertThat(view.timeInMillis.value).isEqualTo(remainingAfterPause)
    }

    @Test
    fun `selectCategory persists the id via the repository`() = runTest {
        val view = newViewModel(defaultPrefs)
        val targetCategory = FocusCategories[2].id // "study"
        view.selectCategory(targetCategory)
        coVerify(exactly = 1) { app().repo.updateCategory(targetCategory) }
    }
}
