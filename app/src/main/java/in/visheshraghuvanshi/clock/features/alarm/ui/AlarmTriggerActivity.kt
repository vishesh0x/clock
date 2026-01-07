package `in`.visheshraghuvanshi.clock.features.alarm.ui

import android.app.KeyguardManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Snooze
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import `in`.visheshraghuvanshi.clock.features.alarm.logic.AlarmService
import `in`.visheshraghuvanshi.clock.ui.theme.ClockTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

class AlarmTriggerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

// Validate and sanitize intent extras
        val label = intent.getStringExtra("ALARM_LABEL")?.takeIf { it.length <= 50 }?.let { 
            it.replace(Regex("[\n\r\t]"), " ").trim()
        } ?: "Alarm"
        val canSnooze = intent.getBooleanExtra("ALARM_SNOOZE", true)

        setContent {
            ClockTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    ExpressiveAlarmScreen(
                        label = label,
                        canSnooze = canSnooze,
                        onSnooze = {
                            startService(Intent(this, AlarmService::class.java).apply { action = "SNOOZE_ALARM" })
                            finish()
                        },
                        onDismiss = {
                            startService(Intent(this, AlarmService::class.java).apply { action = "STOP_ALARM" })
                            finish()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ExpressiveAlarmScreen(
    label: String,
    canSnooze: Boolean,
    onSnooze: () -> Unit,
    onDismiss: () -> Unit
) {
    var currentTime by remember { mutableStateOf(LocalTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = LocalTime.now()
            delay(1000)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AuroraBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Rounded.Alarm,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier
                        .size(48.dp)
                        .padding(bottom = 16.dp)
                )

                Text(
                    text = currentTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 120.sp,
                        fontWeight = FontWeight.Light,
                        letterSpacing = (-6).sp,
                        color = Color.White
                    ),
                    modifier = Modifier.offset(y = 10.dp)
                )

                Text(
                    text = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM")),
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = Color.White.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Normal
                    )
                )

                if (label.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(100),
                    ) {
                        Text(
                            text = label,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.padding(bottom = 40.dp)
            ) {
                if (canSnooze) {
                    SnoozeButton(onClick = onSnooze)
                }

                SwipeToDismissSlider(onDismiss = onDismiss)
            }
        }
    }
}

@Composable
fun AuroraBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "aurora")

    val offset1 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1000f,
        animationSpec = infiniteRepeatable(tween(20000, easing = LinearEasing), RepeatMode.Reverse),
        label = "blob1"
    )
    val offset2 by infiniteTransition.animateFloat(
        initialValue = 1000f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(25000, easing = LinearEasing), RepeatMode.Reverse),
        label = "blob2"
    )

    Canvas(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF312E81), Color.Transparent),
                center = Offset(size.width * 0.2f + offset1 / 4, size.height * 0.3f),
                radius = size.width * 0.8f
            )
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF831843).copy(alpha = 0.6f), Color.Transparent),
                center = Offset(size.width * 0.8f - offset2 / 4, size.height * 0.7f),
                radius = size.width * 0.9f
            )
        )
    }
}

@Composable
fun SnoozeButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White.copy(alpha = 0.1f),
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(24.dp),
        contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp),
        modifier = Modifier.fillMaxWidth(0.8f).height(64.dp)
    ) {
        Icon(Icons.Rounded.Snooze, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            "Snooze for 5 min",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium)
        )
    }
}

@Composable
fun SwipeToDismissSlider(onDismiss: () -> Unit) {
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    val trackHeight = 80.dp
    val knobSize = 72.dp
    val padding = 4.dp

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(trackHeight)
            .padding(horizontal = 16.dp)
    ) {
        val trackWidthPx = with(density) { maxWidth.toPx() }
        val knobSizePx = with(density) { knobSize.toPx() }
        val paddingPx = with(density) { padding.toPx() }

        val swipeOffset = remember { Animatable(0f) }
        val maxSwipeDistance = trackWidthPx - knobSizePx - (paddingPx * 2)
        val progress = (swipeOffset.value / maxSwipeDistance).coerceIn(0f, 1f)

        val trackColor by animateColorAsState(
            targetValue = if (progress > 0.9f) Color(0xFFF43F5E) else Color.White.copy(alpha = 0.15f),
            label = "trackColor"
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(100))
                .background(trackColor)
                .padding(padding),
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Slide to Stop",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp,
                        color = Color.White.copy(alpha = (1f - progress * 1.5f).coerceAtLeast(0f))
                    )
                )
            }

            Box(
                modifier = Modifier
                    .offset { IntOffset(swipeOffset.value.roundToInt(), 0) }
                    .size(knobSize)
                    .clip(CircleShape)
                    .background(Color.White)
                    .draggable(
                        orientation = Orientation.Horizontal,
                        state = rememberDraggableState { delta ->
                            scope.launch {
                                val newVal = (swipeOffset.value + delta).coerceIn(0f, maxSwipeDistance)
                                swipeOffset.snapTo(newVal)
                            }
                        },
                        onDragStopped = {
                            if (swipeOffset.value > maxSwipeDistance * 0.8f) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onDismiss()
                                scope.launch { swipeOffset.animateTo(maxSwipeDistance) }
                            } else {
                                scope.launch {
                                    swipeOffset.animateTo(
                                        targetValue = 0f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessLow
                                        )
                                    )
                                }
                            }
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = "Slide",
                    tint = Color.Black,
                    modifier = Modifier
                        .size(32.dp)
                        .graphicsLayer {
                            rotationZ = progress * 90f
                        }
                )
            }
        }
    }
}