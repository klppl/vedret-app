package se.vedret.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class VedretExtraColors(
    val rain: Color,
    val muted: Color,
    val faint: Color,
    val line: Color,
)

val LocalVedretExtras = staticCompositionLocalOf {
    VedretExtraColors(
        rain = VedretLight.Rain,
        muted = VedretLight.Muted,
        faint = VedretLight.Faint,
        line = VedretLight.Line,
    )
}

private val LightScheme = lightColorScheme(
    primary = VedretLight.Accent,
    onPrimary = Color.White,
    background = VedretLight.Bg,
    onBackground = VedretLight.Ink,
    surface = VedretLight.Surface,
    onSurface = VedretLight.Ink,
    surfaceVariant = VedretLight.Surface,
    onSurfaceVariant = VedretLight.Muted,
    outline = VedretLight.Line,
)

private val DarkScheme = darkColorScheme(
    primary = VedretDark.Accent,
    onPrimary = Color.Black,
    background = VedretDark.Bg,
    onBackground = VedretDark.Ink,
    surface = VedretDark.Surface,
    onSurface = VedretDark.Ink,
    surfaceVariant = VedretDark.Surface,
    onSurfaceVariant = VedretDark.Muted,
    outline = VedretDark.Line,
)

@Composable
fun VedretTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val scheme = if (darkTheme) DarkScheme else LightScheme
    val extras = if (darkTheme) {
        VedretExtraColors(VedretDark.Rain, VedretDark.Muted, VedretDark.Faint, VedretDark.Line)
    } else {
        VedretExtraColors(VedretLight.Rain, VedretLight.Muted, VedretLight.Faint, VedretLight.Line)
    }
    CompositionLocalProvider(LocalVedretExtras provides extras) {
        MaterialTheme(colorScheme = scheme, typography = VedretTypography, content = content)
    }
}
