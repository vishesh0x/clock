package `in`.visheshraghuvanshi.clock.features.alarm

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AlarmOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import `in`.visheshraghuvanshi.clock.core.data.ClockDatabase
import `in`.visheshraghuvanshi.clock.features.alarm.components.AddAlarmSheet
import `in`.visheshraghuvanshi.clock.features.alarm.components.AlarmCard
import `in`.visheshraghuvanshi.clock.features.alarm.data.AlarmEntity
import `in`.visheshraghuvanshi.clock.features.alarm.logic.AlarmScheduler
import `in`.visheshraghuvanshi.clock.navigation.Screen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AlarmScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val config = LocalConfiguration.current

    val isTablet = config.screenWidthDp >= 600
    val isLandscape = config.orientation == Configuration.ORIENTATION_LANDSCAPE
    val gridCells = if (isTablet || isLandscape) StaggeredGridCells.Fixed(2) else StaggeredGridCells.Fixed(1)

    val fabPaddingBottom = if (isTablet || isLandscape) 16.dp else 100.dp
    val listContentBottomPadding = if (isTablet || isLandscape) 16.dp else 110.dp

    val database = remember { ClockDatabase.getDatabase(context) }
    val alarmDao = remember { database.alarmDao() }
    val alarms by alarmDao.getAllAlarms().collectAsState(initial = emptyList())
    val alarmScheduler = remember { AlarmScheduler(context) }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val nextAlarmText = remember(alarms) { calculateNextAlarmText(alarms) }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = Color.Transparent,
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            "Alarms",
                            style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        AnimatedVisibility(visible = nextAlarmText != null) {
                            Text(
                                text = nextAlarmText ?: "",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                ),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showBottomSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(20.dp),
                elevation = FloatingActionButtonDefaults.elevation(8.dp),
                modifier = Modifier
                    .padding(bottom = fabPaddingBottom, end = 8.dp)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add Alarm", modifier = Modifier.size(28.dp))
            }
        }
    ) { innerPadding ->

        if (alarms.isEmpty()) {
            EmptyAlarmState(modifier = Modifier.padding(innerPadding))
        } else {
            LazyVerticalStaggeredGrid(
                columns = gridCells,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding() + 8.dp,
                    start = 16.dp,
                    end = 16.dp,
                    bottom = listContentBottomPadding
                ),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalItemSpacing = 12.dp
            ) {
                items(
                    items = alarms,
                    key = { it.id }
                ) { alarm ->
                    Box(modifier = Modifier.animateItemPlacement(
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                    )) {
                        AlarmCard(
                            time = alarm.time,
                            label = alarm.label,
                            tag = alarm.tag,
                            isActive = alarm.isActive,
                            backgroundColor = Color(alarm.colorArgb),
                            isCompact = isTablet || isLandscape,
                            onToggle = { isChecked ->
                                scope.launch(Dispatchers.IO) {
                                    val updatedAlarm = alarm.copy(isActive = isChecked)
                                    alarmDao.updateAlarm(updatedAlarm)
                                    if (isChecked) alarmScheduler.schedule(updatedAlarm) else alarmScheduler.cancel(updatedAlarm)
                                }
                            },
                            onClick = {
                                navController.navigate(Screen.EditAlarm.createRoute(alarm.id))
                            }
                        )
                    }
                }
            }
        }

        if (showBottomSheet) {
            AddAlarmSheet(
                sheetState = sheetState,
                onDismiss = { showBottomSheet = false },
                onSave = { time, label, tag, color ->
                    scope.launch(Dispatchers.IO) {
                        val newAlarm = AlarmEntity(time = time, label = label, tag = tag, colorArgb = color.toArgb(), isActive = true)
                        alarmDao.insertAlarm(newAlarm)
                        alarmDao.getLatestAlarm()?.let { alarmScheduler.schedule(it) }
                        launch(Dispatchers.Main) {
                            sheetState.hide()
                            showBottomSheet = false
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun EmptyAlarmState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Rounded.AlarmOff,
            contentDescription = null,
            modifier = Modifier.size(100.dp).padding(bottom = 16.dp),
            tint = MaterialTheme.colorScheme.surfaceVariant
        )
        Text(
            "No Alarms",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            "You can sleep tight... for now.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

private fun calculateNextAlarmText(alarms: List<AlarmEntity>): String? {
    val activeAlarms = alarms.filter { it.isActive }
    if (activeAlarms.isEmpty()) return null

    val now = LocalTime.now()
    val today = java.time.LocalDate.now()

    val nextAlarm = activeAlarms
        .map { alarm ->
            val alarmTime = LocalTime.parse(alarm.time, DateTimeFormatter.ofPattern("HH:mm"))
            val alarmDateTime = if (alarmTime.isAfter(now)) {
                LocalDateTime.of(today, alarmTime)
            } else {
                LocalDateTime.of(today.plusDays(1), alarmTime)
            }
            alarmDateTime
        }
        .minOrNull() ?: return null

    val duration = Duration.between(LocalDateTime.now(), nextAlarm)
    val hours = duration.toHours()
    val minutes = duration.toMinutes() % 60

    return if (hours > 0) {
        "Alarm in ${hours}h ${minutes}m"
    } else {
        "Alarm in ${minutes}m"
    }
}