package `in`.visheshraghuvanshi.clock.features.settings

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.visheshraghuvanshi.clock.features.settings.components.SettingsGroup
import `in`.visheshraghuvanshi.clock.features.settings.components.SettingsItem
import `in`.visheshraghuvanshi.clock.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentThemeMode: String,
    onThemeChanged: (String) -> Unit,
    useDynamicColors: Boolean,
    onDynamicColorChanged: (Boolean) -> Unit,
    useAmoled: Boolean,
    onAmoledChanged: (Boolean) -> Unit,
    currentCustomColor: Color,
    onCustomColorChanged: (Color) -> Unit
) {
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val scrollState = rememberScrollState()
    var showThemeDialog by remember { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val isWideScreen = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE || configuration.screenWidthDp >= 600
    val bottomContentPadding = if (isWideScreen) 32.dp else 100.dp

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = Color.Transparent,
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        "Settings",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-1).sp
                        )
                    )
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = 600.dp)
                    .verticalScroll(scrollState)
                    .padding(bottom = bottomContentPadding),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SettingsGroup(title = "Appearance") {
                    SettingsItem(
                        label = "App Theme",
                        value = formatThemeName(currentThemeMode),
                        icon = Icons.Rounded.DarkMode,
                        iconColor = MaterialTheme.colorScheme.primary,
                        onClick = { showThemeDialog = true }
                    )

                    SettingsItem(
                        label = "AMOLED Black",
                        value = "Pure black background",
                        icon = Icons.Rounded.Contrast,
                        iconColor = MaterialTheme.colorScheme.onSurface,
                        isSwitch = true,
                        isChecked = useAmoled,
                        onCheckedChange = onAmoledChanged
                    )

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        SettingsItem(
                            label = "Use Wallpaper Colors",
                            icon = Icons.Rounded.Palette,
                            iconColor = if (useDynamicColors) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline,
                            isSwitch = true,
                            isChecked = useDynamicColors,
                            onCheckedChange = onDynamicColorChanged,
                            showDivider = !useDynamicColors
                        )
                    }

                    AnimatedVisibility(
                        visible = !useDynamicColors,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column {
                            ColorPickerRow(
                                selectedColor = currentCustomColor,
                                onColorSelected = onCustomColorChanged
                            )
                        }
                    }
                }

                SettingsGroup(title = "Support") {
                    SettingsItem(
                        label = "Buy me a coffee",
                        value = "Support development",
                        icon = Icons.Rounded.LocalCafe,
                        iconColor = Color(0xFFFFC107),
                        showDivider = false,
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.buymeacoffee.com/visheshraghuvanshi"))
                            context.startActivity(intent)
                        }
                    )
                }

                SettingsGroup(title = "About") {
                    SettingsItem(
                        label = "Version",
                        value = "0.2.0",
                        icon = Icons.Rounded.Info,
                        iconColor = Color(0xFF4CAF50)
                    )

                    SettingsItem(
                        label = "Website",
                        value = "clock.visheshraghuvanshi.in",
                        icon = Icons.Rounded.Language,
                        iconColor = Color(0xFF2196F3),
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://clock.visheshraghuvanshi.in"))
                            context.startActivity(intent)
                        }
                    )

                    SettingsItem(
                        label = "GitHub",
                        value = "vishesh0x/clock",
                        icon = Icons.Rounded.Code,
                        iconColor = Color(0xFF607D8B),
                        showDivider = false,
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/vishesh0x/clock"))
                            context.startActivity(intent)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Made with ❤️ in India",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }

        if (showThemeDialog) {
            ThemeSelectionDialog(
                currentMode = currentThemeMode,
                onThemeSelected = {
                    onThemeChanged(it)
                    showThemeDialog = false
                },
                onDismiss = { showThemeDialog = false }
            )
        }
    }
}

@Composable
fun ColorPickerRow(selectedColor: Color, onColorSelected: (Color) -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            "Accent Color",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AppColors.forEach { color ->
                val isSelected = color == selectedColor
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(color)
                        .clickable { onColorSelected(color) }
                        .border(
                            width = if (isSelected) 3.dp else 0.dp,
                            color = MaterialTheme.colorScheme.onSurface,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            tint = if (color.luminance() > 0.5f) Color.Black else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

fun Color.luminance(): Float {
    return (0.299 * red + 0.587 * green + 0.114 * blue).toFloat()
}

@Composable
fun ThemeSelectionDialog(currentMode: String, onThemeSelected: (String) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose Appearance", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemeOption("System Default", "system", currentMode, onThemeSelected)
                ThemeOption("Light", "light", currentMode, onThemeSelected)
                ThemeOption("Dark", "dark", currentMode, onThemeSelected)
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(28.dp)
    )
}

@Composable
fun ThemeOption(label: String, mode: String, current: String, onSelect: (String) -> Unit) {
    val isSelected = mode == current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onSelect(mode) }
            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent)
            .padding(16.dp, 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            Modifier.weight(1f),
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
        if (isSelected) Icon(Icons.Rounded.Check, null, tint = MaterialTheme.colorScheme.primary)
    }
}

fun formatThemeName(mode: String): String = when (mode) {
    "light" -> "Light"
    "dark" -> "Dark"
    else -> "System Default"
}