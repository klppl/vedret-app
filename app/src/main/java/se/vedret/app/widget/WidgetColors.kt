package se.vedret.app.widget

import androidx.compose.ui.graphics.Color
import androidx.glance.unit.ColorProvider
import se.vedret.app.data.ThemeMode

internal class WidgetPalette(
    val surface: ColorProvider,
    val ink: ColorProvider,
    val muted: ColorProvider,
    val faint: ColorProvider,
    val line: ColorProvider,
    val accent: ColorProvider,
    val rain: ColorProvider,
)

internal fun paletteFor(theme: ThemeMode): WidgetPalette = when (theme) {
    ThemeMode.RosePine -> RosePineDarkPalette
    ThemeMode.RosePineDawn -> RosePineDawnPalette
}

private val RosePineDarkPalette = WidgetPalette(
    surface = ColorProvider(Color(0xFF1F1D2E)),
    ink = ColorProvider(Color(0xFFE0DEF4)),
    muted = ColorProvider(Color(0xFF908CAA)),
    faint = ColorProvider(Color(0xFF6E6A86)),
    line = ColorProvider(Color(0xFF403D52)),
    accent = ColorProvider(Color(0xFFF6C177)),
    rain = ColorProvider(Color(0xFF9CCFD8)),
)

private val RosePineDawnPalette = WidgetPalette(
    surface = ColorProvider(Color(0xFFFFFAF3)),
    ink = ColorProvider(Color(0xFF575279)),
    muted = ColorProvider(Color(0xFF797593)),
    faint = ColorProvider(Color(0xFF9893A5)),
    line = ColorProvider(Color(0xFFDFDAD9)),
    accent = ColorProvider(Color(0xFFEA9D34)),
    rain = ColorProvider(Color(0xFF56949F)),
)
