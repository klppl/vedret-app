package se.vedret.app.data

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "vedret_prefs")

class Prefs(private val context: Context) {
    private object Keys {
        val LAST_QUERY = stringPreferencesKey("last_query")
        val LAST_LAT = doublePreferencesKey("last_lat")
        val LAST_LON = doublePreferencesKey("last_lon")
        val THEME = stringPreferencesKey("theme") // System | Light | Dark
        val LOCATION_MODE = stringPreferencesKey("location_mode")
        val AUTO_REFRESH_MINUTES = intPreferencesKey("auto_refresh_minutes")
    }

    val lastQuery: Flow<String?> = context.dataStore.data.map { it[Keys.LAST_QUERY] }
    val lastCoords: Flow<Pair<Double, Double>?> = context.dataStore.data.map {
        val lat = it[Keys.LAST_LAT]; val lon = it[Keys.LAST_LON]
        if (lat != null && lon != null) lat to lon else null
    }
    val theme: Flow<ThemeMode> = context.dataStore.data.map { ThemeMode.parse(it[Keys.THEME]) }
    val locationMode: Flow<LocationMode> = context.dataStore.data.map {
        LocationMode.parse(it[Keys.LOCATION_MODE])
    }
    val autoRefreshMinutes: Flow<Int> = context.dataStore.data.map {
        it[Keys.AUTO_REFRESH_MINUTES] ?: 0
    }

    suspend fun setLastQuery(value: String) {
        context.dataStore.edit { it[Keys.LAST_QUERY] = value }
    }

    suspend fun setLastCoords(lat: Double, lon: Double) {
        context.dataStore.edit { it[Keys.LAST_LAT] = lat; it[Keys.LAST_LON] = lon }
    }

    suspend fun setTheme(value: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME] = value.name }
    }

    suspend fun setLocationMode(value: LocationMode) {
        context.dataStore.edit { it[Keys.LOCATION_MODE] = value.name }
    }

    suspend fun setAutoRefreshMinutes(value: Int) {
        context.dataStore.edit { it[Keys.AUTO_REFRESH_MINUTES] = value }
    }

    suspend fun snapshot(): Snapshot {
        val data = context.dataStore.data.first()
        val lat = data[Keys.LAST_LAT]; val lon = data[Keys.LAST_LON]
        return Snapshot(
            query = data[Keys.LAST_QUERY],
            coords = if (lat != null && lon != null) lat to lon else null,
        )
    }

    data class Snapshot(val query: String?, val coords: Pair<Double, Double>?)
}
