package com.focusstreak.app.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log

/**
 * Resolves the correct app-store deep link for the store the app was actually
 * installed from, instead of hardcoding Google Play.
 *
 * OPPO rejected 2.3.0/2.3.1 for "Mandatorily update / download from Google
 * Play". Sending an OPPO App Market user to a Play Store listing — even from a
 * voluntary "Rate us" tap — points them at a competing store and, on devices
 * without Play installed, throws ActivityNotFoundException. We therefore:
 *
 *  - open the OPPO App Market when the app came from it,
 *  - open Play when the app came from Play,
 *  - and report "unavailable" when no store can handle the link, so callers
 *    can hide the entry point entirely rather than showing a dead row.
 */
object StoreLinks {

    private const val TAG = "StoreLinks"

    private const val PLAY_PACKAGE = "com.android.vending"

    /** OPPO App Market / realme Store / OnePlus Store share this package. */
    private const val OPPO_MARKET_PACKAGE = "com.heytap.market"
    private const val OPPO_MARKET_LEGACY_PACKAGE = "com.oppo.market"

    /**
     * Intent that opens this app's listing in whichever store is present, or
     * null when no store on the device can handle it (hide the UI in that case).
     */
    fun storeListingIntent(context: Context): Intent? {
        val pkg = context.packageName
        val candidates = buildList {
            val installer = installerPackage(context)
            // Prefer the store the app was installed from.
            when (installer) {
                OPPO_MARKET_PACKAGE, OPPO_MARKET_LEGACY_PACKAGE ->
                    add(oppoMarketIntent(pkg, installer))
                PLAY_PACKAGE -> add(playIntent(pkg))
            }
            // Then whatever else is installed.
            add(oppoMarketIntent(pkg, OPPO_MARKET_PACKAGE))
            add(oppoMarketIntent(pkg, OPPO_MARKET_LEGACY_PACKAGE))
            add(playIntent(pkg))
        }
        return candidates.firstOrNull { canHandle(context, it) }
    }

    private fun playIntent(pkg: String) =
        Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$pkg"))
            .setPackage(PLAY_PACKAGE)

    private fun oppoMarketIntent(pkg: String, marketPackage: String) =
        Intent(Intent.ACTION_VIEW, Uri.parse("oaps://mk/developer/comment?pkg=$pkg"))
            .setPackage(marketPackage)

    private fun canHandle(context: Context, intent: Intent): Boolean =
        runCatching {
            context.packageManager.queryIntentActivities(intent, 0).isNotEmpty()
        }.getOrElse { false }

    @Suppress("DEPRECATION")
    private fun installerPackage(context: Context): String? = runCatching {
        val pm = context.packageManager
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            pm.getInstallSourceInfo(context.packageName).installingPackageName
        } else {
            pm.getInstallerPackageName(context.packageName)
        }
    }.getOrElse {
        Log.w(TAG, "Could not read installer package", it)
        null
    }
}
