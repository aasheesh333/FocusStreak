package com.focusstreak.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.focusstreak.app.R
import com.focusstreak.app.ui.components.ConfettiAnimation
import com.focusstreak.app.ui.theme.FocusStreakTheme

// --- Colors matching Home Screen Dark Theme ---
private val DialogBackground = Color(0xFF0F0A1E)
private val SurfaceColor = Color(0xFF1A1625)
private val TextWhite = Color.White
private val TextGrey = Color(0xFF888888)
private val AccentPurple = Color(0xFF7000FF)
private val AccentPurpleLight = Color(0xFFA040FF)
private val CardDarkBg = Color(0xFF1C182F)

// Re-define local color for now to avoid dependency issues if not in Theme.kt
// Renaming to avoid conflict if file-level property is an issue
private val DialogFireOrange = Color(0xFFFF5722)

@Composable
fun SessionCompleteDialog(
    onDismiss: () -> Unit,
    onStartAnotherSession: () -> Unit,
    onTakeBreak: () -> Unit,
    onGetBonusTime: () -> Unit
) {
    // Full screen dialog properties
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = DialogBackground
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                // Confetti Animation behind content
                ConfettiAnimation(modifier = Modifier.fillMaxSize())

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    // Close Button
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .background(Color.White.copy(alpha = 0.1f), CircleShape)
                            .size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(id = R.string.cancel),
                            tint = TextWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Trophy Icon Section
                        Box(contentAlignment = Alignment.Center) {
                            // Decorative elements (confetti/stars)
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFFFFD700), // Gold
                                modifier = Modifier
                                    .size(40.dp)
                                    .align(Alignment.TopEnd)
                                    .offset(x = 24.dp, y = (-20).dp)
                            )
                             Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                tint = DialogFireOrange,
                                modifier = Modifier
                                    .size(24.dp)
                                    .align(Alignment.BottomStart)
                                    .offset(x = (-24).dp, y = 10.dp)
                            )

                            // Main Trophy Box
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(RoundedCornerShape(32.dp))
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(AccentPurpleLight, AccentPurple)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.EmojiEvents,
                                    contentDescription = null,
                                    tint = TextWhite,
                                    modifier = Modifier.size(64.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Text
                        Text(
                            text = stringResource(id = R.string.session_complete) + " \uD83C\uDF89", // Party popper emoji
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite,
                            textAlign = TextAlign.Center,
                            lineHeight = 40.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(id = R.string.great_focus),
                            color = TextGrey,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(48.dp))

                        // Bonus Card
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(24.dp))
                                .background(CardDarkBg)
                                .clickable(onClick = onGetBonusTime)
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2D2644)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PlayArrow,
                                    contentDescription = null,
                                    tint = AccentPurpleLight,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(id = R.string.get_bonus_focus_time),
                                    color = TextWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            Text(
                                text = stringResource(id = R.string.watch_short_video),
                                color = TextGrey,
                                fontSize = 12.sp
                            )
                            }
                            Icon(
                                imageVector = Icons.Filled.ArrowForward,
                                contentDescription = null,
                                tint = TextGrey
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Main Action Button
                        Button(
                            onClick = onStartAnotherSession,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            contentPadding = PaddingValues(0.dp),
                            shape = RoundedCornerShape(32.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(AccentPurpleLight, AccentPurple)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Timer, contentDescription = null, tint = TextWhite)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = stringResource(id = R.string.start_another_session),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Secondary Action
                        TextButton(
                            onClick = onTakeBreak
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LocalCafe,
                                contentDescription = null,
                                tint = TextWhite
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(id = R.string.take_a_break),
                                color = TextWhite,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun SessionCompleteDialogPreview() {
    FocusStreakTheme {
        SessionCompleteDialog({}, {}, {}, {})
    }
}
