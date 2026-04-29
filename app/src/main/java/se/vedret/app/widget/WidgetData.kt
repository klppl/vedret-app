package se.vedret.app.widget

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import se.vedret.app.data.Consensus
import java.io.File

internal object WidgetData {
    val json = Json { ignoreUnknownKeys = true; explicitNulls = false }

    suspend fun loadCached(context: Context): Consensus? = withContext(Dispatchers.IO) {
        runCatching {
            val file = File(context.filesDir, "last_response.json")
            if (file.exists()) json.decodeFromString<Consensus>(file.readText()) else null
        }.getOrNull()
    }

    fun parse(text: String?): Consensus? {
        if (text.isNullOrBlank()) return null
        return runCatching { json.decodeFromString<Consensus>(text) }.getOrNull()
    }
}
