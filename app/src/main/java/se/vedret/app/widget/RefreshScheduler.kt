package se.vedret.app.widget

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object RefreshScheduler {
    private const val TAG = "RefreshScheduler"
    private const val ONCE_NAME = "vedret-refresh-once"

    fun apply(context: Context, minutes: Int) {
        val wm = WorkManager.getInstance(context.applicationContext)
        if (minutes <= 0) {
            Log.d(TAG, "auto-refresh OFF, cancelling")
            wm.cancelUniqueWork(RefreshWorker.NAME)
            return
        }
        Log.d(TAG, "scheduling: every $minutes min")
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // Periodic schedule for ongoing refreshes. WorkManager's minimum
        // periodic interval is 15 minutes; our smallest setting matches.
        val periodic = PeriodicWorkRequestBuilder<RefreshWorker>(
            minutes.toLong(), TimeUnit.MINUTES,
        )
            .setConstraints(constraints)
            .build()
        wm.enqueueUniquePeriodicWork(
            RefreshWorker.NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            periodic,
        )

        // WorkManager periodic work fires anywhere in the first interval
        // window. Kick off a one-time run alongside so the user sees fresh
        // data quickly after enabling/changing the interval. KEEP means we
        // don't spam fresh fetches on every app launch — only the first.
        val once = OneTimeWorkRequestBuilder<RefreshWorker>()
            .setConstraints(constraints)
            .build()
        wm.enqueueUniqueWork(ONCE_NAME, ExistingWorkPolicy.KEEP, once)
    }

    fun forceRefresh(context: Context) {
        val wm = WorkManager.getInstance(context.applicationContext)
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val once = OneTimeWorkRequestBuilder<RefreshWorker>()
            .setConstraints(constraints)
            .build()
        wm.enqueueUniqueWork("vedret-refresh-force", ExistingWorkPolicy.REPLACE, once)
    }
}
