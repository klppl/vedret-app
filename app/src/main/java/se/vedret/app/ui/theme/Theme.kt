package se.vedret.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import se.vedret.app.data.ThemeMode

data class VedretExtraColors(
    val rain: Color,
    val muted: Color,
    val faint: Color,
    val line: Color,
)

val LocalVedretExtras = staticCompositionLocalOf {
    VedretExtraColors(
        rain = RosePine.Rain,
        muted = RosePine.Muted,
        faint = RosePine.Faint,
        line = RosePine.Line,
    )
}

private val RosePineScheme = darkColorScheme(
    primary = RosePine.Accent,
    onPrimary = RosePine.Bg,
    background = RosePine.Bg,
    onBackground = RosePine.Ink,
    surface = RosePine.Surface,
    onSurface = RosePine.Ink,
    surfaceVariant = RosePine.Surface,
    onSurfaceVariant = RosePine.Muted,
    outline = RosePine.Line,
    error = RosePine.Love,
)

private val RosePineDawnScheme = lightColorScheme(
    primary = RosePineDawn.Accent,
    onPrimary = RosePineDawn.Bg,
    background = RosePineDawn.Bg,
    onBackground = RosePineDawn.Ink,
    surface = RosePineDawn.Surface,
    onSurface = RosePineDawn.Ink,
    surfaceVariant = RosePineDawn.Surface,
    onSurfaceVariant = RosePineDawn.Muted,
    outline = RosePineDawn.Line,
    error = RosePineDawn.Love,
)

@Composable
fun VedretTheme(
    mode: ThemeMode = ThemeMode.RosePine,
    content: @Composable () -> Unit,
) {
    val isDark = mode == ThemeMode.RosePine
    val scheme = if (isDark) RosePineScheme else RosePineDawnScheme
    val extras = if (isDark) {
        VedretExtraColors(RosePine.Rain, RosePine.Muted, RosePine.Faint, RosePine.Line)
    } else {
        VedretExtraColors(RosePineDawn.Rain, RosePineDawn.Muted, RosePineDawn.Faint, RosePineDawn.Line)
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !isDark
            controller.isAppearanceLightNavigationBars = !isDark
        }
    }

    CompositionLocalProvider(LocalVedretExtras provides extras) {
        MaterialTheme(colorScheme = scheme, typography = VedretTypography, content = content)
    }
}
