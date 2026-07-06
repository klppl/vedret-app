package se.vedret.app.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.CancellationSignal
import android.os.SystemClock
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

// Framework LocationManager instead of Play Services fused location — keeps
// the app free of proprietary dependencies (F-Droid) and working on
// de-Googled devices. City-level accuracy is all weather needs.
class LocationProvider(private val context: Context) {
    private val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    @SuppressLint("MissingPermission")
    suspend fun current(): Pair<Double, Double>? = withTimeoutOrNull(10_000) {
        val providers = candidates().filter { manager.isProviderEnabled(it) }
        if (providers.isEmpty()) return@withTimeoutOrNull null

        // A recent cached fix is plenty for city-level weather — skip the
        // radio wakeup entirely when one exists.
        providers.mapNotNull { manager.getLastKnownLocation(it) }
            .maxByOrNull { it.elapsedRealtimeNanos }
            ?.takeIf { SystemClock.elapsedRealtimeNanos() - it.elapsedRealtimeNanos < FRESH_NANOS }
            ?.let { return@withTimeoutOrNull it.latitude to it.longitude }

        suspendCancellableCoroutine { cont ->
            if (Build.VERSION.SDK_INT >= 30) {
                val signal = CancellationSignal()
                cont.invokeOnCancellation { signal.cancel() }
                manager.getCurrentLocation(providers.first(), signal, context.mainExecutor) { loc ->
                    cont.resume(loc?.let { it.latitude to it.longitude })
                }
            } else {
                val listener = LocationListener { loc -> cont.resume(loc.latitude to loc.longitude) }
                cont.invokeOnCancellation { manager.removeUpdates(listener) }
                @Suppress("DEPRECATION")
                manager.requestSingleUpdate(providers.first(), listener, context.mainLooper)
            }
        }
    }

    // The app holds only ACCESS_COARSE_LOCATION: below API 31 that limits us
    // to the network provider (GPS throws SecurityException); from 31 the
    // platform fused provider accepts coarse and degrades gracefully on
    // devices without Play Services.
    private fun candidates(): List<String> =
        if (Build.VERSION.SDK_INT >= 31) {
            listOf(LocationManager.FUSED_PROVIDER, LocationManager.NETWORK_PROVIDER)
        } else {
            listOf(LocationManager.NETWORK_PROVIDER, LocationManager.PASSIVE_PROVIDER)
        }

    private companion object {
        const val FRESH_NANOS = 10L * 60 * 1_000_000_000 // 10 min
    }
}
