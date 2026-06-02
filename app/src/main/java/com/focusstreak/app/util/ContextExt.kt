package com.focusstreak.app.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

/**
 * Walk a ContextWrapper chain to find the underlying Activity, if any.
 * Returns null if the context is not hosted inside an Activity (e.g. an
 * Application context, a Preview context, or a Service).
 *
 * Use this instead of an unsafe `as Activity` cast, which throws
 * ClassCastException in previews and tests.
 */
fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
