package com.focusstreak.app.viewmodel

import com.focusstreak.app.FocusStreakApplication
import com.focusstreak.app.ads.InterstitialAdManager
import com.focusstreak.app.ads.RewardedAdManager
import com.focusstreak.app.data.UserPreferencesRepository
import io.mockk.mockk

/**
 * Top-level Robolectric Application that substitutes the production
 * FocusStreakApplication's three late-init properties with relaxed
 * mocks. Declared as a top-level class (not nested) so that
 * `@Config(application = TestFocusStreakApplication::class)` resolves
 * correctly from Robolectric's annotation processor.
 *
 * `onCreate` is intentionally a no-op so MobileAds / OneSignal / the
 * notification channels aren't spun up under the Robolectric runtime.
 */
class TestFocusStreakApplication : FocusStreakApplication() {

    val repo: UserPreferencesRepository = mockk(relaxed = true)
    val interstitial: InterstitialAdManager = mockk(relaxed = true)
    val rewarded: RewardedAdManager = mockk(relaxed = true)

    override val userPreferencesRepository get() = repo
    override val interstitialAdManager get() = interstitial
    override val rewardedAdManager get() = rewarded

    override fun onCreate() {
        // Skip the real onCreate (MobileAds / OneSignal / channels).
    }
}
