package com.focusstreak.app.ads

import android.app.Activity
import android.content.Context
import com.focusstreak.app.BuildConfig
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

/**
 * Wraps an interstitial ad unit.
 *
 * Provides a [loadAd] / [showAd] pair, plus a [useTestAdUnit] flag for
 * diagnostic builds: when true, requests go to Google's
 * always-fills test ad unit ID instead of the production one. This
 * is the fastest way to distinguish "the ad flow is broken" from
 * "the ad unit has no fill in this region".
 *
 *   - Test ad shows fine, real ad does not → production ad unit
 *     has 0 fill in this region/category, or the unit is misconfigured
 *     in AdMob console.
 *   - Test ad does not show → SDK init, consent, or device issue.
 */
class InterstitialAdManager(private val context: Context) {

    private var interstitialAd: InterstitialAd? = null
    private var sessionCount = 0

    /**
     * True = always use Google's official test interstitial unit
     * (always serves a sample ad). Default false (production).
     */
    var useTestAdUnit: Boolean = false

    private fun adUnitId(): String =
        if (useTestAdUnit) GOOGLE_TEST_INTERSTITIAL_ID
        else BuildConfig.ADMOB_INTERSTITIAL_ID

    fun loadAd() {
        val adRequest = AdRequest.Builder().build()
        val unitId = adUnitId()
        android.util.Log.i(
            "InterstitialAdManager",
            "Requesting ad load (unit=$unitId, testMode=$useTestAdUnit)"
        )
        InterstitialAd.load(
            context,
            unitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    // Log the error code/message so we can diagnose fill-rate
                    // issues from logcat. Common codes:
                    //   0 = INTERNAL_ERROR
                    //   1 = INVALID_REQUEST (bad ad unit ID — check CI env)
                    //   2 = NETWORK_ERROR
                    //   3 = NO_FILL (region/category has no inventory, or
                    //       no UMP consent yet in EEA/UK)
                    //  10 = NO_INVENTORY
                    android.util.Log.w(
                        "InterstitialAdManager",
                        "Ad failed to load: code=${adError.code} " +
                            "domain=${adError.domain} msg=${adError.message} " +
                            "testMode=$useTestAdUnit"
                    )
                    interstitialAd = null
                }

                override fun onAdLoaded(ad: InterstitialAd) {
                    android.util.Log.i(
                        "InterstitialAdManager",
                        "Ad loaded (unit=$unitId, testMode=$useTestAdUnit)"
                    )
                    interstitialAd = ad
                }
            }
        )
    }

    fun showAd(activity: Activity, onAdDismissed: () -> Unit) {
        sessionCount++
        val ad = interstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    onAdDismissed()
                    interstitialAd = null
                    loadAd()
                }

                override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                    android.util.Log.w("InterstitialAdManager", "Ad failed to show: ${adError.message}")
                    interstitialAd = null
                    loadAd()
                    onAdDismissed()
                }
            }
            ad.show(activity)
        } else {
            android.util.Log.w(
                "InterstitialAdManager",
                "showAd() called but no ad loaded yet " +
                    "(testMode=$useTestAdUnit). Falling through to onAdDismissed."
            )
            onAdDismissed()
        }
    }

    companion object {
        // Google's official always-fills test interstitial unit ID.
        // Safe to ship in any build; serves a sample interstitial.
        const val GOOGLE_TEST_INTERSTITIAL_ID =
            "ca-app-pub-3940256099942544/1033173712"
    }
}
