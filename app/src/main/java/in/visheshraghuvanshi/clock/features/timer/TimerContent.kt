package `in`.visheshraghuvanshi.clock.features.timer

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import `in`.visheshraghuvanshi.clock.features.timer.components.TimerCircularDisplay
import `in`.visheshraghuvanshi.clock.features.timer.components.TimerNumpad

@Composable
fun TimerContent(
    viewModel: TimerViewModel = viewModel()
) {
    val isRunning by viewModel.isRunning.collectAsState()
    val isPaused by viewModel.isPaused.collectAsState()
    val totalTimeSeconds by viewModel.totalTime.collectAsState()
    val remainingTimeSeconds by viewModel.remainingTime.collectAsState()
    val inputString by viewModel.inputString.collectAsState()

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isLandscape = maxWidth > maxHeight
        val isMobilePortrait = !isLandscape && maxWidth < 600.dp
        val bottomPadding = if (isMobilePortrait && isRunning) 100.dp else 24.dp

        AnimatedContent(
            targetState = isRunning,
            transitionSpec = { fadeIn(tween(400)) togetherWith fadeOut(tween(400)) },
            label = "timer_state",
            modifier = Modifier.fillMaxSize()
        ) { running ->
            if (!running) {
                if (isLandscape) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 48.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            TimerInputText(inputString)
                        }
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                TimerNumpad(
                                    onNumberClick = viewModel::onNumberClick,
                                    onBackspace = viewModel::onBackspace,
                                    onStart = viewModel::startTimer
                                )
                            }
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        TimerInputText(inputString)
                        TimerNumpad(
                            onNumberClick = viewModel::onNumberClick,
                            onBackspace = viewModel::onBackspace,
                            onStart = viewModel::startTimer
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            } else {
                if (isLandscape) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            TimerCircularDisplay(
                                progress = if (totalTimeSeconds > 0) remainingTimeSeconds.toFloat() / totalTimeSeconds.toFloat() else 0f,
                                timeText = formatSeconds(remainingTimeSeconds)
                            )
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            TimerControls(
                                isPaused = isPaused,
                                onAddMinute = viewModel::addMinute,
                                onTogglePause = viewModel::togglePause,
                                onStop = viewModel::stopTimer,
                                isVertical = true
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            TimerCircularDisplay(
                                progress = if (totalTimeSeconds > 0) remainingTimeSeconds.toFloat() / totalTimeSeconds.toFloat() else 0f,
                                timeText = formatSeconds(remainingTimeSeconds)
                            )
                        }

                        Box(
                            modifier = Modifier.padding(bottom = bottomPadding),
                            contentAlignment = Alignment.Center
                        ) {
                            TimerControls(
                                isPaused = isPaused,
                                onAddMinute = viewModel::addMinute,
                                onTogglePause = viewModel::togglePause,
                                onStop = viewModel::stopTimer,
                                isVertical = false
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimerInputText(inputString: String) {
    Text(
        text = formatInputPremium(inputString),
        style = MaterialTheme.typography.displayLarge.copy(
            fontSize = 60.sp,
            fontWeight = FontWeight.ExtraLight,
            fontFeatureSettings = "tnum"
        ),
        color = if(inputString.isEmpty()) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f) else MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center,
        maxLines = 1
    )
}

@Composable
fun TimerControls(
    isPaused: Boolean,
    onAddMinute: () -> Unit,
    onTogglePause: () -> Unit,
    onStop: () -> Unit,
    isVertical: Boolean
) {
    if (isVertical) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ControlButtonsContent(isPaused, onAddMinute, onTogglePause, onStop)
        }
    } else {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            ControlButtonsContent(isPaused, onAddMinute, onTogglePause, onStop)
        }
    }
}

@Composable
fun ControlButtonsContent(
    isPaused: Boolean,
    onAddMinute: () -> Unit,
    onTogglePause: () -> Unit,
    onStop: () -> Unit
) {
    Button(
        onClick = onAddMinute,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.height(56.dp)
    ) {
        Text("+1m", fontWeight = FontWeight.SemiBold)
    }

    Spacer(modifier = Modifier.size(24.dp))

    FilledIconButton(
        onClick = onTogglePause,
        modifier = Modifier.size(80.dp),
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Icon(
            imageVector = if (isPaused) Icons.Rounded.PlayArrow else Icons.Rounded.Pause,
            contentDescription = null,
            modifier = Modifier.size(36.dp)
        )
    }

    Spacer(modifier = Modifier.size(24.dp))

    Button(
        onClick = onStop,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        ),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.height(56.dp)
    ) {
        Icon(Icons.Rounded.Stop, null)
    }
}

fun formatInputPremium(input: String): String {
    if (input.isEmpty()) return "00h 00m 00s"
    val padded = input.padStart(6, '0')
    val h = padded.take(2)
    val m = padded.substring(2, 4)
    val s = padded.substring(4, 6)
    return "${h}h ${m}m ${s}s"
}

fun formatSeconds(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) {
        String.format("%d:%02d:%02d", h, m, s)
    } else {
        String.format("%02d:%02d", m, s)
    }
}