package com.focusstreak.app.ads

import android.app.Activity
import android.content.Context
import com.focusstreak.app.BuildConfig
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

class RewardedAdManager(private val context: Context) {

    private var rewardedAd: RewardedAd? = null

    fun loadAd() {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            BuildConfig.ADMOB_REWARDED_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    rewardedAd = null
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                }
            }
        )
    }

    fun showAd(activity: Activity, onAdNotReady: () -> Unit = {}, onRewardEarned: () -> Unit) {
        if (rewardedAd != null) {
            rewardedAd?.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedAd = null
                    loadAd()
                }
            }
            rewardedAd?.show(activity) {
                onRewardEarned()
            }
        } else {
            onAdNotReady()
        }
    }
}
