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
import androidx.compose.foundation.lazy.items as lazyListItems
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
import com.focusstreak.app.ui.components.SlideUp
import com.focusstreak.app.ui.theme.DarkBackground
import com.focusstreak.app.ui.theme.DarkCardBackground
import com.focusstreak.app.ui.theme.DarkIconBgPurple
import com.focusstreak.app.ui.theme.DarkIconBgTeal
import com.focusstreak.app.ui.theme.DarkIconBgOrange
import com.focusstreak.app.ui.theme.DarkIconBgBlue
import com.focusstreak.app.ui.theme.DarkIconTintTealAccent
import com.focusstreak.app.ui.theme.DarkIconTintOrangeAccent
import com.focusstreak.app.ui.theme.DarkBadgeGreen
import com.focusstreak.app.ui.theme.TextPrimaryLight
import com.focusstreak.app.ui.theme.TextSecondary
import com.focusstreak.app.ui.theme.BrandPurple
import com.focusstreak.app.ui.theme.BrandPurpleLight
import com.focusstreak.app.ui.theme.BrandOrange
import com.focusstreak.app.ui.theme.RadiusL
import com.focusstreak.app.ui.theme.RadiusXL
import com.focusstreak.app.ui.theme.CardTitleSize
import com.focusstreak.app.ui.theme.CardBodySize
import com.focusstreak.app.ui.theme.DisplayStreakSize
import com.focusstreak.app.ui.theme.SpaceS
import com.focusstreak.app.ui.theme.SpaceM
import com.focusstreak.app.ui.theme.SpaceL
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

// --- Dark palette aliases (sourced from theme/DesignTokens.kt) ---
private val ProgressBackground = DarkBackground
private val CardBackground = DarkCardBackground
private val TextWhite = TextPrimaryLight
private val TextGrey = TextSecondary
private val AccentPurple = BrandPurple
private val AccentPurpleLight = BrandPurpleLight
private val BadgeGreen = DarkBadgeGreen
private val FireOrange = BrandOrange
private val IconBgPurple = DarkIconBgPurple
private val IconBgTeal = DarkIconBgTeal
private val IconBgOrange = DarkIconBgOrange
private val IconBgBlue = DarkIconBgBlue
private val IconTintTealAccent = DarkIconTintTealAccent
private val IconTintOrangeAccent = DarkIconTintOrangeAccent

@Composable
fun ProgressScreen(navController: NavController, progressViewModel: ProgressViewModel = viewModel()) {
    val userPreferences by progressViewModel.userPreferences.collectAsState()
    val sessionStats by progressViewModel.sessionStats.collectAsState()
    val calendarDays by progressViewModel.calendarDays.collectAsState()
    val weekDays by progressViewModel.weekDays.collectAsState()
    val categoryBreakdown by progressViewModel.categoryBreakdown.collectAsState()
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

            if (sessionStats.totalSessions == 0) {
                item {
                    EmptyProgressState(onStartSession = { navController.popBackStack() })
                }
                // Still show the static section headers below so the
                // user has something to look at — sections will be
                // mostly empty but the structure teaches them what
                // the app is going to track.
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }

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

            item { SlideUp(delayMillis = 100) { ThisWeekSection(weekDays) } }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            item { SlideUp(delayMillis = 200) { StatsGrid(sessionStats) } }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            if (categoryBreakdown.isNotEmpty()) {
                item { SlideUp(delayMillis = 250) {
                    CategoryBreakdownSection(categories = categoryBreakdown)
                } }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }

            item { SlideUp(delayMillis = 300) { MonthlyHeatmapSection(calendarDays) } }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            item { SlideUp(delayMillis = 400) {
                MilestonesSection(currentStreak = sessionStats.currentStreak)
            } }
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
fun EmptyProgressState(onStartSession: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // A subtle ring with an emoji/icon inside — soft illustration
        // to fill the empty space without overwhelming the user.
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(AccentPurpleLight.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "\uD83C\uDFAF", // bullseye
                fontSize = 40.sp
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(id = R.string.empty_progress_title),
            color = TextWhite,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(id = R.string.empty_progress_body),
            color = TextGrey,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(280.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onStartSession,
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentPurple,
                contentColor = TextWhite
            )
        ) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(id = R.string.start_focus),
                fontWeight = FontWeight.Bold
            )
        }
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
                com.focusstreak.app.ui.components.AnimatedCount(
                    target = currentStreak,
                    content = { value ->
                        Text(
                            text = stringResource(id = R.string.days, value),
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    }
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
                iconTint = IconTintTealAccent,
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
                iconTint = IconTintOrangeAccent,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = stringResource(id = R.string.top_category),
                value = stats.topCategory,
                subtitle = stringResource(id = R.string.most_used),
                icon = Icons.Filled.Category,
                iconBg = IconBgTeal,
                iconTint = IconTintTealAccent,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun CategoryBreakdownSection(categories: List<com.focusstreak.app.viewmodel.CategoryStat>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(id = R.string.by_category),
            color = TextWhite,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        val totalMinutes = categories.sumOf { it.minutes }.coerceAtLeast(1)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(RadiusXL)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                categories.forEach { stat ->
                    CategoryRow(
                        name = stat.categoryName,
                        sessions = stat.sessions,
                        minutes = stat.minutes,
                        share = stat.minutes.toFloat() / totalMinutes
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryRow(name: String, sessions: Int, minutes: Int, share: Float) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = name,
                color = TextWhite,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
            Text(
                text = "$sessions sessions · ${minutes}m",
                color = TextGrey,
                fontSize = 12.sp
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { share.coerceIn(0f, 1f) },
            color = AccentPurpleLight,
            trackColor = Color.White.copy(alpha = 0.06f),
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape)
        )
    }
}

@Composable
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
fun MilestonesSection(currentStreak: Int) {
    // Thresholds (in days) and the matching string + label. A milestone
    // is "unlocked" when the user's current streak is greater-than-or-equal
    // the threshold. Locked cards show the next goal to aim for.
    data class Milestone(val threshold: Int, val labelRes: Int)
    val milestones = listOf(
        Milestone(7, R.string.seven_day_streak),
        Milestone(14, R.string.fourteen_day_streak),
        Milestone(30, R.string.thirty_day_streak),
        Milestone(100, R.string.one_hundred_day_streak)
    )
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
            lazyListItems(milestones) { milestone ->
                MilestoneCard(
                    title = stringResource(id = milestone.labelRes),
                    isUnlocked = currentStreak >= milestone.threshold
                )
            }
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
