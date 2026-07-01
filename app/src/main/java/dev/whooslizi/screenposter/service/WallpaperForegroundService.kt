package dev.whooslizi.screenposter.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import dagger.hilt.android.AndroidEntryPoint
import dev.whooslizi.screenposter.data.repository.WallpaperRepository
import dev.whooslizi.screenposter.util.AlarmScheduler
import dev.whooslizi.screenposter.util.WallpaperHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Short-lived foreground service that changes the wallpaper and reschedules
 * the next alarm. Using a foreground service ensures the process stays alive
 * long enough to complete the wallpaper change on aggressive OEM ROMs.
 */
@AndroidEntryPoint
class WallpaperForegroundService : Service() {

    companion object {
        private const val CHANNEL_ID = "wallpaper_change_channel"
        private const val NOTIFICATION_ID = 2001
    }

    @Inject
    lateinit var repository: WallpaperRepository

    @Inject
    lateinit var wallpaperHelper: WallpaperHelper

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = buildNotification()
        startForeground(NOTIFICATION_ID, notification)

        serviceScope.launch {
            try {
                val settings = repository.getSettings()

                // Only proceed if interval is positive (timed mode)
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
                    AlarmScheduler.scheduleNextAlarm(
                        this@WallpaperForegroundService,
                        settings.intervalMinutes
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }

        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Wallpaper Changes",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifications for automatic wallpaper changes"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }

        return builder
            .setContentTitle("ScreenPoster")
            .setContentText("Changing wallpaper…")
            .setSmallIcon(android.R.drawable.ic_menu_gallery)
            .build()
    }
}
