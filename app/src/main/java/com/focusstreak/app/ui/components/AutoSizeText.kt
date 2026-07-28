package com.focusstreak.app.ui.components

import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp

/**
 * Single-line text that auto-scales its font down to whatever size
 * is needed to fit the available width — useful for messages that the
 * design wants on ONE line regardless of device width or density.
 *
 * Wraps Compose's [BasicText] overload + [TextAutoSize.StepBased] so
 * the runtime steps through font sizes from [maxTextSize] down to
 * [minTextSize] in [step] sp increments until the laid-out text fits
 * in a single line.
 *
 * Always sets `maxLines = 1` — callers that need wrapping should use
 * the standard [androidx.compose.material3.Text] composable instead.
 */
@Composable
fun AutoSizeText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    align: TextAlign = TextAlign.Center,
    maxTextSize: Int = 14,
    minTextSize: Int = 9,
    step: Int = 1
) {
    BasicText(
        text = text,
        modifier = modifier,
        style = TextStyle(
            color = color,
            fontSize = maxTextSize.sp,
            textAlign = align
        ),
        maxLines = 1,
        overflow = TextOverflow.Visible,
        autoSize = TextAutoSize.StepBased(
            minFontSize = minTextSize.sp,
            maxFontSize = maxTextSize.sp,
            stepSize = step.sp
        )
    )
}
