package se.vedret.app.widget

import androidx.compose.ui.graphics.Color
import androidx.glance.unit.ColorProvider

internal object WidgetColors {
    private val Surface = Color(0xFF181B22)
    private val Ink = Color(0xFFE6E6E6)
    private val Muted = Color(0xFF9AA0A8)
    private val Faint = Color(0xFF6E747C)
    private val Line = Color(0xFF262A32)
    private val Accent = Color(0xFFF5A623)
    private val Rain = Color(0xFF5AA0FF)

    val surface = ColorProvider(Surface)
    val ink = ColorProvider(Ink)
    val muted = ColorProvider(Muted)
    val faint = ColorProvider(Faint)
    val line = ColorProvider(Line)
    val accent = ColorProvider(Accent)
    val rain = ColorProvider(Rain)
}
