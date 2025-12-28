package com.focusstreak.app.ui

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.focusstreak.app.R
import com.focusstreak.app.ui.theme.FocusStreakTheme
import com.focusstreak.app.viewmodel.ProgressViewModel

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
    val weekDays by progressViewModel.weekDays.collectAsState()
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ProgressBackground)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(bottom = 100.dp) // Space for FAB/Button
        ) {
            item { ProgressHeader(navController) }

            item {
                StreakSection(
                    currentStreak = userPreferences.currentStreak,
                    onShareClick = {
                        val sendIntent: Intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, context.getString(R.string.share_streak_text, userPreferences.currentStreak))
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, null)
                        context.startActivity(shareIntent)
                    }
                )
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }

            item { ThisWeekSection(weekDays) }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            item { StatsGrid(userPreferences.totalFocusMinutes, userPreferences.totalSessions) }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            item { MilestonesSection() }
        }

        // Start Focus Button (Floating at bottom)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
                .background(ProgressBackground.copy(alpha = 0.9f)) // Gradient fade?
        ) {
             Button(
                onClick = {
                    // Navigate back to home or specifically to timer start if possible
                    navController.popBackStack()
                },
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
                contentDescription = "Back",
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
fun StreakSection(currentStreak: Int, onShareClick: () -> Unit) {
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
                text = "TOP 5% OF USERS", // Could be string resource
                color = BadgeGreen,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Streak Count
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

        Text(
            text = stringResource(id = R.string.day_streak), // Using existing string
            color = TextGrey,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Share Button
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
            text = day.take(1), // M, T, W...
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
fun StatsGrid(totalFocusMinutes: Int, totalSessions: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hours Card
        StatCard(
            title = stringResource(id = R.string.hours),
            value = String.format("%.1f", totalFocusMinutes / 60.0),
            subtitle = "Total focused time", // Needs resource or hardcode
            icon = Icons.Filled.AccessTime,
            iconBg = IconBgPurple,
            iconTint = AccentPurpleLight,
            modifier = Modifier.weight(1f)
        )

        // Sessions Card
        StatCard(
            title = stringResource(id = R.string.sessions), // "Sessions"
            value = totalSessions.toString(),
            subtitle = "Completed sessions", // Needs resource or hardcode
            icon = Icons.Filled.CheckCircle,
            iconBg = IconBgTeal,
            iconTint = Color(0xFF26A69A),
            modifier = Modifier.weight(1f)
        )
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
                    text = "$title $subtitle", // e.g. "HOURS Total focused time" -> actually design has "42.5 HOURS" then newline "Total focused time"
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

        // Horizontal list of milestones
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                MilestoneCard(
                    title = stringResource(id = R.string.seven_day_streak),
                    isUnlocked = true
                )
            }
            item {
                MilestoneCard(
                    title = stringResource(id = R.string.fourteen_day_streak),
                    isUnlocked = false
                )
            }
             item {
                MilestoneCard(
                    title = stringResource(id = R.string.thirty_day_streak),
                    isUnlocked = false
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

@Preview(showBackground = true)
@Composable
fun ProgressScreenPreview() {
    FocusStreakTheme {
        ProgressScreen(rememberNavController())
    }
}
