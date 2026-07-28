package com.focusstreak.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.collectLatest

/**
 * Counter that animates from its previous integer value to the new
 * one with a soft slide+fade transition. Used by the streak display on
 * the Home and Progress screens so the number doesn't "jump" when a
 * session completes.
 */
@Composable
fun AnimatedCount(
    target: Int,
    content: @Composable (Int) -> Unit
) {
    var current by remember { mutableIntStateOf(target) }
    LaunchedEffect(target) {
        snapshotFlow { target }
            .collectLatest { new ->
                // Step through intermediate values so a 7→10 transition
                // visibly counts up, but a 0→0 doesn't fire any frames.
                val from = current
                if (new == from) return@collectLatest
                val direction = if (new > from) 1 else -1
                var v = from + direction
                while (v != new) {
                    current = v
                    kotlinx.coroutines.delay(80)
                    v += direction
                }
                current = new
            }
    }

    AnimatedContent(
        targetState = current,
        transitionSpec = {
            (slideInVertically(
                animationSpec = tween(durationMillis = 220),
                initialOffsetY = { full -> if (targetState > initialState) full else -full }
            ) + fadeIn(tween(220))) togetherWith
                (slideOutVertically(
                    animationSpec = tween(durationMillis = 220),
                    targetOffsetY = { full -> if (targetState > initialState) -full else full }
                ) + fadeOut(tween(220)))
        },
        label = "AnimatedCount"
    ) { value -> content(value) }
}

/**
 * One-shot fade-in. Animates from invisible (0) to visible (1) after
 * the given delay. The wrapped content is composed only when visible,
 * which keeps first-paint cost low when several sections are
 * staggered.
 */
@Composable
fun FadeIn(
    delayMillis: Int = 0,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(delayMillis.toLong())
        visible = 1
    }
    AnimatedContent(
        targetState = visible,
        transitionSpec = {
            fadeIn(tween(durationMillis = 300)) togetherWith
                fadeOut(tween(durationMillis = 0))
        },
        label = "FadeIn"
    ) { state -> if (state == 1) content() }
}

/**
 * Combined slide+fade — use for section reveal (e.g., progress cards
 * appearing after the streak header animates in).
 */
@Composable
fun SlideUp(
    delayMillis: Int = 0,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(delayMillis.toLong())
        visible = 1
    }
    AnimatedContent(
        targetState = visible,
        transitionSpec = {
            val slide = slideInVertically(
                animationSpec = tween(durationMillis = 320),
                initialOffsetY = { it / 2 }
            )
            val fade = fadeIn(animationSpec = tween(durationMillis = 320))
            (slide + fade) togetherWith fadeOut(tween(0))
        },
        label = "SlideUp"
    ) { state -> if (state == 1) content() }
}
