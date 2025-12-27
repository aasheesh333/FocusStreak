package com.focusstreak.app.ui

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.focusstreak.app.R
import com.focusstreak.app.ui.theme.FocusStreakTheme
import com.focusstreak.app.viewmodel.ProgressViewModel

@Composable
fun ProgressScreen(navController: NavController, progressViewModel: ProgressViewModel = viewModel()) {
    val userPreferences by progressViewModel.userPreferences.collectAsState()
    val weekDays by progressViewModel.weekDays.collectAsState()
    val context = LocalContext.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Header(navController)
            Streak(userPreferences.currentStreak) {
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, context.getString(R.string.share_streak_text, userPreferences.currentStreak))
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, null)
                context.startActivity(shareIntent)
            }
            ThisWeek(weekDays)
            Stats(userPreferences.totalFocusMinutes, userPreferences.totalSessions)
            NextMilestone()
        }
    }
}

@Composable
fun Header(navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { navController.popBackStack() }) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
        }
        Text(text = stringResource(id = R.string.my_progress), color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.width(48.dp))
    }
}

@Composable
fun Streak(currentStreak: Int, onShareClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(32.dp)
    ) {
        Text(
            text = currentStreak.toString(),
            fontSize = 110.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = stringResource(id = R.string.day_streak),
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onShareClick) {
            Icon(imageVector = Icons.Default.Share, contentDescription = stringResource(id = R.string.share_streak))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = stringResource(id = R.string.share_streak))
        }
    }
}

@Composable
fun ThisWeek(weekDays: List<Triple<String, Boolean, Boolean>>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(text = stringResource(id = R.string.this_week), color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(16.dp))
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            weekDays.forEach { (dayName, isCompleted, isToday) ->
                Day(dayName, isCompleted, isToday)
            }
        }
    }
}

@Composable
fun Day(day: String, isCompleted: Boolean, isToday: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = day, color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(if (isCompleted) MaterialTheme.colorScheme.primary else Color.Transparent)
                .then(if (isToday) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape) else Modifier),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(imageVector = Icons.Default.Check, contentDescription = "Completed", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

@Composable
fun Stats(totalFocusMinutes: Int, totalSessions: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        StatCard(stringResource(id = R.string.hours), (totalFocusMinutes / 60.0).toString(), Icons.Default.Timer)
        StatCard(stringResource(id = R.string.sessions), totalSessions.toString(), Icons.Default.Psychology)
    }
}

@Composable
fun StatCard(title: String, value: String, icon: ImageVector) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(120.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = title, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                Icon(imageVector = icon, contentDescription = title, tint = MaterialTheme.colorScheme.primary)
            }
            Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun NextMilestone() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(text = stringResource(id = R.string.next_milestone), color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
        LazyRow {
            item { MilestoneCard(stringResource(id = R.string.seven_day_streak), true) }
            item { MilestoneCard(stringResource(id = R.string.fourteen_day_streak), false) }
            item { MilestoneCard(stringResource(id = R.string.thirty_day_streak), false) }
        }
    }
}

@Composable
fun MilestoneCard(text: String, isUnlocked: Boolean) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .height(80.dp)
            .padding(end = 16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isUnlocked) ImageVector.vectorResource(id = R.drawable.ic_fire) else Icons.Default.Lock,
                contentDescription = text,
                tint = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = text, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
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
