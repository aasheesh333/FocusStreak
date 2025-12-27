package com.focusstreak.app.ui

import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@Composable
fun SettingsScreen(navController: NavController, settingsViewModel: SettingsViewModel = viewModel()) {
    var showDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item { Header(navController) }
            item { FocusSection(settingsViewModel) }
            item { NotificationsSection(settingsViewModel) }
            item { AppearanceSection(settingsViewModel) }
            item { AboutSection() }
            item {
                ResetSection {
                    showDialog = true
                }
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(text = stringResource(id = R.string.reset_dialog_title)) },
                text = { Text(text = stringResource(id = R.string.reset_dialog_message)) },
                confirmButton = {
                    Button(
                        onClick = {
                            settingsViewModel.resetAllProgress()
                            showDialog = false
                        }
                    ) {
                        Text(stringResource(id = R.string.reset))
                    }
                },
                dismissButton = {
                    Button(onClick = { showDialog = false }) {
                        Text(stringResource(id = R.string.cancel))
                    }
                }
            )
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
        Text(text = stringResource(id = R.string.settings), color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Spacer(modifier = Modifier.width(48.dp))
    }
}

@Composable
fun FocusSection(viewModel: SettingsViewModel) {
    val userPreferences by viewModel.userPreferencesFlow.collectAsState(initial = null)
    val selectedDuration = userPreferences?.focusDuration ?: 25
    var showCustomDurationDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(text = stringResource(id = R.string.focus), color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f), fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            SettingItem(stringResource(id = R.string.focus_duration), Icons.Default.Timer)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                DurationButton(25, selectedDuration) { viewModel.updateFocusDuration(it) }
                DurationButton(30, selectedDuration) { viewModel.updateFocusDuration(it) }
                DurationButton(45, selectedDuration) { viewModel.updateFocusDuration(it) }
                DurationButton(-1, selectedDuration) { showCustomDurationDialog = true }
            }
            SettingItem(stringResource(id = R.string.auto_start_break), Icons.Default.Autorenew, true, userPreferences?.autoStartBreak ?: false) {
                viewModel.updateAutoStartBreak(it)
            }
        }
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
fun DurationButton(duration: Int, selectedDuration: Int, onClick: (Int) -> Unit) {
    Button(
        onClick = { onClick(duration) },
        colors = ButtonDefaults.buttonColors(
            containerColor = if (duration == selectedDuration) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        )
    ) {
        if (duration > 0) {
            Text(text = stringResource(id = R.string.duration_format, duration))
        } else {
            Icon(imageVector = Icons.Default.Edit, contentDescription = stringResource(id = R.string.custom))
        }
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
            Button(onClick = onDismiss) {
                Text(stringResource(id = R.string.cancel))
            }
        }
    )
}

@Composable
fun NotificationsSection(viewModel: SettingsViewModel) {
    val context = LocalContext.current
    val userPreferences by viewModel.userPreferencesFlow.collectAsState(initial = null)
    var reminderTime by remember { mutableStateOf("09:00") }
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

    LaunchedEffect(userPreferences) {
        userPreferences?.let {
            reminderTime = context.getString(R.string.reminder_time_format, it.reminderHour, it.reminderMinute)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(text = stringResource(id = R.string.notifications), color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f), fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            SettingItem(stringResource(id = R.string.daily_reminder), Icons.Default.Notifications, hasSwitch = true, isChecked = userPreferences?.dailyReminderEnabled == true && hasNotificationPermission) {
                if (hasNotificationPermission) {
                    viewModel.updateDailyReminderEnabled(it)
                    if (it) {
                        val (hour, minute) = reminderTime.split(":").map { it.toInt() }
                        viewModel.scheduleDailyReminder(hour, minute)
                    } else {
                        viewModel.cancelDailyReminder()
                    }
                } else {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        launcher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }
            if (userPreferences?.dailyReminderEnabled == true && hasNotificationPermission) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = stringResource(id = R.string.remind_me_at), color = MaterialTheme.colorScheme.onSurface)
                    Button(onClick = {
                        val (hour, minute) = reminderTime.split(":").map { it.toInt() }
                        TimePickerDialog(
                            context,
                            { _, h, m ->
                                reminderTime = context.getString(R.string.reminder_time_format, h, m)
                                viewModel.scheduleDailyReminder(h, m)
                            },
                            hour,
                            minute,
                            false
                        ).show()
                    }) {
                        Text(text = reminderTime)
                    }
                }
            }
            SettingItem(stringResource(id = R.string.sound_effects), Icons.Default.VolumeUp, true, userPreferences?.soundEffectsEnabled ?: true) {
                // TODO: Implement sound effects
                viewModel.updateSoundEffectsEnabled(it)
            }
        }
    }
}

@Composable
fun AppearanceSection(viewModel: SettingsViewModel) {
    val userPreferences by viewModel.userPreferencesFlow.collectAsState(initial = null)
    val selectedTheme = userPreferences?.theme ?: "System"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(text = stringResource(id = R.string.appearance), color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f), fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            SettingItem(stringResource(id = R.string.theme), Icons.Default.Palette)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                ThemeButton(stringResource(id = R.string.light), selectedTheme) { viewModel.updateTheme("Light") }
                ThemeButton(stringResource(id = R.string.dark), selectedTheme) { viewModel.updateTheme("Dark") }
                ThemeButton(stringResource(id = R.string.system), selectedTheme) { viewModel.updateTheme("System") }
            }
        }
    }
}

@Composable
fun ThemeButton(theme: String, selectedTheme: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (theme == selectedTheme) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        )
    ) {
        Text(text = theme)
    }
}

@Composable
fun AboutSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(text = stringResource(id = R.string.about), color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f), fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            SettingItem(stringResource(id = R.string.rate_us), Icons.Default.Star) {
                // TODO: Implement rate us
            }
            SettingItem(stringResource(id = R.string.privacy_policy), Icons.Default.Lock) {
                // TODO: Implement privacy policy
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = stringResource(id = R.string.version), color = MaterialTheme.colorScheme.onSurface)
                Text(text = BuildConfig.VERSION_NAME, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
fun ResetSection(onResetClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = onResetClick,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Icon(imageVector = Icons.Default.Delete, contentDescription = stringResource(id = R.string.reset_all_progress))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = stringResource(id = R.string.reset_all_progress))
        }
    }
}

@Composable
fun SettingItem(
    title: String,
    icon: ImageVector,
    hasSwitch: Boolean = false,
    isChecked: Boolean = false,
    onCheckedChange: ((Boolean) -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange?.invoke(!isChecked) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = title, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = title, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
        if (hasSwitch) {
            Switch(checked = isChecked, onCheckedChange = onCheckedChange)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    FocusStreakTheme {
        SettingsScreen(rememberNavController())
    }
}
