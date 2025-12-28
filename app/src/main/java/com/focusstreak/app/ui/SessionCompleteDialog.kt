package com.focusstreak.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.focusstreak.app.R
import com.focusstreak.app.ui.theme.FocusStreakTheme

@Composable
fun SessionCompleteDialog(
    onDismiss: () -> Unit,
    onStartAnotherSession: () -> Unit,
    onTakeBreak: () -> Unit,
    onGetBonusTime: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = stringResource(id = R.string.cancel), tint = MaterialTheme.colorScheme.onSurface)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Icon(
                    imageVector = Icons.Filled.CardGiftcard,
                    contentDescription = stringResource(id = R.string.session_complete),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(128.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(id = R.string.session_complete),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(id = R.string.great_focus),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(onClick = onStartAnotherSession, modifier = Modifier.fillMaxWidth()) {
                    Icon(imageVector = Icons.Default.Timer, contentDescription = stringResource(id = R.string.start_another_session))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(id = R.string.start_another_session))
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(onClick = onTakeBreak, modifier = Modifier.fillMaxWidth()) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = stringResource(id = R.string.take_a_break))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(id = R.string.take_a_break))
                }
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = onGetBonusTime) {
                    Text(text = stringResource(id = R.string.get_bonus_focus_time))
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
