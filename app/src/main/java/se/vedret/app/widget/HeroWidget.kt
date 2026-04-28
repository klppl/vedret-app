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
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import se.vedret.app.MainActivity
import se.vedret.app.data.Condition
import se.vedret.app.data.Consensus
import kotlin.math.roundToInt

class HeroWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Single

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val data = WidgetData.loadCached(context)
        provideContent { HeroContent(data) }
    }
}

class HeroWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = HeroWidget()
}

@Composable
internal fun HeroContent(data: Consensus?) {
    val context = LocalContext.current
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(WidgetColors.surface)
            .cornerRadius(20.dp)
            .clickable(actionStartActivity(Intent(context, MainActivity::class.java)))
            .padding(12.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        if (data == null) {
            Text(
                text = "…",
                style = TextStyle(color = WidgetColors.muted, fontSize = 16.sp),
            )
        } else {
            HeroBlock(data)
        }
    }
}

@Composable
internal fun HeroBlock(data: Consensus) {
    val current = data.current ?: return
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "${current.temperature.roundToInt()}",
            style = TextStyle(
                color = WidgetColors.accent,
                fontSize = 48.sp,
                fontWeight = FontWeight.Normal,
            ),
        )
        Spacer(GlanceModifier.width(12.dp))
        Column(modifier = GlanceModifier.defaultWeight()) {
            Text(
                text = "grader och ${Condition.displayFor(current.condition)} ${Condition.emojiFor(current.condition)}",
                style = TextStyle(color = WidgetColors.ink, fontSize = 14.sp),
            )
            Spacer(GlanceModifier.height(2.dp))
            Text(
                text = "${current.windSpeed.roundToInt()} m/s vind · ${current.rainProbability}% risk för regn",
                style = TextStyle(color = WidgetColors.muted, fontSize = 11.sp),
            )
        }
    }
}
