package com.focusstreak.app.ui

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.focusstreak.app.R
import com.focusstreak.app.ui.theme.FocusStreakTheme
import com.focusstreak.app.util.findActivity
import com.focusstreak.app.viewmodel.HeatmapCell
import com.focusstreak.app.viewmodel.ProgressViewModel
import com.focusstreak.app.viewmodel.SessionStats
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

// --- Colors matching Home Screen Dark Theme ---
private val ProgressBackground = Color(0xFF0F0A1E)
private val CardBackground = Color(0xFF1C162E)
private val TextWhite = Color.White
private val TextGrey = Color(0xFF888888)
private val AccentPurple = Color(0xFF7000FF)
private val AccentPurpleLight = Color(0xFFA040FF)
private val BadgeGreen = Color(0xFF00C853)
private val FireOrange = Color(0xFFFF5722)
private val IconBgPurple = Color(0xFF2D2644)
private val IconBgTeal = Color(0xFF1E2D2F)

@Composable
fun ProgressScreen(navController: NavController, progressViewModel: ProgressViewModel = viewModel()) {
    val userPreferences by progressViewModel.userPreferences.collectAsState()
    val sessionStats by progressViewModel.sessionStats.collectAsState()
    val calendarDays by progressViewModel.calendarDays.collectAsState()
    val weekDays by progressViewModel.weekDays.collectAsState()
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val scope = rememberCoroutineScope()

    // Note: status-bar styling is owned by FocusStreakTheme.

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ProgressBackground)
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item { ProgressHeader(navController) }

            item {
                StreakSection(
                    currentStreak = sessionStats.currentStreak,
                    bestStreak = sessionStats.bestStreak,
                    onShareClick = {
                        scope.launch {
                            shareStreak(
                                context,
                                sessionStats.currentStreak,
                                sessionStats.totalSessions
                            )
                        }
                    }
                )
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }

            item { ThisWeekSection(weekDays) }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            item { StatsGrid(sessionStats) }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            item { MonthlyHeatmapSection(calendarDays) }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            item { MilestonesSection() }
        }

        // Floating Action Button
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
                .background(ProgressBackground.copy(alpha = 0.9f))
        ) {
             Button(
                onClick = { navController.popBackStack() },
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
                        Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = TextWhite)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(id = R.string.start_focus),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressHeader(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 16.dp)
    ) {
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = stringResource(id = R.string.cd_back),
                tint = TextWhite
            )
        }
        Text(
            text = stringResource(id = R.string.my_progress).uppercase(),
            color = TextWhite,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
fun StreakSection(currentStreak: Int, bestStreak: Int, onShareClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Top Badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(BadgeGreen.copy(alpha = 0.1f))
                .border(1.dp, BadgeGreen, RoundedCornerShape(50))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = stringResource(id = R.string.top_users),
                color = BadgeGreen,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Streak Count with Animation
        Box(contentAlignment = Alignment.Center) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val fireIcon = ImageVector.vectorResource(id = R.drawable.ic_fire)
                Icon(
                    imageVector = fireIcon,
                    contentDescription = null,
                    tint = FireOrange,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(id = R.string.days, currentStreak),
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
            }

            // Celebratory Animations
            CelebrationIcons()
        }

        Text(
            text = stringResource(id = R.string.day_streak),
            color = TextGrey,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(id = R.string.best_streak_format, bestStreak),
            color = TextGrey,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onShareClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White.copy(alpha = 0.1f),
                contentColor = TextWhite
            ),
            shape = RoundedCornerShape(24.dp),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Share,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = AccentPurpleLight
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(id = R.string.share_streak),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun CelebrationIcons() {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
        delay(2000)
        visible = false
    }

    Box(modifier = Modifier.size(100.dp)) { // Canvas for particles
        // Particle 1: Fire
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(initialOffsetY = { 50 }, animationSpec = tween(1000)) + fadeIn(animationSpec = tween(500)),
            exit = fadeOut(animationSpec = tween(1000)),
            modifier = Modifier.align(Alignment.CenterStart).offset(y = (-40).dp)
        ) {
            Icon(ImageVector.vectorResource(id = R.drawable.ic_fire), contentDescription = null, tint = FireOrange, modifier = Modifier.size(24.dp))
        }

        // Particle 2: Sparkle
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(initialOffsetY = { 60 }, animationSpec = tween(1200)) + fadeIn(animationSpec = tween(600)),
            exit = fadeOut(animationSpec = tween(1000)),
            modifier = Modifier.align(Alignment.TopEnd).offset(x = (-10).dp, y = (-20).dp)
        ) {
            Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = Color.Yellow, modifier = Modifier.size(20.dp))
        }

        // Particle 3: Party
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(initialOffsetY = { 40 }, animationSpec = tween(1500)) + fadeIn(animationSpec = tween(700)),
            exit = fadeOut(animationSpec = tween(1000)),
            modifier = Modifier.align(Alignment.TopCenter).offset(y = (-60).dp)
        ) {
             Text("🎉", fontSize = 24.sp)
        }
    }
}

@Composable
fun ThisWeekSection(weekDays: List<Triple<String, Boolean, Boolean>>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(id = R.string.this_week),
            color = TextWhite,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            weekDays.forEach { (dayName, isCompleted, isToday) ->
                DayCircle(dayName, isCompleted, isToday)
            }
        }
    }
}

@Composable
fun DayCircle(day: String, isCompleted: Boolean, isToday: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = day.take(2),
            color = TextGrey,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    if (isCompleted) AccentPurple else if (isToday) Color.White.copy(alpha = 0.1f) else Color.Transparent
                )
                .border(
                    width = if (isCompleted) 0.dp else 1.dp,
                    color = if (isCompleted) Color.Transparent else Color.White.copy(alpha = 0.2f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = TextWhite,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun StatsGrid(stats: SessionStats) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard(
                title = stringResource(id = R.string.hours),
                value = String.format(java.util.Locale.US, "%.1f", stats.totalMinutes / 60.0),
                subtitle = stringResource(id = R.string.total_focused_time),
                icon = Icons.Filled.AccessTime,
                iconBg = IconBgPurple,
                iconTint = AccentPurpleLight,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = stringResource(id = R.string.sessions),
                value = stats.totalSessions.toString(),
                subtitle = stringResource(id = R.string.completed_sessions),
                icon = Icons.Filled.CheckCircle,
                iconBg = IconBgTeal,
                iconTint = Color(0xFF26A69A),
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard(
                title = stringResource(id = R.string.weekly_minutes),
                value = stats.weeklyMinutes.toString(),
                subtitle = stringResource(id = R.string.this_week),
                icon = Icons.Filled.Today,
                iconBg = IconBgOrange,
                iconTint = Color(0xFFFF9800),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = stringResource(id = R.string.top_category),
                value = stats.topCategory,
                subtitle = stringResource(id = R.string.most_used),
                icon = Icons.Filled.Category,
                iconBg = IconBgTeal,
                iconTint = Color(0xFF26A69A),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

fun MonthlyHeatmapSection(days: List<HeatmapCell>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(id = R.string.last_six_weeks),
            color = TextWhite,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(days.size) { index ->
                val cell = days[index]
                val color = when {
                    cell.isToday -> AccentPurple
                    cell.isCompleted -> AccentPurpleLight.copy(alpha = 0.7f)
                    else -> CardBackground.copy(alpha = 0.4f)
                }
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(color)
                )
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    modifier: Modifier
) {
    Card(
        modifier = modifier.height(140.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(16.dp)
                )
            }
            Column {
                Text(
                    text = value,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
                Text(
                    text = "$title $subtitle",
                    fontSize = 12.sp,
                    color = TextGrey,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun MilestonesSection() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(id = R.string.next_milestone),
            color = TextWhite,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { MilestoneCard(stringResource(id = R.string.seven_day_streak), true) }
            item { MilestoneCard(stringResource(id = R.string.fourteen_day_streak), false) }
            item { MilestoneCard(stringResource(id = R.string.thirty_day_streak), false) }
        }
    }
}

@Composable
fun MilestoneCard(title: String, isUnlocked: Boolean) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(100.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = if (isUnlocked) Icons.Filled.EmojiEvents else Icons.Filled.Lock,
                contentDescription = null,
                tint = if (isUnlocked) Color(0xFFFFD700) else Color.Gray,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = title,
                color = if (isUnlocked) TextWhite else TextGrey,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

suspend fun shareStreak(context: Context, streak: Int, sessions: Int) {
    // Bitmap creation, drawing, and file I/O are off the main thread to
    // avoid ANRs on slower devices (~4.4 MB ARGB_8888 allocation).
    val uri = withContext(Dispatchers.IO) {
        val width = 1080
        val height = 1080
        val bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)

        canvas.drawColor(android.graphics.Color.parseColor("#0F0A1E"))

        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        }

        paint.textSize = 60f
        canvas.drawText(context.getString(R.string.streak_share_branding), width / 2f, 150f, paint)

        paint.textSize = 400f
        canvas.drawText(streak.toString(), width / 2f, height / 2f + 50f, paint)

        paint.textSize = 60f
        paint.color = android.graphics.Color.parseColor("#888888")
        canvas.drawText(context.getString(R.string.streak_share_label), width / 2f, height / 2f + 200f, paint)

        paint.textSize = 50f
        paint.color = android.graphics.Color.WHITE
        canvas.drawText(
            context.getString(R.string.streak_share_sessions, sessions),
            width / 2f,
            height / 2f + 300f,
            paint
        )

        paint.textSize = 40f
        paint.color = android.graphics.Color.WHITE
        canvas.drawText(
            context.getString(R.string.streak_share_motto),
            width / 2f,
            height - 150f,
            paint
        )

        // Ensure the shared/ subfolder exists (matches FileProvider scope).
        val sharedDir = File(context.cacheDir, "shared")
        if (!sharedDir.exists()) sharedDir.mkdirs()
        val file = File(sharedDir, "streak_share.png")
        FileOutputStream(file).use { stream ->
            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, stream)
        }
        bitmap.recycle()

        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    // startActivity must be on the main thread.
    withContext(Dispatchers.Main) {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, context.getString(R.string.share_streak_text, streak))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_chooser_title)))
        } catch (e: android.content.ActivityNotFoundException) {
            android.util.Log.w("ProgressScreen", "No activity available to share streak", e)
        } catch (e: Exception) {
            android.util.Log.e("ProgressScreen", "Failed to share streak", e)
        }
    }
}
