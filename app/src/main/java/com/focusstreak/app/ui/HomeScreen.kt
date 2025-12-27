package com.focusstreak.app.ui

import android.app.Activity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.focusstreak.app.R
import com.focusstreak.app.navigation.Screen
import com.focusstreak.app.ui.theme.AccentFire
import com.focusstreak.app.ui.theme.FocusStreakTheme
import com.focusstreak.app.viewmodel.HomeViewModel
import com.focusstreak.app.BuildConfig
import com.focusstreak.app.viewmodel.TimerState
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

@Composable
fun HomeScreen(navController: NavController, homeViewModel: HomeViewModel = viewModel()) {
    val timerState by homeViewModel.timerState.collectAsState()
    val timeInMillis by homeViewModel.timeInMillis.collectAsState()
    val userPreferences by homeViewModel.userPreferences.collectAsState()
    val totalTime = userPreferences.focusDuration * 60 * 1000L
    val context = LocalContext.current as Activity

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Header(navController, userPreferences.currentStreak)
            Timer(timeInMillis, totalTime)
            Footer(timerState, homeViewModel)
        }
    }

    if (timerState is TimerState.Completed) {
        LaunchedEffect(timerState) {
            homeViewModel.showInterstitialAd(context)
        }
        SessionCompleteDialog(
            onDismiss = { homeViewModel.endTimer() },
            onStartAnotherSession = { homeViewModel.startTimer() },
            onTakeBreak = { homeViewModel.endTimer() },
            onGetBonusTime = { homeViewModel.showRewardedAd(context) }
        )
    }
}

@Composable
fun Header(navController: NavController, currentStreak: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(top = 32.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(40.dp))
            Text(
                text = stringResource(id = R.string.focus_streak),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                fontSize = 12.sp
            )
            IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(id = R.string.settings),
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { navController.navigate(Screen.Progress.route) }
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_fire),
                contentDescription = stringResource(id = R.string.day_streak),
                tint = AccentFire,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(id = R.string.days, currentStreak),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(id = R.string.keep_the_momentum),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            textAlign = TextAlign.Center,
            modifier = Modifier.width(240.dp)
        )
    }
}

@Composable
fun Timer(timeInMillis: Long, totalTime: Long) {
    val minutes = (timeInMillis / 1000 / 60).toString().padStart(2, '0')
    val seconds = (timeInMillis / 1000 % 60).toString().padStart(2, '0')
    val progress = if (totalTime > 0) (totalTime - timeInMillis) / totalTime.toFloat() else 0f

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(300.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawArc(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 8f)
            )
            drawArc(
                color = MaterialTheme.colorScheme.primary,
                startAngle = -90f,
                sweepAngle = 360 * progress,
                useCenter = false,
                style = Stroke(width = 12f, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(id = R.string.time_format, minutes, seconds),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 80.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(id = R.string.pomodoro),
                color = MaterialTheme.colorScheme.primary,
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
        modifier = Modifier.padding(bottom = 32.dp)
    ) {
        when (timerState) {
            is TimerState.Idle -> {
                PrimaryButton(stringResource(id = R.string.start_focus), Icons.Default.PlayArrow) { viewModel.startTimer() }
            }
            is TimerState.Running -> {
                PrimaryButton(stringResource(id = R.string.pause), Icons.Default.Pause) { viewModel.pauseTimer() }
            }
            is TimerState.Paused -> {
                Row {
                    PrimaryButton(stringResource(id = R.string.resume), Icons.Default.PlayArrow) { viewModel.resumeTimer() }
                    Spacer(modifier = Modifier.width(16.dp))
                    PrimaryButton(stringResource(id = R.string.end), Icons.Default.Stop, color = Color.Gray) { viewModel.endTimer() }
                }
            }
            is TimerState.Completed -> {
                PrimaryButton(stringResource(id = R.string.start_another_session), Icons.Default.PlayArrow) { viewModel.startTimer() }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { context ->
                AdView(context).apply {
                    setAdSize(AdSize.BANNER)
                    adUnitId = BuildConfig.ADMOB_BANNER_ID
                    loadAd(AdRequest.Builder().build())
                }
            }
        )
    }
}

@Composable
fun PrimaryButton(text: String, icon: ImageVector, color: Color = MaterialTheme.colorScheme.primary, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .defaultMinSize(minWidth = 150.dp)
            .height(72.dp)
            .clip(CircleShape),
        colors = ButtonDefaults.buttonColors(containerColor = color)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    FocusStreakTheme {
        HomeScreen(rememberNavController())
    }
}
