package `in`.visheshraghuvanshi.clock.features.alarm.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Label
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun AlarmCard(
    time: String,
    label: String,
    tag: String,
    isActive: Boolean,
    backgroundColor: Color,
    isCompact: Boolean = false,
    onToggle: (Boolean) -> Unit,
    onClick: () -> Unit
) {
    val parsedTime = remember(time) {
        try {
            val localTime = LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"))
            val formatter = DateTimeFormatter.ofPattern("h:mm")
            val amPmFormatter = DateTimeFormatter.ofPattern("a")
            Pair(localTime.format(formatter), localTime.format(amPmFormatter))
        } catch (e: Exception) {
            Pair(time, "")
        }
    }
    val timeDisplay = parsedTime.first
    val amPmDisplay = parsedTime.second

    val containerColor = if (isActive) backgroundColor else MaterialTheme.colorScheme.surfaceContainerHighest

    val contentColor = if (isActive) Color(0xFF1C1B1F) else MaterialTheme.colorScheme.onSurface
    val secondaryColor = if (isActive) Color(0xFF1C1B1F).copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isCompact) Modifier.aspectRatio(1.4f) else Modifier.heightIn(min = 160.dp))
            .clickable(onClick = onClick)
            .animateContentSize(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (label.isEmpty()) "Alarm" else label,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = contentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (tag.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        TagBadge(tag = tag, isActive = isActive, contentColor = contentColor)
                    }
                }

                Switch(
                    checked = isActive,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color.Black.copy(alpha = 0.15f),
                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                        uncheckedTrackColor = Color.Transparent,
                        uncheckedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier
                        .scale(0.8f)
                        .offset(x = 8.dp, y = (-8).dp)
                )
            }

            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = timeDisplay,
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontSize = if (isCompact) 52.sp else 72.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = (-2).sp
                    ),
                    color = contentColor,
                    modifier = Modifier.alignByBaseline()
                )

                if (amPmDisplay.isNotEmpty()) {
                    Text(
                        text = amPmDisplay.lowercase(),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = if (isCompact) 20.sp else 24.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        color = secondaryColor,
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .alignByBaseline()
                    )
                }
            }
        }
    }
}

@Composable
fun TagBadge(tag: String, isActive: Boolean, contentColor: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isActive) Color.Black.copy(alpha = 0.05f)
                else MaterialTheme.colorScheme.surfaceContainerHigh
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Rounded.Label,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = contentColor.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = tag,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = 0.7f)
            )
        }
    }
}

fun Modifier.scale(scale: Float) = this.then(
    Modifier.graphicsLayer(scaleX = scale, scaleY = scale)
)