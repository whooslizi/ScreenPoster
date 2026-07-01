package dev.whooslizi.screenposter.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dev.whooslizi.screenposter.worker.WallpaperWorker

/**
 * Receives exact alarm broadcasts and delegates the work to an Expedited WorkManager Request.
 */
class WallpaperAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Delegate to Expedited WorkManager request to handle the wallpaper change.
        // Expedited work requests bypass Doze and most OEM restrictions.
        WallpaperWorker.enqueueExpedited(context)
    }
}
