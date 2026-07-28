package com.focusstreak.app.ui

import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.focusstreak.app.data.FocusCategories
import com.focusstreak.app.ui.IconBgOrange
import com.focusstreak.app.ui.IconTintOrange
import com.focusstreak.app.ui.theme.FocusStreakTheme
import com.focusstreak.app.ui.theme.LightBackground
import com.focusstreak.app.ui.theme.LightCardBackground
import com.focusstreak.app.ui.theme.LightDivider
import com.focusstreak.app.ui.theme.LightIconBgPurple
import com.focusstreak.app.ui.theme.LightIconTintPurple
import com.focusstreak.app.ui.theme.LightIconBgTeal
import com.focusstreak.app.ui.theme.LightIconTintTeal
import com.focusstreak.app.ui.theme.LightIconBgBlue
import com.focusstreak.app.ui.theme.LightIconTintBlue
import com.focusstreak.app.ui.theme.LightToggleActiveTrack
import com.focusstreak.app.ui.theme.LightToggleInactiveTrack
import com.focusstreak.app.ui.theme.LightTrackSurface
import com.focusstreak.app.ui.theme.LightSurfaceGray
import com.focusstreak.app.ui.theme.TextSecondary
import com.focusstreak.app.ui.theme.BrandFreezeBlueAccent
import com.focusstreak.app.ui.theme.RadiusL
import com.focusstreak.app.ui.theme.CardTitleSize
import com.focusstreak.app.ui.theme.CardBodySize
import com.focusstreak.app.ui.theme.HelperTextSize
import com.focusstreak.app.ui.theme.SpaceL
import com.focusstreak.app.ui.components.SlideUp
import com.focusstreak.app.util.findActivity
import com.focusstreak.app.viewmodel.SettingsUiEvent
import com.focusstreak.app.viewmodel.SettingsViewModel
import kotlinx.coroutines.flow.collectLatest

// --- Light palette aliases (sourced from theme/DesignTokens.kt) ---
private val ScreenBackground = LightBackground
private val SectionHeaderColor = TextSecondary
private val CardBackground = LightCardBackground

private val IconBgPurple = LightIconBgPurple
private val IconTintPurple = LightIconTintPurple

private val IconBgTeal = LightIconBgTeal
private val IconTintTeal = LightIconTintTeal

private val IconBgBlue = LightIconBgBlue
private val IconTintBlue = LightIconTintBlue

private val ToggleActiveTrack = LightToggleActiveTrack
private val ToggleInactiveTrack = LightToggleInactiveTrack

@Composable
fun SettingsScreen(navController: NavController, settingsViewModel: SettingsViewModel = viewModel()) {
    val context = LocalContext.current
    val loadingAdText = stringResource(id = R.string.loading_ad)

    // Surface one-off UI events (e.g. "ad not ready") from the ViewModel.
    LaunchedEffect(settingsViewModel) {
        settingsViewModel.events.collectLatest { event ->
            when (event) {
                SettingsUiEvent.AdNotReady ->
                    Toast.makeText(context, loadingAdText, Toast.LENGTH_SHORT).show()
            }
        }
    }

    var showResetDialog by rememberSaveable { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars),
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
                        SlideUp(delayMillis = 80) {
                            FocusSectionContent(settingsViewModel)
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }

                // NOTIFICATIONS SECTION
                item { SettingsSectionHeader(stringResource(id = R.string.notifications).uppercase()) }
                item {
                    SettingsCard {
                        SlideUp(delayMillis = 160) {
                            NotificationsSectionContent(settingsViewModel)
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }

                // APPEARANCE SECTION
                item { SettingsSectionHeader(stringResource(id = R.string.appearance).uppercase()) }
                item {
                    SettingsCard {
                        SlideUp(delayMillis = 240) {
                            AppearanceSectionContent(settingsViewModel)
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }

                // DIAGNOSTICS SECTION — only shown in debug builds. The
                // "Use Test Ads" toggle is for verifying the ad flow on a
                // device; it has no business in a shipping app.
                if (BuildConfig.DEBUG) {
                    item { SettingsSectionHeader(stringResource(id = R.string.diagnostics).uppercase()) }
                    item {
                        SettingsCard {
                            SlideUp(delayMillis = 320) {
                                DiagnosticsSectionContent(settingsViewModel)
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }

                // ABOUT SECTION
                item { SettingsSectionHeader(stringResource(id = R.string.about).uppercase()) }
                item {
                    SettingsCard {
                        SlideUp(delayMillis = 400) {
                            AboutSectionContent()
                        }
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
                contentDescription = stringResource(id = R.string.cd_back),
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
    var showCustomDurationDialog by rememberSaveable { mutableStateOf(false) }

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
            .background(LightTrackSurface)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        DurationSegment(25, selectedDuration == 25) { viewModel.updateFocusDuration(25) }
        DurationSegment(30, selectedDuration == 30) { viewModel.updateFocusDuration(30) }
        DurationSegment(45, selectedDuration == 45) { viewModel.updateFocusDuration(45) }
        DurationSegmentCustom(selectedDuration !in listOf(25, 30, 45)) { showCustomDurationDialog = true }
    }

    Spacer(modifier = Modifier.height(24.dp))
    // Focus Category Row
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingsIcon(icon = Icons.Filled.Category, bgColor = IconBgPurple, tint = IconTintPurple)
        Spacer(modifier = Modifier.width(12.dp))
        LazyRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(FocusCategories) { category ->
                val selected = category.id == (userPreferences?.focusCategory ?: FocusCategories.first().id)
                FilterChip(
                    selected = selected,
                    onClick = { viewModel.updateCategory(category.id) },
                    label = { Text(category.name) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = IconTintPurple,
                        selectedLabelColor = Color.White,
                        containerColor = LightTrackSurface,
                        labelColor = Color.Black
                    )
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(24.dp))
    Divider(color = LightDivider, thickness = 1.dp)
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
            onSetDuration = { input ->
                val duration = input.toIntOrNull()
                if (duration != null && duration in MIN_FOCUS_DURATION..MAX_FOCUS_DURATION) {
                    viewModel.updateFocusDuration(duration)
                    showCustomDurationDialog = false
                }
                // If invalid, the dialog itself surfaces the error and stays open.
            }
        )
    }

    Spacer(modifier = Modifier.height(24.dp))
    Divider(color = LightDivider, thickness = 1.dp)
    Spacer(modifier = Modifier.height(16.dp))

    // Streak Freezes card — surfaces the user's banked freezes and
    // explains the earning rule, so the Home chip isn't the only
    // place they learn about the feature.
    StreakFreezeInfoCard(
        freezesAvailable = userPreferences?.freezesAvailable ?: 0,
        sessionsCompleted = userPreferences?.totalSessions ?: 0
    )
}

@Composable
private fun StreakFreezeInfoCard(
    freezesAvailable: Int,
    sessionsCompleted: Int
) {
    val freezeCountLabel = stringResource(
        id = R.string.freeze_count_label,
        freezesAvailable
    )
    val freezeExplanation = stringResource(
        id = R.string.freeze_explanation,
        com.focusstreak.app.data.FREEZE_GRANT_THRESHOLD
    )
    val earnedToNext = sessionsCompleted % com.focusstreak.app.data.FREEZE_GRANT_THRESHOLD
    val remainingToNext = com.focusstreak.app.data.FREEZE_GRANT_THRESHOLD - earnedToNext

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(LightTrackSurface)
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        SettingsIcon(
            icon = Icons.Filled.AcUnit,
            bgColor = LightIconBgBlue,
            tint = BrandFreezeBlueAccent
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = freezeCountLabel,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = freezeExplanation,
                fontSize = 13.sp,
                color = Color.Gray,
                lineHeight = 18.sp
            )
            if (freezesAvailable < com.focusstreak.app.data.MAX_FREEZE_COUNT) {
                Spacer(modifier = Modifier.height(8.dp))
                val progress = (sessionsCompleted % com.focusstreak.app.data.FREEZE_GRANT_THRESHOLD) /
                    com.focusstreak.app.data.FREEZE_GRANT_THRESHOLD.toFloat()
                LinearProgressIndicator(
                    progress = { progress },
                    color = BrandFreezeBlueAccent,
                    trackColor = LightSurfaceGray,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (remainingToNext == com.focusstreak.app.data.FREEZE_GRANT_THRESHOLD) {
                        stringResource(id = R.string.freeze_chip_zero)
                    } else {
                        "$remainingToNext sessions to next freeze"
                    },
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

private const val MIN_FOCUS_DURATION = 1
private const val MAX_FOCUS_DURATION = 180

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
            contentDescription = stringResource(id = R.string.cd_custom),
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
        // Delegate to platform DateFormat so the system 12/24h preference
        // is respected and the locale's time format is used.
        val cal = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, h)
            set(java.util.Calendar.MINUTE, m)
        }
        val flags = android.text.format.DateUtils.FORMAT_SHOW_TIME or
            if (android.text.format.DateFormat.is24HourFormat(context))
                android.text.format.DateUtils.FORMAT_24HOUR
            else
                android.text.format.DateUtils.FORMAT_12HOUR
        return android.text.format.DateUtils.formatDateTime(context, cal.timeInMillis, flags)
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
                        android.text.format.DateFormat.is24HourFormat(context)
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
    Divider(color = LightDivider, thickness = 1.dp)
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
fun DiagnosticsSectionContent(viewModel: SettingsViewModel) {
    val useTestAds by viewModel.useTestAds.collectAsState()
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SettingsIcon(
                    icon = Icons.Filled.BugReport,
                    bgColor = IconBgOrange,
                    tint = IconTintOrange
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = stringResource(id = R.string.use_test_ads),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                    Text(
                        text = stringResource(id = R.string.use_test_ads_subtitle),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
            Switch(
                checked = useTestAds,
                onCheckedChange = { viewModel.setUseTestAds(it) },
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
}

@Composable
fun AboutSectionContent() {
    val context = LocalContext.current
    val playStoreUrl = stringResource(id = R.string.play_store_listing_url)

    AboutItemRow(title = stringResource(id = R.string.rate_us), icon = null) {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(playStoreUrl)
        )
        context.startActivity(intent)
    }
    Divider(color = LightDivider, thickness = 1.dp)
    AboutItemRow(title = stringResource(id = R.string.privacy_policy), icon = Icons.Filled.ArrowForward) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://dhanuk.page.gd/FocusStreak/Privacy-Policy.html"))
        context.startActivity(intent)
    }
    Divider(color = LightDivider, thickness = 1.dp)

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
    val parsed = duration.toIntOrNull()
    val isInvalid = duration.isNotBlank() && (parsed == null || parsed !in MIN_FOCUS_DURATION..MAX_FOCUS_DURATION)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(id = R.string.set_custom_duration)) },
        text = {
            Column {
                TextField(
                    value = duration,
                    onValueChange = { newValue ->
                        // Restrict to digits only.
                        duration = newValue.filter { it.isDigit() }.take(3)
                    },
                    label = { Text(stringResource(id = R.string.duration_in_minutes)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = isInvalid,
                    supportingText = {
                        if (isInvalid) {
                            Text(
                                text = stringResource(id = R.string.duration_invalid),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSetDuration(duration) },
                enabled = !isInvalid && duration.isNotBlank()
            ) {
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
