package se.vedret.app.widget

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first
import se.vedret.app.data.LocationMode
import se.vedret.app.data.Prefs
import se.vedret.app.data.WeatherRepository

/**
 * Background fetch + widget update. WorkManager runs this on the app's behalf
 * even when the activity has been killed. We deliberately don't request live
 * GPS here — background location requires extra permission and we'd just get
 * delayed by location lookups. Last-known coords/city are good enough; user
 * gets a fresh GPS fix the next time they open the app or tap the crosshair.
 *
 * Persisting to last_response.json triggers WidgetUpdater.refreshAll via
 * WeatherRepository.persist, so the widgets receive new data immediately.
 */
class RefreshWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "doWork start (run #${runAttemptCount})")
        return try {
            val ctx = applicationContext
            val prefs = Prefs(ctx)
            val repo = WeatherRepository(ctx)
            val snap = prefs.snapshot()
            prefs.locationMode.first()
            when {
                snap.coords != null -> {
                    Log.d(TAG, "fetching by coords (${snap.coords.first}, ${snap.coords.second})")
                    repo.byCoords(snap.coords.first, snap.coords.second)
                }
                snap.query != null -> {
                    Log.d(TAG, "fetching by city '${snap.query}'")
                    repo.byCity(snap.query)
                }
                else -> {
                    Log.d(TAG, "fetching by IP (no last-known location)")
                    repo.byIp()
                }
            }
            Log.d(TAG, "doWork success")
            Result.success()
        } catch (t: Throwable) {
            Log.w(TAG, "doWork failed; will retry", t)
            Result.retry()
        }
    }

    companion object {
        const val NAME = "vedret-refresh"
        private const val TAG = "RefreshWorker"
    }
}
