package se.vedret.app.ui.theme

import androidx.compose.ui.graphics.Color

// Sourced from main.go:644-709 (`:root` and `[data-theme="dark"]` CSS variables).
object VedretLight {
    val Bg = Color(0xFFF5F6F8)
    val Surface = Color(0xFFFFFFFF)
    val Ink = Color(0xFF1A1D23)
    val Muted = Color(0xFF5A6068)
    val Faint = Color(0xFF8A8F96)
    val Line = Color(0xFFE3E5E9)
    val Accent = Color(0xFFE89611)
    val Rain = Color(0xFF3A86FF)
}

object VedretDark {
    val Bg = Color(0xFF0F1115)
    val Surface = Color(0xFF181B22)
    val Ink = Color(0xFFE6E6E6)
    val Muted = Color(0xFF9AA0A8)
    val Faint = Color(0xFF6E747C)
    val Line = Color(0xFF262A32)
    val Accent = Color(0xFFF5A623)
    val Rain = Color(0xFF5AA0FF)
}
