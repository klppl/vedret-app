package se.vedret.app.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import se.vedret.app.MainActivity
import se.vedret.app.data.Condition
import se.vedret.app.data.Consensus
import se.vedret.app.data.ThemeMode
import kotlin.math.roundToInt

class HeroWidget : GlanceAppWidget() {
    // Exact mode lets the composable read LocalSize.current so we can scale
    // the big number with whatever the launcher actually allocates.
    override val sizeMode: SizeMode = SizeMode.Exact
    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        WidgetUpdater.seedState(context, id)
        provideContent { HeroContent() }
    }
}

class HeroWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = HeroWidget()

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        RefreshScheduler.forceRefresh(context)
    }
}

@Composable
internal fun HeroContent() {
    val state = currentState<Preferences>()
    val theme = ThemeMode.parse(state[WidgetStateKeys.Theme])
    val palette = paletteFor(theme)
    val data = WidgetData.parse(state[WidgetStateKeys.DataJson])
    val context = LocalContext.current
    val size = LocalSize.current

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(palette.surface)
            .cornerRadius(20.dp)
            .clickable(actionStartActivity(Intent(context, MainActivity::class.java)))
            .padding(12.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (data == null) {
            Text(
                text = "…",
                style = TextStyle(color = palette.muted, fontSize = 16.sp),
            )
        } else {
            HeroBlock(data, palette, scaleFor(size.width.value))
        }
    }
}

internal data class HeroScale(
    val numberSp: TextUnit,
    val descSp: TextUnit,
    val factSp: TextUnit,
)

internal fun scaleFor(widthDp: Float): HeroScale = when {
    widthDp >= 320f -> HeroScale(64.sp, 16.sp, 13.sp)
    widthDp >= 240f -> HeroScale(54.sp, 15.sp, 12.sp)
    else -> HeroScale(44.sp, 13.sp, 11.sp)
}

@Composable
internal fun HeroBlock(data: Consensus, palette: WidgetPalette, scale: HeroScale) {
    val current = data.current ?: return
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "${current.temperature.roundToInt()}",
            style = TextStyle(
                color = palette.accent,
                fontSize = scale.numberSp,
                fontWeight = FontWeight.Normal,
            ),
        )
        Spacer(GlanceModifier.width(12.dp))
        Column(modifier = GlanceModifier.defaultWeight()) {
            Text(
                text = "grader och ${Condition.displayFor(current.condition)} ${Condition.emojiFor(current.condition)}",
                style = TextStyle(color = palette.ink, fontSize = scale.descSp),
            )
            Spacer(GlanceModifier.height(2.dp))
            Text(
                text = "${current.windSpeed.roundToInt()} m/s vind · ${current.rainProbability}% risk för regn",
                style = TextStyle(color = palette.muted, fontSize = scale.factSp),
            )
        }
    }
}
