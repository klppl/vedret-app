package se.vedret.app.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Consensus(
    val city: String = "",
    val location: GeoLocation? = null,
    val current: Slot? = null,
    val providers: List<ProviderRow> = emptyList(),
    val upcoming: List<Slot> = emptyList(),
    @SerialName("today_day") val todayDay: DayForecast? = null,
    val days: List<DayForecast> = emptyList(),
    val sunrise: String? = null,
    val sunset: String? = null,
    @SerialName("generated_at") val generatedAt: String? = null,
    val error: String = "",
)

@Serializable
data class GeoLocation(
    val name: String,
    val lat: Double,
    val lon: Double,
)

@Serializable
data class Slot(
    val time: String,
    val temperature: Double,
    val condition: String,
    @SerialName("wind_speed") val windSpeed: Double = 0.0,
    @SerialName("rain_probability") val rainProbability: Int = 0,
    @SerialName("uv_index") val uvIndex: Double = 0.0,
)

@Serializable
data class DayForecast(
    val date: String,
    val label: String,
    val high: Double,
    val low: Double,
    val condition: String = "",
    @SerialName("wind_speed") val windSpeed: Double = 0.0,
    @SerialName("rain_probability") val rainProbability: Int = 0,
    @SerialName("uv_index") val uvIndex: Double = 0.0,
    val slots: List<Slot>? = null,
)

@Serializable
data class ProviderRow(
    val provider: String,
    val temperature: Double,
)
