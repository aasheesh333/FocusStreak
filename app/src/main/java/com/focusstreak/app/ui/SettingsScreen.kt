package com.focusstreak.app.ui

import android.app.Activity
import android.app.TimePickerDialog
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.focusstreak.app.BuildConfig
import com.focusstreak.app.R
import com.focusstreak.app.ui.theme.FocusStreakTheme
import com.focusstreak.app.viewmodel.SettingsViewModel

// --- Colors from Design ---
private val ScreenBackground = Color(0xFFF8F9FA)
private val SectionHeaderColor = Color(0xFF9E9E9E)
private val CardBackground = Color.White

private val IconBgPurple = Color(0xFFECE6F0)
private val IconTintPurple = Color(0xFF6750A4)

private val IconBgTeal = Color(0xFFE0F2F1)
private val IconTintTeal = Color(0xFF009688)

private val IconBgOrange = Color(0xFFFFE0B2)
private val IconTintOrange = Color(0xFFFF9800)

private val IconBgBlue = Color(0xFFE3F2FD)
private val IconTintBlue = Color(0xFF2196F3)

private val ToggleActiveTrack = Color(0xFF6750A4)
private val ToggleInactiveTrack = Color(0xFFE0E0E0)

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun SettingsScreen(navController: NavController, settingsViewModel: SettingsViewModel = viewModel()) {
    var showResetDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            SettingsHeader(navController)

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                // FOCUS SECTION
                item { SettingsSectionHeader(stringResource(id = R.string.focus).uppercase()) }
                item {
                    SettingsCard {
                        FocusSectionContent(settingsViewModel)
                    }
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }

                // NOTIFICATIONS SECTION
                item { SettingsSectionHeader(stringResource(id = R.string.notifications).uppercase()) }
                item {
                    SettingsCard {
                        NotificationsSectionContent(settingsViewModel)
                    }
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }

                // APPEARANCE SECTION
                item { SettingsSectionHeader(stringResource(id = R.string.appearance).uppercase()) }
                item {
                    SettingsCard {
                        AppearanceSectionContent(settingsViewModel)
                    }
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }

                // ABOUT SECTION
                item { SettingsSectionHeader(stringResource(id = R.string.about).uppercase()) }
                item {
                    SettingsCard {
                        AboutSectionContent()
                    }
                }

                item { Spacer(modifier = Modifier.height(32.dp)) }

                // RESET BUTTON
                item {
                    ResetButton { showResetDialog = true }
                }
            }
        }

        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                title = { Text(text = stringResource(id = R.string.reset_dialog_title)) },
                text = { Text(text = stringResource(id = R.string.reset_dialog_message)) },
                confirmButton = {
                    Button(
                        onClick = {
                            settingsViewModel.resetAllProgress()
                            showResetDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text(stringResource(id = R.string.reset))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetDialog = false }) {
                        Text(stringResource(id = R.string.cancel))
                    }
                }
            )
        }
    }
}

@Composable
private fun SettingsHeader(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
        Text(
            text = stringResource(id = R.string.settings),
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        color = SectionHeaderColor,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            content()
        }
    }
}

@Composable
fun FocusSectionContent(viewModel: SettingsViewModel) {
    val userPreferences by viewModel.userPreferencesFlow.collectAsState(initial = null)
    val selectedDuration = userPreferences?.focusDuration ?: 25
    var showCustomDurationDialog by remember { mutableStateOf(false) }

    // Focus Duration Row
    Row(verticalAlignment = Alignment.CenterVertically) {
        SettingsIcon(icon = Icons.Filled.Timer, bgColor = IconBgPurple, tint = IconTintPurple)
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = stringResource(id = R.string.focus_duration),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Duration Segmented Control
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF5F5F5))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        DurationSegment(25, selectedDuration == 25) { viewModel.updateFocusDuration(25) }
        DurationSegment(30, selectedDuration == 30) { viewModel.updateFocusDuration(30) }
        DurationSegment(45, selectedDuration == 45) { viewModel.updateFocusDuration(45) }
        DurationSegmentCustom(selectedDuration !in listOf(25, 30, 45)) { showCustomDurationDialog = true }
    }

    Spacer(modifier = Modifier.height(24.dp))
    Divider(color = Color(0xFFF0F0F0), thickness = 1.dp)
    Spacer(modifier = Modifier.height(16.dp))

    // Auto-start Break Row
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SettingsIcon(icon = Icons.Filled.Autorenew, bgColor = IconBgTeal, tint = IconTintTeal)
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = stringResource(id = R.string.auto_start_break),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        }
        Switch(
            checked = userPreferences?.autoStartBreak ?: false,
            onCheckedChange = { viewModel.updateAutoStartBreak(it) },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = ToggleActiveTrack,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = ToggleInactiveTrack,
                uncheckedBorderColor = Color.Transparent
            )
        )
    }

    if (showCustomDurationDialog) {
        CustomDurationDialog(
            onDismiss = { showCustomDurationDialog = false },
            onSetDuration = {
                it.toIntOrNull()?.let { duration ->
                    viewModel.updateFocusDuration(duration)
                    showCustomDurationDialog = false
                }
            }
        )
    }
}

@Composable
fun RowScope.DurationSegment(duration: Int, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) Color.White else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "${duration}m",
            color = if (isSelected) Color.Black else Color.Gray,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun RowScope.DurationSegmentCustom(isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) Color.White else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Edit,
            contentDescription = "Custom",
            tint = if (isSelected) Color.Black else Color.Gray,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
fun NotificationsSectionContent(viewModel: SettingsViewModel) {
    val context = LocalContext.current
    val userPreferences by viewModel.userPreferencesFlow.collectAsState(initial = null)
    var reminderTime by remember { mutableStateOf("09:00 AM") } // Default format

    // Formatting helper
    fun formatTime(h: Int, m: Int): String {
        val amPm = if (h >= 12) "PM" else "AM"
        val hour12 = if (h > 12) h - 12 else if (h == 0) 12 else h
        return String.format("%02d:%02d %s", hour12, m, amPm)
    }

    LaunchedEffect(userPreferences) {
        userPreferences?.let {
            reminderTime = formatTime(it.reminderHour, it.reminderMinute)
        }
    }

    val hasNotificationPermission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED
    } else {
        true
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                viewModel.updateDailyReminderEnabled(true)
            }
        }
    )

    // Daily Reminder Row
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SettingsIcon(icon = Icons.Filled.Notifications, bgColor = IconBgOrange, tint = IconTintOrange)
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = stringResource(id = R.string.daily_reminder),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        }
        Switch(
            checked = userPreferences?.dailyReminderEnabled == true && hasNotificationPermission,
            onCheckedChange = {
                 if (hasNotificationPermission) {
                    viewModel.updateDailyReminderEnabled(it)
                    if (it) {
                        // Reschedule current time
                        viewModel.scheduleDailyReminder(userPreferences?.reminderHour ?: 9, userPreferences?.reminderMinute ?: 0)
                    } else {
                        viewModel.cancelDailyReminder()
                    }
                } else {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        launcher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = ToggleActiveTrack,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = ToggleInactiveTrack,
                uncheckedBorderColor = Color.Transparent
            )
        )
    }

    if (userPreferences?.dailyReminderEnabled == true && hasNotificationPermission) {
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 56.dp), // Indent to align with text
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.remind_me_at),
                color = Color.Gray,
                fontSize = 14.sp
            )

            TextButton(
                onClick = {
                    val currentHour = userPreferences?.reminderHour ?: 9
                    val currentMinute = userPreferences?.reminderMinute ?: 0
                    TimePickerDialog(
                        context,
                        { _, h, m ->
                            viewModel.scheduleDailyReminder(h, m)
                        },
                        currentHour,
                        currentMinute,
                        false // 12h format
                    ).show()
                },
                colors = ButtonDefaults.textButtonColors(contentColor = ToggleActiveTrack),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.background(IconBgPurple.copy(alpha=0.5f), RoundedCornerShape(8.dp))
            ) {
                Text(
                    text = reminderTime,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))
    Divider(color = Color(0xFFF0F0F0), thickness = 1.dp)
    Spacer(modifier = Modifier.height(16.dp))

    // Sound Effects Row
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SettingsIcon(icon = Icons.Filled.VolumeUp, bgColor = IconBgBlue, tint = IconTintBlue)
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = stringResource(id = R.string.sound_effects),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        }
        Switch(
            checked = userPreferences?.soundEffectsEnabled ?: true,
            onCheckedChange = { viewModel.updateSoundEffectsEnabled(it) },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = ToggleActiveTrack,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = ToggleInactiveTrack,
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}

@Composable
fun AppearanceSectionContent(viewModel: SettingsViewModel) {
    val context = LocalContext.current
    val userPreferences by viewModel.userPreferencesFlow.collectAsState(initial = null)
    val selectedTheme = userPreferences?.theme ?: "System"
    var showAdDialog by remember { mutableStateOf(false) }
    var pendingTheme by remember { mutableStateOf<String?>(null) }

    Row(verticalAlignment = Alignment.CenterVertically) {
        SettingsIcon(icon = Icons.Filled.Palette, bgColor = IconBgPurple, tint = IconTintPurple)
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = stringResource(id = R.string.theme),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )
    }

    Spacer(modifier = Modifier.height(24.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Light
        ThemeOptionCard(
            title = stringResource(id = R.string.light),
            icon = Icons.Filled.WbSunny,
            isSelected = selectedTheme == "Light",
            modifier = Modifier.weight(1f)
        ) {
            if (selectedTheme != "Light") {
                pendingTheme = "Light"
                showAdDialog = true
            }
        }

        // Dark
        ThemeOptionCard(
            title = stringResource(id = R.string.dark),
            icon = Icons.Filled.DarkMode,
            isSelected = selectedTheme == "Dark",
            modifier = Modifier.weight(1f)
        ) {
             if (selectedTheme != "Dark") {
                pendingTheme = "Dark"
                showAdDialog = true
            }
        }

        // System
        ThemeOptionCard(
            title = stringResource(id = R.string.system),
            icon = Icons.Filled.Smartphone,
            isSelected = selectedTheme == "System",
            modifier = Modifier.weight(1f)
        ) {
             if (selectedTheme != "System") {
                pendingTheme = "System"
                showAdDialog = true
            }
        }
    }

    if (showAdDialog && pendingTheme != null) {
        AlertDialog(
            onDismissRequest = { showAdDialog = false },
            title = { Text(stringResource(id = R.string.change_theme)) },
            text = { Text(stringResource(id = R.string.watch_ad_to_apply_theme)) },
            confirmButton = {
                Button(
                    onClick = {
                        showAdDialog = false
                        val activity = context.findActivity()
                        if (activity != null && pendingTheme != null) {
                            viewModel.showThemeAd(activity, pendingTheme!!)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ToggleActiveTrack)
                ) {
                    Text(stringResource(id = R.string.watch_ad))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAdDialog = false }) {
                    Text(stringResource(id = R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun ThemeOptionCard(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) Color(0xFFF3E5F5) else Color(0xFFF8F9FA))
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) ToggleActiveTrack else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(contentAlignment = Alignment.TopEnd, modifier = Modifier.fillMaxSize()) {
             if (isSelected) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = ToggleActiveTrack,
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.TopEnd)
                )
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                 Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (isSelected) ToggleActiveTrack else Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isSelected) ToggleActiveTrack else Color.Gray
                )
            }
        }
    }
}

@Composable
fun AboutSectionContent() {
    val context = LocalContext.current

    AboutItemRow(title = stringResource(id = R.string.rate_us), icon = null) {
        // TODO: Rate Us
    }
    Divider(color = Color(0xFFF0F0F0), thickness = 1.dp)
    AboutItemRow(title = stringResource(id = R.string.privacy_policy), icon = Icons.Filled.ArrowForward) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://dhanuk.page.gd/FocusStreak/Privacy-Policy.html"))
        context.startActivity(intent)
    }
    Divider(color = Color(0xFFF0F0F0), thickness = 1.dp)

    // Version Row
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(id = R.string.version),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )
        Text(
            text = BuildConfig.VERSION_NAME,
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun AboutItemRow(title: String, icon: ImageVector?, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(16.dp)
            )
        } else {
             Icon(
                imageVector = Icons.Filled.Star, // Explicit star for Rate Us as per typical patterns, though design showed plain text, usually implies action.
                contentDescription = null,
                tint = Color.Gray,
                 modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun ResetButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Color(0xFFD32F2F) // Red
        ),
        shape = RoundedCornerShape(32.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFEBEE)),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.Delete,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(id = R.string.reset_all_progress),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}

@Composable
fun SettingsIcon(icon: ImageVector, bgColor: Color, tint: Color) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun CustomDurationDialog(onDismiss: () -> Unit, onSetDuration: (String) -> Unit) {
    var duration by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(id = R.string.set_custom_duration)) },
        text = {
            TextField(
                value = duration,
                onValueChange = { duration = it },
                label = { Text(stringResource(id = R.string.duration_in_minutes)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        },
        confirmButton = {
            Button(onClick = { onSetDuration(duration) }) {
                Text(stringResource(id = R.string.set))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.cancel))
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    FocusStreakTheme {
        SettingsScreen(rememberNavController())
    }
}
