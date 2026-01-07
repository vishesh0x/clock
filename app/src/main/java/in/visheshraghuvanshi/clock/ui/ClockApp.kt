package `in`.visheshraghuvanshi.clock.ui

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import `in`.visheshraghuvanshi.clock.features.alarm.AlarmScreen
import `in`.visheshraghuvanshi.clock.features.alarm.EditAlarmScreen
import `in`.visheshraghuvanshi.clock.features.clock.ClockScreen
import `in`.visheshraghuvanshi.clock.features.settings.SettingsScreen
import `in`.visheshraghuvanshi.clock.features.timer.TimerScreen
import `in`.visheshraghuvanshi.clock.navigation.Screen
import `in`.visheshraghuvanshi.clock.ui.theme.ClockTheme
import `in`.visheshraghuvanshi.clock.ui.theme.SeedBlue
import kotlin.math.roundToInt

@Composable
fun ClockApp(
    isSystemDark: Boolean
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("clock_settings", Context.MODE_PRIVATE) }

    var themeMode by remember { mutableStateOf(prefs.getString("theme", "system") ?: "system") }
    var useDynamicColors by remember { mutableStateOf(prefs.getBoolean("dynamic", true)) }
    var useAmoled by remember { mutableStateOf(prefs.getBoolean("amoled", false)) }

    val savedColorArgb = prefs.getInt("custom_color", SeedBlue.toArgb())
    var customColor by remember { mutableStateOf(Color(savedColorArgb)) }

    val isDarkTheme = when (themeMode) {
        "light" -> false
        "dark" -> true
        else -> isSystemDark
    }

    ClockTheme(
        darkTheme = isDarkTheme,
        useDynamicColors = useDynamicColors,
        useAmoled = useAmoled,
        customColor = customColor
    ) {
        val view = LocalView.current
        if (!view.isInEditMode) {
            SideEffect {
                val window = (view.context as Activity).window
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDarkTheme
            }
        }

        val navController = rememberNavController()
        val items = listOf(Screen.Clock, Screen.Alarm, Screen.Timer, Screen.Settings)
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        val configuration = LocalConfiguration.current
        val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val isTablet = configuration.screenWidthDp >= 600
        val useNavRail = isLandscape || isTablet
        val isNavVisible = currentRoute?.startsWith("edit_alarm") == false
        val systemBarsPadding = WindowInsets.systemBars.asPaddingValues()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                if (useNavRail && isNavVisible) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(start = 24.dp, top = 24.dp, bottom = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        LiquidNavRail(
                            items = items,
                            currentRoute = currentRoute,
                            isDarkTheme = isDarkTheme,
                            onItemClick = { screen ->
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }

                Scaffold(
                    containerColor = Color.Transparent,
                    contentWindowInsets = WindowInsets(0, 0, 0, 0)
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .padding(innerPadding)
                            .padding(start = if (useNavRail && isNavVisible) 16.dp else 0.dp)
                            .fillMaxSize()
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = Screen.Clock.route
                        ) {
                            composable(
                                route = Screen.Clock.route,
                                enterTransition = { slideInHorizontally { getSlideOffset(initialState, targetState, items, it) } + fadeIn() },
                                exitTransition = { slideOutHorizontally { getSlideOffset(initialState, targetState, items, -it) } + fadeOut() }
                            ) { ClockScreen() }

                            composable(
                                route = Screen.Alarm.route,
                                enterTransition = { slideInHorizontally { getSlideOffset(initialState, targetState, items, it) } + fadeIn() },
                                exitTransition = { slideOutHorizontally { getSlideOffset(initialState, targetState, items, -it) } + fadeOut() }
                            ) { AlarmScreen(navController = navController) }

                            composable(
                                route = Screen.Timer.route,
                                enterTransition = { slideInHorizontally { getSlideOffset(initialState, targetState, items, it) } + fadeIn() },
                                exitTransition = { slideOutHorizontally { getSlideOffset(initialState, targetState, items, -it) } + fadeOut() }
                            ) { TimerScreen() }

                            composable(
                                route = Screen.Settings.route,
                                enterTransition = { slideInHorizontally { getSlideOffset(initialState, targetState, items, it) } + fadeIn() },
                                exitTransition = { slideOutHorizontally { getSlideOffset(initialState, targetState, items, -it) } + fadeOut() }
                            ) {
                                SettingsScreen(
                                    currentThemeMode = themeMode,
                                    onThemeChanged = {
                                        themeMode = it
                                        prefs.edit().putString("theme", it).apply()
                                    },
                                    useDynamicColors = useDynamicColors,
                                    onDynamicColorChanged = {
                                        useDynamicColors = it
                                        prefs.edit().putBoolean("dynamic", it).apply()
                                    },
                                    useAmoled = useAmoled,
                                    onAmoledChanged = {
                                        useAmoled = it
                                        prefs.edit().putBoolean("amoled", it).apply()
                                    },
                                    currentCustomColor = customColor,
                                    onCustomColorChanged = {
                                        customColor = it
                                        prefs.edit().putInt("custom_color", it.toArgb()).apply()
                                    }
                                )
                            }

                            composable(
                                route = Screen.EditAlarm.route,
                                arguments = listOf(navArgument("alarmId") { type = NavType.IntType }),
                                enterTransition = { slideInVertically { it } + fadeIn() },
                                exitTransition = { slideOutVertically { it } + fadeOut() }
                            ) { backStackEntry ->
                                val alarmId = backStackEntry.arguments?.getInt("alarmId") ?: -1
                                EditAlarmScreen(
                                    alarmId = alarmId,
                                    onCancel = { navController.popBackStack() },
                                    onSave = { navController.popBackStack() }
                                )
                            }
                        }
                    }
                }
            }

            if (!useNavRail) {
                AnimatedVisibility(
                    visible = isNavVisible,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp + systemBarsPadding.calculateBottomPadding()),
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut()
                ) {
                    LiquidNavBar(
                        items = items,
                        currentRoute = currentRoute,
                        isDarkTheme = isDarkTheme,
                        onItemClick = { screen ->
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    }
}

fun getSlideOffset(initial: NavBackStackEntry, target: NavBackStackEntry, items: List<Screen>, fullWidth: Int): Int {
    val initialRoute = initial.destination.route
    val targetRoute = target.destination.route
    val initialIndex = items.indexOfFirst { it.route == initialRoute }
    val targetIndex = items.indexOfFirst { it.route == targetRoute }
    return if (targetIndex > initialIndex) fullWidth else -fullWidth
}

@Composable
fun LiquidNavBar(items: List<Screen>, currentRoute: String?, isDarkTheme: Boolean, onItemClick: (Screen) -> Unit) {
    val activeIndex = items.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)

    NavContainer(
        isDarkTheme = isDarkTheme,
        modifier = Modifier.fillMaxWidth(0.92f).height(72.dp)
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val containerWidthPx = constraints.maxWidth.toFloat()
            val tabWidthPx = if (items.isNotEmpty()) containerWidthPx / items.size else 0f

            val density = LocalDensity.current
            val tabWidthDp = with(density) { tabWidthPx.toDp() }

            val indicatorOffset by animateFloatAsState(
                targetValue = activeIndex * tabWidthPx,
                animationSpec = spring(dampingRatio = 0.7f, stiffness = 300f)
            )

            if (tabWidthPx > 0) {
                Box(
                    modifier = Modifier
                        .width(tabWidthDp)
                        .fillMaxHeight()
                        .offset { IntOffset(indicatorOffset.roundToInt(), 0) }
                        .padding(12.dp)
                ) {
                    BlobIndicator()
                }
            }
            Row(Modifier.fillMaxSize()) {
                items.forEachIndexed { index, screen ->
                    NavIconItem(
                        screen = screen,
                        isSelected = index == activeIndex,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        onClick = { onItemClick(screen) }
                    )
                }
            }
        }
    }
}

@Composable
fun LiquidNavRail(items: List<Screen>, currentRoute: String?, isDarkTheme: Boolean, onItemClick: (Screen) -> Unit) {
    val activeIndex = items.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)

    NavContainer(
        isDarkTheme = isDarkTheme,
        modifier = Modifier.width(72.dp).height(320.dp)
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val containerHeightPx = constraints.maxHeight.toFloat()
            val tabHeightPx = if (items.isNotEmpty()) containerHeightPx / items.size else 0f

            val density = LocalDensity.current
            val tabHeightDp = with(density) { tabHeightPx.toDp() }

            val indicatorOffset by animateFloatAsState(
                targetValue = activeIndex * tabHeightPx,
                animationSpec = spring(dampingRatio = 0.7f, stiffness = 300f)
            )

            if (tabHeightPx > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(tabHeightDp)
                        .offset { IntOffset(0, indicatorOffset.roundToInt()) }
                        .padding(12.dp)
                ) {
                    BlobIndicator()
                }
            }
            Column(Modifier.fillMaxSize()) {
                items.forEachIndexed { index, screen ->
                    NavIconItem(
                        screen = screen,
                        isSelected = index == activeIndex,
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        onClick = { onItemClick(screen) }
                    )
                }
            }
        }
    }
}

@Composable
inline fun NavContainer(
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val glassColor = if (isDarkTheme) Color(0xFF1E1E1E).copy(alpha = 0.9f) else Color(0xFFFFFFFF).copy(alpha = 0.9f)
    val borderColor = if (isDarkTheme) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f)
    val shadowColor = if (isDarkTheme) Color.Black.copy(alpha = 0.4f) else Color.Black.copy(alpha = 0.1f)

    Box(
        modifier = modifier
            .shadow(24.dp, RoundedCornerShape(50), spotColor = shadowColor)
            .clip(RoundedCornerShape(50))
            .background(glassColor)
            .border(1.dp, borderColor, RoundedCornerShape(50)),
        content = content
    )
}

@Composable
fun BlobIndicator() {
    val gradientColors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
    Box(modifier = Modifier
        .fillMaxSize()
        .clip(RoundedCornerShape(40))
        .background(Brush.linearGradient(colors = gradientColors))
        .graphicsLayer { alpha = 0.9f }
    )
}

@Composable
fun NavIconItem(screen: Screen, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    val activeIconTint = MaterialTheme.colorScheme.onPrimary
    val inactiveIconTint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    val scale by animateFloatAsState(if (isSelected) 1.1f else 1.0f, spring(dampingRatio = 0.5f))

    Box(
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        },
        contentAlignment = Alignment.Center
    ) {
        Crossfade(targetState = isSelected) { selected ->
            Icon(
                imageVector = if (selected) screen.activeIcon else screen.inactiveIcon,
                contentDescription = screen.label,
                tint = if (selected) activeIconTint else inactiveIconTint,
                modifier = Modifier.size(26.dp).scale(scale)
            )
        }
    }
}