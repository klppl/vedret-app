package se.vedret.app.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import se.vedret.app.widget.WidgetUpdater
import java.io.File

class WeatherRepository(
    context: Context,
    private val api: VedretApi = VedretApi(),
) {
    private val appContext = context.applicationContext
    private val cacheFile = File(context.filesDir, "last_response.json")
    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false }

    suspend fun loadCached(): Consensus? = withContext(Dispatchers.IO) {
        runCatching {
            if (cacheFile.exists()) json.decodeFromString<Consensus>(cacheFile.readText()) else null
        }.getOrNull()
    }

    suspend fun byCity(name: String): Consensus = api.getByCity(name).also { persist(it) }
    suspend fun byCoords(lat: Double, lon: Double): Consensus = api.getByCoords(lat, lon).also { persist(it) }
    suspend fun byIp(): Consensus = api.getByIp().also { persist(it) }

    private suspend fun persist(c: Consensus) {
        if (c.error.isNotEmpty()) return
        withContext(Dispatchers.IO) {
            runCatching { cacheFile.writeText(json.encodeToString(Consensus.serializer(), c)) }
        }
        runCatching { WidgetUpdater.refreshAll(appContext) }
    }
}
