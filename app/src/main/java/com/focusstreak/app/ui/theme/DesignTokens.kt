package com.focusstreak.app.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Centralized design tokens for FocusStreak. All UI code should source
 * colors, radii, durations, and spacing from here — not from local
 * file-level `val`s. This keeps light & dark screens visually
 * consistent (Home / Progress / SessionCompleteDialog follow the dark
 * palette; Settings follows light) and makes a future theming
 * overhaul a one-file change.
 *
 * Naming follows a `<role><Modifier>` convention (BackgroundDark,
 * TextSecondary, Accent…). Tokens are intentionally `val`s (singletons)
 * rather than MaterialTheme extension functions because the same hex
 * is used across light *and* dark screens — the screen decides which
 * palette it pulls from.
 */

// region Brand & Accent -----------------------------------------------------
val BrandPurple = Color(0xFF7000FF)
val BrandPurpleLight = Color(0xFFA040FF)
val BrandPurpleDeep = Color(0xFF5000B8)
val BrandOrange = Color(0xFFFF5722)
val BrandFreezeBlue = Color(0xFFB4D8F5)
val BrandFreezeBlueAccent = Color(0xFF1976D2)
// endregion

// region Dark screen palette (Home / Progress / SessionCompleteDialog) ------
val DarkBackground = Color(0xFF0F0A1E)
val DarkSurface = Color(0xFF1C162E)
val DarkSurfaceElevated = Color(0xFF231630)
val DarkCardBackground = Color(0xFF1C182F)
val DarkIconBgPurple = Color(0xFF2D2644)
val DarkIconBgTeal = Color(0xFF1E2D2F)
val DarkOverlay = Color(0xFF1A1625)
val DarkBadgeGreen = Color(0xFF00C853)
// endregion

// region Light screen palette (Settings) -----------------------------------
val LightBackground = Color(0xFFF8F9FA)
val LightCardBackground = Color.White
val LightDivider = Color(0xFFF0F0F0)
val LightIconBgPurple = Color(0xFFECE6F0)
val LightIconTintPurple = Color(0xFF6750A4)
val LightIconBgTeal = Color(0xFFE0F2F1)
val LightIconTintTeal = Color(0xFF009688)
val LightIconBgBlue = Color(0xFFE3F2FD)
val LightIconTintBlue = Color(0xFF2196F3)
val LightIconBgOrange = Color(0xFFFFE0B2)
val LightIconTintOrange = Color(0xFFFF9800)
val LightToggleActiveTrack = Color(0xFF6750A4)
val LightToggleInactiveTrack = Color(0xFFE0E0E0)
val LightTrackSurface = Color(0xFFF5F5F5)
val LightSurfaceGray = Color(0xFFDADADA)
// endregion

// region Text ---------------------------------------------------------------
val TextPrimaryLight = Color.White   // use on dark surfaces
val TextPrimaryDark = Color.Black     // use on light surfaces
val TextSecondary = Color(0xFF888888) // shared by both palettes — works on both
// endregion

// region Radii ---------------------------------------------------------------
/** 4dp — small controls, input chips */
val RadiusS = 4.dp
/** 12dp — segments, small cards */
val RadiusM = 12.dp
/** 16dp — info cards */
val RadiusL = 16.dp
/** 20dp — chips */
val RadiusXL = 20.dp
/** 24dp — primary streak card */
val RadiusXXL = 24.dp
// endregion

// region Motion durations (ms as Int, since Compose animations accept Int) --
/** 150ms — micro-interactions: button press, toggle */
const val DurFast = 150
/** 300ms — enter/exit transitions, count-up animations */
const val DurMedium = 300
/** 500ms — multi-step reveals: empty-state fade-in */
const val DurSlow = 500
/** 1200ms — celebratory loops: confetti, pulse */
const val DurLong = 1200
// endregion

// region Spacing scale (4dp grid) ------------------------------------------
val SpaceXS = 4.dp   // tight gaps next to icons/chevrons
val SpaceS = 8.dp   // chip internal spacing
val SpaceM = 12.dp  // row internal padding
val SpaceL = 16.dp  // section internal padding
val SpaceXL = 24.dp // section break between groups
// endregion

// region Typography (used outside MaterialTheme.typography) -----------------
/** Streak counter display */
val DisplayStreakSize = 36.sp
/** Section header caption (homescreen "FOCUS STREAK") */
val SectionHeaderSize = 12.sp
val MomentumTextSize = 14.sp
val ChipTextSize = 12.sp
val CardTitleSize = 16.sp
val CardBodySize = 13.sp
val HelperTextSize = 11.sp
// endregion
