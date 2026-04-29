package se.vedret.app.widget

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object RefreshScheduler {
    fun apply(context: Context, minutes: Int) {
        val wm = WorkManager.getInstance(context.applicationContext)
        if (minutes <= 0) {
            wm.cancelUniqueWork(RefreshWorker.NAME)
            return
        }
        // WorkManager's periodic minimum is 15 minutes; our smallest setting is 15.
        val request = PeriodicWorkRequestBuilder<RefreshWorker>(
            minutes.toLong(), TimeUnit.MINUTES,
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build(),
            )
            .build()
        wm.enqueueUniquePeriodicWork(
            RefreshWorker.NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }
}
