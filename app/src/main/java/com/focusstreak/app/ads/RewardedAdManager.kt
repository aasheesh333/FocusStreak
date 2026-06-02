package com.focusstreak.app.ads

import android.app.Activity
import android.content.Context
import com.focusstreak.app.BuildConfig
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

/**
 * Wraps a rewarded ad unit.
 *
 * See [InterstitialAdManager] for the [useTestAdUnit] rationale: when
 * enabled, requests go to Google's always-fills test rewarded unit,
 * which is the fastest way to distinguish "rewarded flow is broken"
 * from "real rewarded unit has 0 fill".
 */
class RewardedAdManager(private val context: Context) {

    private var rewardedAd: RewardedAd? = null

    /**
     * True = always use Google's official test rewarded unit.
     * Default false (production).
     */
    var useTestAdUnit: Boolean = false

    private fun adUnitId(): String =
        if (useTestAdUnit) GOOGLE_TEST_REWARDED_ID
        else BuildConfig.ADMOB_REWARDED_ID

    fun loadAd() {
        val adRequest = AdRequest.Builder().build()
        val unitId = adUnitId()
        android.util.Log.i(
            "RewardedAdManager",
            "Requesting ad load (unit=$unitId, testMode=$useTestAdUnit)"
        )
        RewardedAd.load(
            context,
            unitId,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    // See InterstitialAdManager for code reference.
                    android.util.Log.w(
                        "RewardedAdManager",
                        "Ad failed to load: code=${adError.code} " +
                            "domain=${adError.domain} msg=${adError.message} " +
                            "testMode=$useTestAdUnit"
                    )
                    rewardedAd = null
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    android.util.Log.i(
                        "RewardedAdManager",
                        "Ad loaded (unit=$unitId, testMode=$useTestAdUnit)"
                    )
                    rewardedAd = ad
                }
            }
        )
    }

    fun showAd(
        activity: Activity,
        onAdNotReady: () -> Unit = {},
        onRewardEarned: () -> Unit
    ) {
        val ad = rewardedAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedAd = null
                    loadAd()
                }

                override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                    android.util.Log.w("RewardedAdManager", "Ad failed to show: ${adError.message}")
                    rewardedAd = null
                    loadAd()
                    onAdNotReady()
                }
            }
            ad.show(activity) {
                onRewardEarned()
            }
        } else {
            // Ad isn't ready. This happens when the user taps "Bonus
            // Time" before the first load has completed (e.g. user
            // finished their session in <2s after launch), or after a
            // previous load failed with NO_FILL. We kick off a fresh
            // load here so the next tap has a chance, and tell the
            // caller to ask the user to try again shortly.
            android.util.Log.w(
                "RewardedAdManager",
                "showAd() called but no ad loaded yet " +
                    "(testMode=$useTestAdUnit). Kicking off retry load."
            )
            loadAd()
            onAdNotReady()
        }
    }

    companion object {
        // Google's official always-fills test rewarded unit ID.
        const val GOOGLE_TEST_REWARDED_ID =
            "ca-app-pub-3940256099942544/5224354917"
    }
}
