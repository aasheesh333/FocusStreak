package com.focusstreak.app.ads

import android.content.Context
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.focusstreak.app.BuildConfig
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

/**
 * Inline banner ad composable. Renders a 320x50 anchored banner at the
 * bottom of its parent.
 *
 * Pass [useTestAdUnit] = true to swap in Google's official test banner
 * (always serves). The unit ID is read from BuildConfig.ADMOB_BANNER_ID
 * for production builds.
 *
 * The AdView is created once per composition; its loadAd call is fired
 * on first attach via AndroidView's factory block.
 */
@Composable
fun BannerAd(
    modifier: Modifier = Modifier,
    useTestAdUnit: Boolean = false
) {
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context: Context ->
            val adView = AdView(context).apply {
                adUnitId = if (useTestAdUnit) {
                    GOOGLE_TEST_BANNER_ID
                } else {
                    BuildConfig.ADMOB_BANNER_ID
                }
                setAdSize(AdSize.BANNER)
            }
            android.util.Log.i(
                "BannerAd",
                "Loading banner (unit=${adView.adUnitId}, testMode=$useTestAdUnit)"
            )
            adView.loadAd(AdRequest.Builder().build())
            adView
        }
    )
}

private const val GOOGLE_TEST_BANNER_ID =
    "ca-app-pub-3940256099942544/6300978111"
