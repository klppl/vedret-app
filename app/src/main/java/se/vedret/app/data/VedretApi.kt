package se.vedret.app.data

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import se.vedret.app.BuildConfig

class VedretApi(
    private val baseUrl: String = BuildConfig.API_BASE,
) {
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        coerceInputValues = true
    }

    private val client = HttpClient(Android) {
        install(ContentNegotiation) { json(json) }
        install(HttpCache)
        install(HttpTimeout) {
            requestTimeoutMillis = 15_000
            connectTimeoutMillis = 10_000
            socketTimeoutMillis = 15_000
        }
        defaultRequest {
            url(baseUrl)
            headers.append("User-Agent", "vedret-android/0.1")
        }
    }

    suspend fun getByCity(name: String): Consensus = fetch {
        parameter("ort", name)
    }

    suspend fun getByCoords(lat: Double, lon: Double): Consensus = fetch {
        parameter("lat", lat)
        parameter("lon", lon)
    }

    suspend fun getByIp(): Consensus = fetch {
        parameter("geo", "ip")
    }

    suspend fun getCities(): List<String> {
        val text = client.get("orter.json").bodyAsText()
        return json.decodeFromString(text)
    }

    private suspend fun fetch(block: io.ktor.client.request.HttpRequestBuilder.() -> Unit): Consensus {
        return client.get(baseUrl) {
            parameter("format", "json")
            block()
        }.let { resp ->
            json.decodeFromString(resp.bodyAsText())
        }
    }

    fun close() = client.close()
}
