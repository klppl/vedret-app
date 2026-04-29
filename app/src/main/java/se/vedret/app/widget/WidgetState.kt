package se.vedret.app.widget

import androidx.datastore.preferences.core.stringPreferencesKey

/**
 * Keys for the Glance-managed Preferences state. The widget composables read
 * these via `currentState<Preferences>()`; WidgetUpdater writes them. Glance
 * observes state changes and recomposes — `update(context, id)` alone does
 * NOT re-run provideGlance for an active session, which is why the data
 * the composables need has to live in this state, not in provideGlance locals.
 */
internal object WidgetStateKeys {
    val Theme = stringPreferencesKey("theme")
    val DataJson = stringPreferencesKey("data_json")
}
