package se.vedret.app.widget

import android.content.Context
import android.util.Log
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import kotlinx.coroutines.flow.first
import se.vedret.app.data.Consensus
import se.vedret.app.data.Prefs

/**
 * Push the latest theme + cached weather into each widget's Glance state and
 * trigger a recomposition. Writing to the state is what makes Glance actually
 * re-render — calling `update(context, id)` alone reuses the live session
 * and skips the composable's external reads.
 */
internal object WidgetUpdater {
    private const val TAG = "WidgetUpdater"

    suspend fun refreshAll(context: Context) {
        val app = context.applicationContext
        val theme = Prefs(app).theme.first().name
        val dataJson = WidgetData.loadCached(app)?.let { encode(it) }
        val manager = GlanceAppWidgetManager(app)
        seedAndUpdate(app, manager, HeroWidget(), theme, dataJson, "HeroWidget")
        seedAndUpdate(app, manager, HeroUpcomingWidget(), theme, dataJson, "HeroUpcomingWidget")
    }

    /**
     * Seed a single widget instance's state from prefs + cache. Called from
     * `provideGlance` on session start so a freshly placed widget renders
     * immediately instead of stalling on "…" until the next refresh.
     */
    suspend fun seedState(context: Context, id: GlanceId) {
        val app = context.applicationContext
        val theme = Prefs(app).theme.first().name
        val dataJson = WidgetData.loadCached(app)?.let { encode(it) }
        updateAppWidgetState(app, PreferencesGlanceStateDefinition, id) { prefs ->
            prefs.toMutablePreferences().apply {
                this[WidgetStateKeys.Theme] = theme
                if (dataJson != null) this[WidgetStateKeys.DataJson] = dataJson
            }
        }
    }

    private suspend fun seedAndUpdate(
        context: Context,
        manager: GlanceAppWidgetManager,
        widget: GlanceAppWidget,
        theme: String,
        dataJson: String?,
        name: String,
    ) {
        try {
            val ids = manager.getGlanceIds(widget::class.java)
            Log.d(TAG, "$name: refreshing ${ids.size} placed instance(s)")
            for (id in ids) {
                updateAppWidgetState(context, PreferencesGlanceStateDefinition, id) { prefs ->
                    prefs.toMutablePreferences().apply {
                        this[WidgetStateKeys.Theme] = theme
                        if (dataJson != null) this[WidgetStateKeys.DataJson] = dataJson
                    }
                }
                widget.update(context, id)
            }
        } catch (t: Throwable) {
            Log.w(TAG, "$name refresh failed", t)
        }
    }

    private fun encode(c: Consensus): String =
        WidgetData.json.encodeToString(Consensus.serializer(), c)
}
