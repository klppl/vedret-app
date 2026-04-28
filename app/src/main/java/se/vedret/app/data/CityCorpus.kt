package se.vedret.app.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import java.io.File
import java.text.Normalizer

class CityCorpus(
    context: Context,
    private val api: VedretApi = VedretApi(),
) {
    private val cacheFile = File(context.filesDir, "orter.json")
    private val json = Json { ignoreUnknownKeys = true }
    private val mutex = Mutex()
    private val serializer = ListSerializer(String.serializer())

    @Volatile private var cached: List<String>? = null

    suspend fun ensureLoaded(): List<String> = mutex.withLock {
        cached?.let { return it }
        val fromDisk = withContext(Dispatchers.IO) {
            runCatching {
                if (cacheFile.exists()) json.decodeFromString(serializer, cacheFile.readText()) else null
            }.getOrNull()
        }
        if (fromDisk != null) {
            cached = fromDisk
            return fromDisk
        }
        val fetched = runCatching { api.getCities() }.getOrDefault(emptyList())
        if (fetched.isNotEmpty()) {
            cached = fetched
            withContext(Dispatchers.IO) {
                runCatching { cacheFile.writeText(json.encodeToString(serializer, fetched)) }
            }
        }
        fetched
    }

    fun filter(query: String, max: Int = 8): List<String> {
        val list = cached ?: return emptyList()
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return emptyList()
        val needle = fold(trimmed)
        // Substring match on accent-folded form. Corpus is sorted by population
        // descending, so first hits are the largest cities (per main.go:806-938).
        return list.asSequence()
            .filter { fold(it).contains(needle) }
            .take(max)
            .toList()
    }

    private fun fold(s: String): String {
        val nfd = Normalizer.normalize(s, Normalizer.Form.NFD)
        return DIACRITIC.replace(nfd, "").lowercase()
    }

    companion object {
        private val DIACRITIC = Regex("\\p{InCombiningDiacriticalMarks}+")
    }
}
