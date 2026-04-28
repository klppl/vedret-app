package se.vedret.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Drop Inter into res/font/ later; FontFamily.Default keeps things working until then.
val VedretFontFamily = FontFamily.Default

private val tnum = TextStyle(fontFamily = VedretFontFamily).copy(
    platformStyle = PlatformTextStyle(includeFontPadding = false)
)

val VedretTypography = Typography(
    displayLarge = tnum.copy(fontSize = 80.sp, fontWeight = FontWeight.ExtraLight),
    displayMedium = tnum.copy(fontSize = 56.sp, fontWeight = FontWeight.Light),
    headlineMedium = tnum.copy(fontSize = 28.sp, fontWeight = FontWeight.Normal),
    titleLarge = tnum.copy(fontSize = 22.sp, fontWeight = FontWeight.Medium),
    bodyLarge = tnum.copy(fontSize = 16.sp),
    bodyMedium = tnum.copy(fontSize = 14.sp),
    bodySmall = tnum.copy(fontSize = 12.sp),
    labelMedium = tnum.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium),
)
