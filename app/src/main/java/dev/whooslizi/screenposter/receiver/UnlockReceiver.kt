package dev.whooslizi.screenposter.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dev.whooslizi.screenposter.data.repository.WallpaperRepository
import dev.whooslizi.screenposter.util.AlarmScheduler
import dev.whooslizi.screenposter.worker.WallpaperWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class UnlockReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repository: WallpaperRepository

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_USER_PRESENT -> {
                CoroutineScope(Dispatchers.IO).launch {
                    val settings = repository.getSettings()
                    
                    if (settings.intervalMinutes == -1) {
                        // "Every Unlock" mode
                        WallpaperWorker.enqueueExpedited(context)
                    } else if (settings.intervalMinutes > 0) {
                        // "Timed" mode — check if we missed the alarm due to Doze/OEM killing
                        val elapsed = System.currentTimeMillis() - settings.lastChangeTimeMillis
                        if (elapsed >= settings.intervalMinutes * 60_000L) {
                            // It's past due! Change it now upon waking the device.
                            WallpaperWorker.enqueueExpedited(context)
                        }
                    }
                }
            }
            Intent.ACTION_BOOT_COMPLETED -> {
                CoroutineScope(Dispatchers.IO).launch {
                    val settings = repository.getSettings()
                    if (settings.intervalMinutes > 0) {
                        // Reschedule the exact alarm after reboot
                        AlarmScheduler.scheduleNextAlarm(context, settings.intervalMinutes)
                    } else if (settings.intervalMinutes == -2) {
                        // "On Device Boot" mode
                        WallpaperWorker.enqueueExpedited(context)
                    }
                }
            }
        }
    }
}
