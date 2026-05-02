package se.vedret.app.system

import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.provider.Settings

/**
 * Helps the user opt the app out of Android's battery optimization, which
 * otherwise throttles or kills WorkManager periodic jobs (the widget refresh)
 * on aggressive vendors like Samsung One UI.
 *
 * Reading the current state via PowerManager.isIgnoringBatteryOptimizations
 * needs no permission. We deliberately don't use ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
 * — that intent is the direct allow/deny dialog but it requires the
 * REQUEST_IGNORE_BATTERY_OPTIMIZATIONS permission, which Google Play
 * restricts to apps with a justified core-functionality use case. Sending
 * the user to the system list is one extra tap but works without the
 * restricted permission.
 */
object BatteryOptimization {

    fun isIgnoring(context: Context): Boolean {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return pm.isIgnoringBatteryOptimizations(context.packageName)
    }

    /** Opens the system "Battery optimization" list. The user finds vedret and toggles it. */
    fun openSettings(context: Context) {
        val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
