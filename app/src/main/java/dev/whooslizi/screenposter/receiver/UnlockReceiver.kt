package dev.whooslizi.screenposter.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dev.whooslizi.screenposter.data.repository.WallpaperRepository
import dev.whooslizi.screenposter.util.AlarmScheduler
import dev.whooslizi.screenposter.util.WallpaperHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class UnlockReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repository: WallpaperRepository
    
    @Inject
    lateinit var wallpaperHelper: WallpaperHelper

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_USER_PRESENT -> {
                // Change wallpaper on every unlock if configured
                CoroutineScope(Dispatchers.IO).launch {
                    val settings = repository.getSettings()
                    // -1 implies Every Unlock
                    if (settings.intervalMinutes == -1) {
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
                    }
                }
            }
            Intent.ACTION_BOOT_COMPLETED -> {
                // Reschedule alarm after device reboot
                CoroutineScope(Dispatchers.IO).launch {
                    val settings = repository.getSettings()
                    when {
                        settings.intervalMinutes > 0 -> {
                            // Re-schedule the exact alarm for timed wallpaper changes
                            AlarmScheduler.scheduleNextAlarm(context, settings.intervalMinutes)
                        }
                        settings.intervalMinutes == -2 -> {
                            // "On Device Boot" mode: change wallpaper now
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
                        }
                    }
                }
            }
        }
    }
}
