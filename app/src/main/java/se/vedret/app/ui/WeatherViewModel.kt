package se.vedret.app.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import se.vedret.app.data.CityCorpus
import se.vedret.app.data.Consensus
import se.vedret.app.data.LocationMode
import se.vedret.app.data.Prefs
import se.vedret.app.data.WeatherRepository
import se.vedret.app.location.LocationProvider

data class WeatherUiState(
    val loading: Boolean = false,
    val refreshing: Boolean = false,
    val data: Consensus? = null,
    val error: String? = null,
    val query: String = "",
    val suggestions: List<String> = emptyList(),
    val locationMode: LocationMode = LocationMode.Auto,
    val autoRefreshMinutes: Int = 0,
)

class WeatherViewModel(
    private val repo: WeatherRepository,
    private val prefs: Prefs,
    private val cities: CityCorpus,
    private val location: LocationProvider,
) : ViewModel() {

    private val _state = MutableStateFlow(WeatherUiState(loading = true))
    val state: StateFlow<WeatherUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repo.loadCached()?.let { update { copy(data = it, query = it.city) } }
            val snap = prefs.snapshot()
            when {
                snap.coords != null -> loadCoords(snap.coords.first, snap.coords.second)
                snap.query != null -> loadCity(snap.query)
                else -> loadIp()
            }
        }
        viewModelScope.launch { cities.ensureLoaded() }

        // Mirror persisted settings into UI state.
        viewModelScope.launch {
            prefs.locationMode.collectLatest { mode -> update { copy(locationMode = mode) } }
        }
        // Auto-refresh ticker: collectLatest cancels the prior loop on every interval change.
        viewModelScope.launch {
            prefs.autoRefreshMinutes.collectLatest { minutes ->
                update { copy(autoRefreshMinutes = minutes) }
                if (minutes <= 0) return@collectLatest
                val intervalMs = minutes.toLong() * 60_000L
                while (isActive) {
                    delay(intervalMs)
                    refresh()
                }
            }
        }
    }

    fun onQueryChange(text: String) {
        update { copy(query = text, suggestions = cities.filter(text)) }
    }

    fun submitCity(name: String) {
        if (name.isBlank()) return
        update { copy(suggestions = emptyList()) }
        viewModelScope.launch { loadCity(name.trim()) }
    }

    fun submitIp() {
        viewModelScope.launch { loadIp() }
    }

    fun submitCoords(lat: Double, lon: Double) {
        viewModelScope.launch { loadCoords(lat, lon) }
    }

    fun requestLocation() {
        viewModelScope.launch {
            update { copy(loading = data == null, refreshing = data != null, error = null) }
            val coords = location.current()
            if (coords != null) loadCoords(coords.first, coords.second)
            else loadIp()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            update { copy(refreshing = true) }
            try {
                val mode = state.value.locationMode
                val c: Consensus = if (mode == LocationMode.Auto) {
                    val coords = location.current()
                    when {
                        coords != null -> repo.byCoords(coords.first, coords.second)
                        else -> fallbackByLastKnown()
                    }
                } else {
                    fallbackByLastKnown()
                }
                update {
                    copy(
                        refreshing = false,
                        data = c,
                        error = c.error.takeIf { it.isNotEmpty() },
                        query = c.city.takeIf { it.isNotEmpty() } ?: query,
                    )
                }
                if (c.error.isEmpty()) {
                    c.location?.let { prefs.setLastCoords(it.lat, it.lon) }
                }
            } catch (t: Throwable) {
                update { copy(refreshing = false, error = t.message) }
            }
        }
    }

    fun setLocationMode(mode: LocationMode) {
        viewModelScope.launch { prefs.setLocationMode(mode) }
    }

    fun setAutoRefreshMinutes(minutes: Int) {
        viewModelScope.launch { prefs.setAutoRefreshMinutes(minutes) }
    }

    private suspend fun fallbackByLastKnown(): Consensus {
        val snap = prefs.snapshot()
        return when {
            snap.coords != null -> repo.byCoords(snap.coords.first, snap.coords.second)
            snap.query != null -> repo.byCity(snap.query)
            else -> repo.byIp()
        }
    }

    private suspend fun loadCity(name: String) {
        update { copy(loading = data == null, refreshing = data != null, error = null) }
        try {
            val c = repo.byCity(name)
            prefs.setLastQuery(name)
            c.location?.let { prefs.setLastCoords(it.lat, it.lon) }
            update {
                copy(
                    loading = false,
                    refreshing = false,
                    data = c,
                    error = c.error.takeIf { it.isNotEmpty() },
                    query = c.city.takeIf { it.isNotEmpty() } ?: query,
                    suggestions = emptyList(),
                )
            }
        } catch (t: Throwable) {
            update { copy(loading = false, refreshing = false, error = t.message ?: "fel") }
        }
    }

    private suspend fun loadCoords(lat: Double, lon: Double) {
        update { copy(loading = data == null, refreshing = data != null, error = null) }
        try {
            val c = repo.byCoords(lat, lon)
            prefs.setLastCoords(lat, lon)
            update {
                copy(
                    loading = false,
                    refreshing = false,
                    data = c,
                    error = c.error.takeIf { it.isNotEmpty() },
                    query = c.city.takeIf { it.isNotEmpty() } ?: query,
                    suggestions = emptyList(),
                )
            }
        } catch (t: Throwable) {
            update { copy(loading = false, refreshing = false, error = t.message ?: "fel") }
        }
    }

    private suspend fun loadIp() {
        update { copy(loading = data == null, refreshing = data != null, error = null) }
        try {
            val c = repo.byIp()
            update {
                copy(
                    loading = false,
                    refreshing = false,
                    data = c,
                    error = c.error.takeIf { it.isNotEmpty() },
                    query = c.city.takeIf { it.isNotEmpty() } ?: query,
                    suggestions = emptyList(),
                )
            }
        } catch (t: Throwable) {
            update { copy(loading = false, refreshing = false, error = t.message ?: "fel") }
        }
    }

    private inline fun update(block: WeatherUiState.() -> WeatherUiState) {
        _state.value = _state.value.block()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
                WeatherViewModel(
                    repo = WeatherRepository(app),
                    prefs = Prefs(app),
                    cities = CityCorpus(app),
                    location = LocationProvider(app),
                )
            }
        }
    }
}
