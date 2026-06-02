package com.focusstreak.app.ui

import android.graphics.BlurMaskFilter
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.focusstreak.app.R
import com.focusstreak.app.ads.BannerAd
import com.focusstreak.app.navigation.Screen
import com.focusstreak.app.ui.theme.FocusStreakTheme
import com.focusstreak.app.util.findActivity
import com.focusstreak.app.viewmodel.HomeViewModel
import com.focusstreak.app.viewmodel.TimerState
import android.view.WindowManager
import android.content.Context
import android.content.Intent

// --- Colors from Home Design (Dark Theme) ---
private val HomeBackground = Color(0xFF0F0A1E)
private val TextWhite = Color.White
private val TextGrey = Color(0xFF888888)
private val AccentPurple = Color(0xFF7000FF)
private val AccentPurpleLight = Color(0xFFA040FF)
private val TimerGlowColor = Color(0xFF5000B8)
// Fallback if AccentFire isn't resolved from theme
private val FireOrange = Color(0xFFFF5722)

@Composable
fun HomeScreen(navController: NavController, homeViewModel: HomeViewModel = viewModel()) {
    val timerState by homeViewModel.timerState.collectAsState()
    val timeInMillis by homeViewModel.timeInMillis.collectAsState()
    val userPreferences by homeViewModel.userPreferences.collectAsState()

    val totalTime = userPreferences.focusDuration * 60 * 1000L
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }

    // Note: status-bar styling is owned by FocusStreakTheme.

    // Keep screen on when timer is running
    DisposableEffect(timerState, activity) {
        val window = activity?.window
        if (window != null && timerState is TimerState.Running) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HomeBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            HomeHeader(
                navController = navController,
                currentStreak = userPreferences.currentStreak,
                onShareClick = {
                    shareStreakText(
                        context = context,
                        streak = userPreferences.currentStreak
                    )
                }
            )

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Timer(timeInMillis, totalTime)
            }

            Footer(timerState, homeViewModel)

            // Banner ad pinned below the Footer. We intentionally
            // only show it when the timer is Idle so the user isn't
            // distracted by ads while focusing.
            if (timerState is TimerState.Idle) {
                BannerAd(modifier = Modifier.padding(top = 8.dp))
            }
        }
    }

    // Ad Logic: Show ad when in AdShowing state
    if (timerState is TimerState.AdShowing && activity != null) {
        LaunchedEffect(timerState, activity) {
            homeViewModel.showInterstitialAd(activity)
        }
    }

    // Session Complete Dialog only shows in Completed state (which happens AFTER ad)
    if (timerState is TimerState.Completed) {
        SessionCompleteDialog(
            onDismiss = { homeViewModel.endTimer() },
            onStartAnotherSession = { homeViewModel.startTimer() },
            onTakeBreak = { homeViewModel.endTimer() },
            onGetBonusTime = { activity?.let { homeViewModel.showRewardedAd(it) } }
        )
    }
}

@Composable
fun HomeHeader(navController: NavController, currentStreak: Int, onShareClick: () -> Unit) {
    val focusStreakText = stringResource(id = R.string.focus_streak)
    val settingsDesc = stringResource(id = R.string.settings)
    val dayStreakDesc = stringResource(id = R.string.day_streak)
    val daysText = stringResource(id = R.string.days, currentStreak)
    val momentumText = stringResource(id = R.string.keep_the_momentum)
    val viewDashboardText = stringResource(id = R.string.view_streak_dashboard)
    val shareDesc = stringResource(id = R.string.share_my_streak)

    val fireIcon = ImageVector.vectorResource(id = R.drawable.ic_fire)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
    ) {
        // Top Bar
        Box(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = focusStreakText.uppercase(),
                color = TextGrey,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.Center)
            )
            IconButton(
                onClick = { navController.navigate(Screen.Settings.route) },
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = settingsDesc,
                    tint = TextGrey
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Streak Section — now visibly a card the user can tap to
        // open the streak dashboard, with a quick share button.
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White.copy(alpha = 0.06f))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                .clickable { navController.navigate(Screen.Progress.route) }
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            Icon(
                imageVector = fireIcon,
                contentDescription = dayStreakDesc,
                tint = FireOrange,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = daysText,
                color = TextWhite,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = viewDashboardText,
                tint = TextGrey
            )
            Spacer(modifier = Modifier.width(4.dp))
            IconButton(onClick = onShareClick) {
                Icon(
                    imageVector = Icons.Filled.Share,
                    contentDescription = shareDesc,
                    tint = AccentPurpleLight
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = momentumText,
            color = TextGrey,
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
            modifier = Modifier.width(260.dp)
        )
    }
}

@Composable
fun Timer(timeInMillis: Long, totalTime: Long) {
    val minutes = (timeInMillis / 1000 / 60).toString().padStart(2, '0')
    val seconds = (timeInMillis / 1000 % 60).toString().padStart(2, '0')
    val progress = if (totalTime > 0) (totalTime - timeInMillis) / totalTime.toFloat() else 0f

    val ringSize = 280.dp
    val strokeWidth = 12.dp
    val glowColor = TimerGlowColor
    val progressBrush = Brush.verticalGradient(
        colors = listOf(AccentPurpleLight, AccentPurple)
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(ringSize + 40.dp) // Extra space for glow
    ) {
        Canvas(modifier = Modifier.size(ringSize)) {
            // Background Track
            drawArc(
                color = Color.White.copy(alpha = 0.05f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx())
            )

            // Progress Arc with Glow
            // We verify if progress is > 0 to draw
            if (progress < 1f) {
                // Assuming "time remaining": 25:00 -> 00:00.
                // Progress 0 = Full Time. Progress 1 = Time Up.
                // We want to draw the REMAINING time.
                val remainingSweep = 360f * (timeInMillis.toFloat() / totalTime.toFloat())

                // Glow effect using native canvas
                drawIntoCanvas { canvas ->
                    val paint = Paint()
                    val frameworkPaint = paint.asFrameworkPaint()
                    frameworkPaint.color = glowColor.toArgb()
                    frameworkPaint.style = android.graphics.Paint.Style.STROKE
                    frameworkPaint.strokeWidth = strokeWidth.toPx()
                    frameworkPaint.strokeCap = android.graphics.Paint.Cap.ROUND
                    frameworkPaint.maskFilter = BlurMaskFilter(40f, BlurMaskFilter.Blur.NORMAL)

                    canvas.drawArc(
                        left = 0f + strokeWidth.toPx()/2,
                        top = 0f + strokeWidth.toPx()/2,
                        right = size.width - strokeWidth.toPx()/2,
                        bottom = size.height - strokeWidth.toPx()/2,
                        startAngle = -90f,
                        sweepAngle = remainingSweep,
                        useCenter = false,
                        paint = paint
                    )
                }

                // Main Gradient Arc
                drawArc(
                    brush = progressBrush,
                    startAngle = -90f,
                    sweepAngle = remainingSweep,
                    useCenter = false,
                    style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$minutes:$seconds",
                color = TextWhite,
                fontSize = 72.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-2).sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(id = R.string.pomodoro).uppercase(),
                color = AccentPurple,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun Footer(timerState: TimerState, viewModel: HomeViewModel) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(bottom = 16.dp)
    ) {
        val buttonModifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(AccentPurpleLight, AccentPurple)
                )
            )

        when (timerState) {
            is TimerState.Idle -> {
                GradientButton(
                    text = stringResource(id = R.string.start_focus),
                    icon = Icons.Filled.PlayArrow,
                    modifier = buttonModifier,
                    onClick = { viewModel.startTimer() }
                )
            }
            is TimerState.Running -> {
                GradientButton(
                    text = stringResource(id = R.string.pause),
                    icon = Icons.Filled.Pause,
                    modifier = buttonModifier,
                    onClick = { viewModel.pauseTimer() }
                )
            }
            is TimerState.Paused -> {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    GradientButton(
                        text = stringResource(id = R.string.resume),
                        icon = Icons.Filled.PlayArrow,
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp)
                            .clip(RoundedCornerShape(32.dp))
                            .background(Brush.horizontalGradient(colors = listOf(AccentPurpleLight, AccentPurple))),
                        onClick = { viewModel.resumeTimer() }
                    )

                    // End Button (Secondary style)
                    Button(
                        onClick = { viewModel.endTimer() },
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(32.dp)
                    ) {
                        Icon(Icons.Filled.Stop, contentDescription = null, tint = TextWhite)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = stringResource(id = R.string.end), color = TextWhite, fontWeight = FontWeight.Bold)
                    }
                }
            }
            is TimerState.Completed -> {
                GradientButton(
                    text = stringResource(id = R.string.start_another_session),
                    icon = Icons.Filled.PlayArrow,
                    modifier = buttonModifier,
                    onClick = { viewModel.startTimer() }
                )
            }
            is TimerState.AdShowing -> {
                Row(
                    modifier = buttonModifier,
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = TextWhite,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(id = R.string.ad_loading),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                }
            }
        }
    }
}

@Composable
fun GradientButton(
    text: String,
    icon: ImageVector,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier.clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextWhite,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    FocusStreakTheme {
        HomeScreen(rememberNavController())
    }
}

/**
 * Quick text-only share of the current streak count. Lightweight
 * counterpart of the PNG-card share on the Progress screen. Falls
 * back to a no-op (with a logcat warning) if no chooser activity
 * is available on the device.
 */
fun shareStreakText(context: Context, streak: Int) {
    try {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(
                Intent.EXTRA_TEXT,
                context.getString(R.string.share_streak_text, streak)
            )
        }
        context.startActivity(
            Intent.createChooser(sendIntent, context.getString(R.string.share_chooser_title))
        )
    } catch (e: android.content.ActivityNotFoundException) {
        android.util.Log.w("HomeScreen", "No activity available to share streak", e)
    } catch (e: Exception) {
        android.util.Log.e("HomeScreen", "Failed to share streak", e)
    }
}
