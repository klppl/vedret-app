package se.vedret.app.widget

import android.content.Context
import androidx.glance.appwidget.updateAll

internal object WidgetUpdater {
    suspend fun refreshAll(context: Context) {
        runCatching { HeroWidget().updateAll(context) }
        runCatching { HeroUpcomingWidget().updateAll(context) }
    }
}
