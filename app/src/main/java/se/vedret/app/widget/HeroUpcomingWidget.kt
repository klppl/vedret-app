package se.vedret.app.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import se.vedret.app.MainActivity
import se.vedret.app.data.Condition
import se.vedret.app.data.Consensus
import se.vedret.app.data.Slot
import kotlin.math.roundToInt

class HeroUpcomingWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Single

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val data = WidgetData.loadCached(context)
        provideContent { HeroUpcomingContent(data) }
    }
}

class HeroUpcomingWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = HeroUpcomingWidget()
}

@Composable
private fun HeroUpcomingContent(data: Consensus?) {
    val context = LocalContext.current
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(WidgetColors.surface)
            .cornerRadius(20.dp)
            .clickable(actionStartActivity(Intent(context, MainActivity::class.java)))
            .padding(16.dp),
    ) {
        if (data == null) {
            Box(modifier = GlanceModifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("…", style = TextStyle(color = WidgetColors.muted, fontSize = 16.sp))
            }
            return@Column
        }
        HeroBlock(data)
        if (data.upcoming.isNotEmpty()) {
            Spacer(GlanceModifier.height(12.dp))
            UpcomingRow(data.upcoming.take(5))
        }
    }
}

@Composable
private fun UpcomingRow(slots: List<Slot>) {
    Row(modifier = GlanceModifier.fillMaxWidth()) {
        slots.forEach { slot ->
            Column(
                modifier = GlanceModifier.defaultWeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = timeLabel(slot.time),
                    style = TextStyle(color = WidgetColors.muted, fontSize = 11.sp),
                )
                Spacer(GlanceModifier.height(2.dp))
                Text(
                    text = Condition.emojiFor(slot.condition),
                    style = TextStyle(fontSize = 18.sp),
                )
                Spacer(GlanceModifier.height(2.dp))
                Text(
                    text = "${slot.temperature.roundToInt()}°",
                    style = TextStyle(
                        color = WidgetColors.ink,
                        fontSize = 14.sp,
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
