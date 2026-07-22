package com.focusstreak.app.data

import org.json.JSONArray
import org.json.JSONObject

/**
 * A single completed focus session.
 *
 * Kept intentionally small so the JSON history stays compact in DataStore.
 */
data class FocusSession(
    val timestampMillis: Long,
    val durationMinutes: Int,
    val category: String,
    val completed: Boolean = true
)

private const val KEY_TIMESTAMP = "t"
private const val KEY_DURATION = "d"
private const val KEY_CATEGORY = "c"
private const val KEY_COMPLETED = "x"

fun FocusSession.toJson(): JSONObject = JSONObject().apply {
    put(KEY_TIMESTAMP, timestampMillis)
    put(KEY_DURATION, durationMinutes)
    put(KEY_CATEGORY, category)
    put(KEY_COMPLETED, completed)
}

private fun JSONObject.toFocusSession(): FocusSession = FocusSession(
    timestampMillis = optLong(KEY_TIMESTAMP, 0L),
    durationMinutes = optInt(KEY_DURATION, 0),
    category = optString(KEY_CATEGORY, "General"),
    completed = optBoolean(KEY_COMPLETED, true)
)

fun List<FocusSession>.encodeToJson(): String =
    JSONArray().apply { forEach { put(it.toJson()) } }.toString()

fun String.decodeSessionHistory(): List<FocusSession> =
    try {
        val array = JSONArray(this)
        List(array.length()) { index -> array.getJSONObject(index).toFocusSession() }
    } catch (_: Exception) {
        emptyList()
    }

/** Predefined categories shown in the UI. */
val FocusCategories = listOf(
    "General",
    "Work",
    "Study",
    "Reading",
    "Exercise",
    "Deep Work"
)
