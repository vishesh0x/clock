package `in`.visheshraghuvanshi.clock.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    surface = SurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    background = SurfaceDark
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimaryLight,
    surface = SurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    primaryContainer = PrimaryContainerLight,
    background = SurfaceLight
)

@Composable
fun ClockTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    useDynamicColors: Boolean = true,
    useAmoled: Boolean = false,
    customColor: Color = SeedBlue,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val baseScheme = when {
        useDynamicColors && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme.copy(
            primary = customColor,
            primaryContainer = customColor.copy(alpha = 0.2f),
            onPrimary = Color.White
        )
        else -> LightColorScheme.copy(
            primary = customColor,
            primaryContainer = customColor.copy(alpha = 0.1f),
            onPrimary = Color.White
        )
    }
    val finalScheme = if (darkTheme && useAmoled) {
        baseScheme.copy(
            background = Color.Black,
            surface = Color.Black,
            surfaceContainer = Color.Black,
            surfaceVariant = Color(0xFF121212)
        )
    } else {
        baseScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = finalScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}