package se.vedret.app.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
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
import androidx.glance.layout.fillMaxWidth
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
    // Responsive mode hands the launcher pre-rendered RemoteViews for each
    // declared tier so the launcher (not the composable) picks which to
    // show. This is more reliable on launchers (notably One UI) that don't
    // populate AppWidgetManager.OPTION_APPWIDGET_* consistently after a
    // host rebuild — Exact mode would fall back to metadata defaults at
    // those moments and render the wrong scale.
    override val sizeMode: SizeMode = SizeMode.Responsive(SUPPORTED_SIZES)
    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        WidgetUpdater.seedState(context, id)
        provideContent { HeroContent() }
    }

    companion object {
        internal val SMALL = DpSize(180.dp, 50.dp)
        internal val MEDIUM = DpSize(240.dp, 60.dp)
        internal val LARGE = DpSize(320.dp, 80.dp)
        internal val SUPPORTED_SIZES = setOf(SMALL, MEDIUM, LARGE)
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
            .fillMaxWidth()
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

// With SizeMode.Responsive, the composable runs once per DpSize in
// SUPPORTED_SIZES and Glance hands the launcher a SizedRemoteViews set;
// widthDp here is therefore one of the declared tier widths. The
// >= comparisons also tolerate launchers that report slightly off sizes.
internal fun scaleFor(widthDp: Float): HeroScale = when {
    widthDp >= HeroWidget.LARGE.width.value -> HeroScale(64.sp, 16.sp, 13.sp)
    widthDp >= HeroWidget.MEDIUM.width.value -> HeroScale(54.sp, 15.sp, 12.sp)
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
