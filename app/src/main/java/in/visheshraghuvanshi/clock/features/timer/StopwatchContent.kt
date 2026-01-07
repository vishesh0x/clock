package `in`.visheshraghuvanshi.clock.features.timer

import android.annotation.SuppressLint
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun StopwatchContent(
    viewModel: StopwatchViewModel = viewModel()
) {
    val isRunning by viewModel.isRunning.collectAsState()
    val timeMillis by viewModel.elapsedMillis.collectAsState()
    val laps by viewModel.laps.collectAsState()

    val listState = rememberLazyListState()

    val digitColor by animateColorAsState(
        targetValue = if (isRunning) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        label = "color"
    )
    LaunchedEffect(laps.size) {
        if (laps.isNotEmpty()) listState.animateScrollToItem(0)
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isPortrait = maxHeight > maxWidth
        val isMobile = maxWidth < 600.dp
        val hasBottomNav = isPortrait && isMobile
        val listBottomPadding = if (hasBottomNav) 100.dp else 16.dp

        if (isPortrait) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(60.dp))

                StopwatchDisplay(timeMillis, digitColor, fontSize = 90)

                Spacer(modifier = Modifier.height(60.dp))

                StopwatchControls(
                    isRunning = isRunning,
                    onToggle = viewModel::toggleStartPause,
                    onLapOrReset = viewModel::lapOrReset
                )

                Spacer(modifier = Modifier.height(40.dp))

                LapList(
                    laps = laps,
                    listState = listState,
                    bottomPadding = listBottomPadding,
                    modifier = Modifier.weight(1f).fillMaxWidth()
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp, vertical = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    StopwatchDisplay(timeMillis, digitColor, fontSize = 70)
                    Spacer(modifier = Modifier.height(32.dp))
                    StopwatchControls(
                        isRunning = isRunning,
                        onToggle = viewModel::toggleStartPause,
                        onLapOrReset = viewModel::lapOrReset
                    )
                }

                Spacer(modifier = Modifier.width(48.dp))

                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    LapList(
                        laps = laps,
                        listState = listState,
                        bottomPadding = 16.dp,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
fun StopwatchDisplay(timeMillis: Long, color: Color, fontSize: Int) {
    Text(
        text = formatStopwatch(timeMillis),
        style = MaterialTheme.typography.displayLarge.copy(
            fontSize = fontSize.sp,
            fontWeight = FontWeight.Light,
            fontFeatureSettings = "tnum",
            letterSpacing = (-3).sp
        ),
        color = color,
        maxLines = 1
    )
}

@Composable
fun StopwatchControls(
    isRunning: Boolean,
    onToggle: () -> Unit,
    onLapOrReset: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilledIconButton(
            onClick = onLapOrReset,
            modifier = Modifier.size(64.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Icon(
                imageVector = if (isRunning) Icons.Rounded.Flag else Icons.Rounded.Refresh,
                contentDescription = "Lap"
            )
        }

        Spacer(modifier = Modifier.width(32.dp))

        FilledIconButton(
            onClick = onToggle,
            modifier = Modifier.size(96.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = if (isRunning) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                contentColor = if (isRunning) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            Icon(
                imageVector = if (isRunning) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                contentDescription = "Start",
                modifier = Modifier.size(40.dp)
            )
        }
    }
}

@Composable
fun LapList(
    laps: List<Long>,
    listState: androidx.compose.foundation.lazy.LazyListState,
    bottomPadding: androidx.compose.ui.unit.Dp,
    modifier: Modifier
) {
    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(top = 16.dp, start = 24.dp, end = 24.dp, bottom = bottomPadding),
        modifier = modifier
    ) {
        itemsIndexed(laps) { index, lapTime ->
            val lapNumber = laps.size - index
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Lap $lapNumber",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                )
                Text(
                    formatStopwatch(lapTime),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontFeatureSettings = "tnum"
                    )
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
        }
    }
}

fun formatStopwatch(millis: Long): String {
    val minutes = (millis / 1000) / 60
    val seconds = (millis / 1000) % 60
    val hundreds = (millis % 1000) / 10
    return String.format("%02d:%02d.%02d", minutes, seconds, hundreds)
}