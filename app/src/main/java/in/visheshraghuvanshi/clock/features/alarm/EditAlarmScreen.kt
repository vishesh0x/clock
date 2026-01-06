package `in`.visheshraghuvanshi.clock.features.alarm

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Label
import androidx.compose.material.icons.rounded.Snooze
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.visheshraghuvanshi.clock.core.data.ClockDatabase
import `in`.visheshraghuvanshi.clock.features.alarm.logic.AlarmScheduler
import `in`.visheshraghuvanshi.clock.features.settings.components.SettingsGroup
import `in`.visheshraghuvanshi.clock.features.settings.components.SettingsItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAlarmScreen(
    alarmId: Int,
    onCancel: () -> Unit,
    onSave: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val database = remember { ClockDatabase.getDatabase(context) }
    val alarmDao = remember { database.alarmDao() }
    val alarmScheduler = remember { AlarmScheduler(context) }

    val timeState = rememberTimePickerState()

    var label by remember { mutableStateOf("") }
    var isVibrationEnabled by remember { mutableStateOf(true) }
    var isSnoozeEnabled by remember { mutableStateOf(true) }

    var showLabelDialog by remember { mutableStateOf(false) }

    val timePickerTypography = MaterialTheme.typography.copy(
        displayLarge = MaterialTheme.typography.displayLarge.copy(fontSize = 52.sp)
    )

    LaunchedEffect(alarmId) {
        val alarm = withContext(Dispatchers.IO) { alarmDao.getAlarmById(alarmId) }
        alarm?.let {
            val parts = it.time.split(":")
            if (parts.size == 2) {
                timeState.hour = parts[0].toInt()
                timeState.minute = parts[1].toInt()
            }
            label = it.label
            isSnoozeEnabled = it.isSnoozeEnabled
            isVibrationEnabled = it.isVibrationEnabled
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Edit Alarm", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                },
                navigationIcon = { TextButton(onClick = onCancel) { Text("Cancel") } },
                actions = {
                    Button(
                        onClick = {
                            scope.launch(Dispatchers.IO) {
                                val formattedTime = String.format("%02d:%02d", timeState.hour, timeState.minute)
                                val originalAlarm = alarmDao.getAlarmById(alarmId)

                                if (originalAlarm != null) {
                                    val updatedAlarm = originalAlarm.copy(
                                        time = formattedTime,
                                        label = label,
                                        isActive = true,
                                        isSnoozeEnabled = isSnoozeEnabled,
                                        isVibrationEnabled = isVibrationEnabled
                                    )
                                    alarmDao.updateAlarm(updatedAlarm)
                                    alarmScheduler.schedule(updatedAlarm)
                                }
                                withContext(Dispatchers.Main) { onSave() }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) { Text("Save") }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            androidx.compose.foundation.layout.Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                MaterialTheme(typography = timePickerTypography) {
                    TimePicker(state = timeState)
                }
            }

            SettingsGroup(title = "Options") {
                SettingsItem(
                    label = "Label",
                    value = label.ifBlank { "Alarm" },
                    icon = Icons.Rounded.Label,
                    iconColor = MaterialTheme.colorScheme.primary,
                    onClick = { showLabelDialog = true }
                )
                SettingsItem(
                    label = "Snooze",
                    icon = Icons.Rounded.Snooze,
                    iconColor = Color(0xFFFF9800),
                    isSwitch = true,
                    isChecked = isSnoozeEnabled,
                    onCheckedChange = { isSnoozeEnabled = it }
                )
                SettingsItem(
                    label = "Vibration",
                    icon = Icons.Rounded.Vibration,
                    iconColor = Color(0xFFE91E63),
                    isSwitch = true,
                    isChecked = isVibrationEnabled,
                    onCheckedChange = { isVibrationEnabled = it },
                    showDivider = false
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = {
                    scope.launch(Dispatchers.IO) {
                        val alarmToDelete = alarmDao.getAlarmById(alarmId)
                        alarmToDelete?.let {
                            alarmScheduler.cancel(it)
                            alarmDao.deleteAlarm(it)
                        }
                        withContext(Dispatchers.Main) { onSave() }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(56.dp)
            ) {
                Icon(Icons.Rounded.Delete, null)
                Spacer(modifier = Modifier.padding(4.dp))
                Text("Delete Alarm")
            }
        }
    }

    if (showLabelDialog) {
        EditLabelDialog(
            initialLabel = label,
            onDismiss = { showLabelDialog = false },
            onConfirm = {
                label = it
                showLabelDialog = false
            }
        )
    }
}

@Composable
fun EditLabelDialog(
    initialLabel: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf(initialLabel) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        title = { Text("Alarm Label", fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                singleLine = true,
                placeholder = { Text("Enter label") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(text) }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}