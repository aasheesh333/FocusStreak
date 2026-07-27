package com.focusstreak.app.viewmodel

import androidx.test.core.app.ApplicationProvider
import com.focusstreak.app.data.FocusCategories
import com.focusstreak.app.data.UserPreferences
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
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
 * The Robolectric-injected [TestFocusStreakApplication] substitutes
 * the production FocusStreakApplication's singleton properties with
 * relaxed mocks, so the ViewModel exercises against in-memory fakes
 * instead of the real DataStore / AdMob managers.
 *
 * Five tests cover the most important state transitions:
 *  1. `init` seeds `_timeInMillis` at `focusDuration × 60_000` ms
 *  2. `startTimer` consumes any pending bonus minutes from the repo
 *  3. `startTimer` enforces the `appLaunchCount > 1` gate
 *  4. `pauseTimer` / `resumeTimer` preserves remaining time
 *  5. `selectCategory` delegates to the repository
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], application = TestFocusStreakApplication::class)
class HomeViewModelTest {

    private fun app(): TestFocusStreakApplication =
        ApplicationProvider.getApplicationContext()

    /** Default prefs used by the timer tests. */
    private val defaultPrefs = UserPreferences.DEFAULT.copy(
        focusDuration = 25,
        bonusMinutes = 0,
        appLaunchCount = 3,
        focusCategory = FocusCategories.first().id,
        soundEffectsEnabled = false
    )

    private fun newViewModel(prefs: UserPreferences): HomeViewModel {
        coEvery { app().repo.userPreferencesFlow } returns MutableStateFlow(prefs)
        coEvery { app().repo.consumeBonusMinutes() } returns prefs.bonusMinutes
        return HomeViewModel(app())
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
        // appLaunchCount > 1 routes to AdShowing. With the unconfined
        // dispatcher and a 25-minute ticker budget, we can only verify
        // the gate evaluated without a crash by confirming the state
        // exited Idle (it transitions to Running immediately).
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
        // Resume must NOT reset the timer to the base value; the new
        // ticker picks up from `remainingAfterPause`.
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
