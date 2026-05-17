package se.vedret.app.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
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
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import se.vedret.app.MainActivity
import se.vedret.app.data.Condition
import se.vedret.app.data.Consensus
import se.vedret.app.data.Slot
import se.vedret.app.data.ThemeMode
import kotlin.math.roundToInt

class HeroUpcomingWidget : GlanceAppWidget() {
    // Responsive (not Exact) so Glance hands the launcher a SizedRemoteViews
    // set with one pre-rendered tier per declared DpSize. SizeMode.Exact
    // relies on AppWidgetManager.OPTION_APPWIDGET_* keys being populated when
    // the composable runs; One UI tears down and rebuilds widget hosts after
    // the user returns to the launcher, and during that window those keys
    // sometimes only contain the metadata defaults, which made the
    // Exact-mode composition fall through to the smallest tier and clip the
    // upcoming row. With Responsive, the launcher picks the tier from the
    // pre-rendered set, so this is no longer dependent on the options Bundle.
    override val sizeMode: SizeMode = SizeMode.Responsive(SUPPORTED_SIZES)
    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        WidgetUpdater.seedState(context, id)
        provideContent { HeroUpcomingContent() }
    }

    companion object {
        internal val SMALL = DpSize(250.dp, 130.dp)
        internal val MEDIUM = DpSize(280.dp, 160.dp)
        internal val LARGE = DpSize(320.dp, 200.dp)
        internal val SUPPORTED_SIZES = setOf(SMALL, MEDIUM, LARGE)
    }
}

class HeroUpcomingWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = HeroUpcomingWidget()

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
private fun HeroUpcomingContent() {
    val state = currentState<Preferences>()
    val theme = ThemeMode.parse(state[WidgetStateKeys.Theme])
    val palette = paletteFor(theme)
    val data = WidgetData.parse(state[WidgetStateKeys.DataJson])
    val context = LocalContext.current
    val size = LocalSize.current
    val scale = upcomingScaleFor(size)

    // fillMaxSize paints the rounded card across the whole launcher cell so
    // there's no transparent gap; the launcher picks one of the declared
    // tiers (small/medium/large) so the hero + upcoming row always fit
    // inside that cell.
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(palette.surface)
            .cornerRadius(20.dp)
            .clickable(actionStartActivity(Intent(context, MainActivity::class.java)))
            .padding(scale.padDp),
    ) {
        if (data == null) {
            Box(modifier = GlanceModifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("…", style = TextStyle(color = palette.muted, fontSize = 16.sp))
            }
            return@Column
        }
        HeroBlock(data, palette, scale.heroScale)
        Spacer(GlanceModifier.height(scale.spacerDp))
        if (data.upcoming.isNotEmpty()) {
            UpcomingRow(data.upcoming.take(5), palette, scale.heroScale)
        }
    }
}

internal data class UpcomingScale(
    val heroScale: HeroScale,
    val padDp: Dp,
    val spacerDp: Dp,
)

// With SizeMode.Responsive, Glance composes once per DpSize in SUPPORTED_SIZES,
// so `size` here equals one of those declared tiers and the launcher chooses
// which pre-rendered RemoteViews to display. We still tolerate sizes that
// don't exactly match (some launchers report slightly off dimensions) by
// falling through to the smallest tier.
internal fun upcomingScaleFor(size: DpSize): UpcomingScale = when {
    size.width >= HeroUpcomingWidget.LARGE.width && size.height >= HeroUpcomingWidget.LARGE.height ->
        UpcomingScale(HeroScale(64.sp, 16.sp, 13.sp), 16.dp, 16.dp)
    size.width >= HeroUpcomingWidget.MEDIUM.width && size.height >= HeroUpcomingWidget.MEDIUM.height ->
        UpcomingScale(HeroScale(54.sp, 15.sp, 12.sp), 12.dp, 10.dp)
    else ->
        UpcomingScale(HeroScale(44.sp, 13.sp, 11.sp), 8.dp, 6.dp)
}

@Composable
private fun UpcomingRow(slots: List<Slot>, palette: WidgetPalette, scale: HeroScale) {
    Row(modifier = GlanceModifier.fillMaxWidth()) {
        slots.forEach { slot ->
            Column(
                modifier = GlanceModifier.defaultWeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = timeLabel(slot.time),
                    style = TextStyle(color = palette.muted, fontSize = scale.factSp),
                )
                Spacer(GlanceModifier.height(2.dp))
                Text(
                    text = Condition.emojiFor(slot.condition),
                    style = TextStyle(fontSize = scale.descSp.value.coerceAtLeast(16f).sp),
                )
                Spacer(GlanceModifier.height(2.dp))
                Text(
                    text = "${slot.temperature.roundToInt()}°",
                    style = TextStyle(
                        color = palette.ink,
                        fontSize = scale.descSp,
                        fontWeight = FontWeight.Medium,
                    ),
                )
            }
        }
    }
}

private fun timeLabel(iso: String): String {
    val t = iso.indexOf('T').takeIf { it >= 0 } ?: return iso
    val rest = iso.substring(t + 1)
    val end = rest.indexOfFirst { it == '+' || it == '-' || it == 'Z' }
        .takeIf { it > 0 } ?: rest.length
    return rest.substring(0, end).take(5)
}
