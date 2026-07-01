package dev.whooslizi.screenposter.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import dagger.hilt.android.AndroidEntryPoint
import dev.whooslizi.screenposter.data.repository.WallpaperRepository
import dev.whooslizi.screenposter.util.AlarmScheduler
import dev.whooslizi.screenposter.util.WallpaperHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Receives alarm broadcasts and performs the wallpaper change directly
 * using goAsync() + WakeLock. 
 * 
 * Why NOT start a ForegroundService here:
 * - Vivo OriginOS and other Chinese OEMs block startForegroundService() from
 *   BroadcastReceivers when the app process is killed
 * - goAsync() gives us ~30 seconds to complete work, which is plenty for
 *   reading a bitmap and setting it as wallpaper
 * - WakeLock ensures the CPU stays awake during the operation
 */
@AndroidEntryPoint
class WallpaperAlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repository: WallpaperRepository

    @Inject
    lateinit var wallpaperHelper: WallpaperHelper

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()

        // Acquire a WakeLock to keep CPU alive during wallpaper change
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "ScreenPoster:WallpaperChange"
        ).apply {
            acquire(30_000L) // 30 second timeout
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val settings = repository.getSettings()

                if (settings.intervalMinutes > 0) {
                    val nextWallpaper = repository.getNextWallpaper()
                    if (nextWallpaper != null) {
                        val uriStr = nextWallpaper.editedUri ?: nextWallpaper.uri
                        wallpaperHelper.setWallpaper(
                            uriStr,
                            WallpaperHelper.TARGET_BOTH,
                            settings.homeScreenBlurPercent
                        )
                        repository.addHistory(nextWallpaper.id)
                    }

                    // Reschedule the next alarm
                    AlarmScheduler.scheduleNextAlarm(context, settings.intervalMinutes)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Still try to reschedule even if this round failed
                try {
                    val settings = repository.getSettings()
                    if (settings.intervalMinutes > 0) {
                        AlarmScheduler.scheduleNextAlarm(context, settings.intervalMinutes)
                    }
                } catch (_: Exception) {}
            } finally {
                try {
                    if (wakeLock.isHeld) wakeLock.release()
                } catch (_: Exception) {}
                pendingResult.finish()
            }
        }
    }
}
