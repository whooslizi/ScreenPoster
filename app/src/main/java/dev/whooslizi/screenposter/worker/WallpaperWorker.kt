package dev.whooslizi.screenposter.worker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dev.whooslizi.screenposter.data.repository.WallpaperRepository
import dev.whooslizi.screenposter.util.AlarmScheduler
import dev.whooslizi.screenposter.util.WallpaperHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class WallpaperWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val repository: WallpaperRepository,
    private val wallpaperHelper: WallpaperHelper
) : CoroutineWorker(context, params) {

    companion object {
        const val WORK_NAME = "wallpaper_expedited_change"
        private const val NOTIFICATION_ID = 2002
        private const val CHANNEL_ID = "wallpaper_worker_channel"

        fun enqueueExpedited(context: Context) {
            val request = OneTimeWorkRequestBuilder<WallpaperWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()

            WorkManager.getInstance(context).enqueue(request)
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Wallpaper Change Service",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("ScreenPoster")
            .setContentText("Changing wallpaper...")
            .setSmallIcon(android.R.drawable.ic_menu_gallery)
            .build()

        return ForegroundInfo(NOTIFICATION_ID, notification)
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val settings = repository.getSettings()
            
            // Check if it's too soon to change (e.g., if unlock triggered it right after an alarm)
            // But if it's explicitly Every Unlock (-1) or Boot (-2), allow it.
            if (settings.intervalMinutes > 0) {
                val elapsed = System.currentTimeMillis() - settings.lastChangeTimeMillis
                // Allow a small buffer of 1 minute (60,000 ms)
                if (elapsed < (settings.intervalMinutes * 60_000L) - 60_000L) {
                    return@withContext Result.success()
                }
            }

            val nextWallpaper = repository.getNextWallpaper()
            if (nextWallpaper != null) {
                val uriStr = nextWallpaper.editedUri ?: nextWallpaper.uri
                
                wallpaperHelper.setWallpaper(
                    uriStr,
                    WallpaperHelper.TARGET_BOTH,
                    settings.homeScreenBlurPercent
                )
                
                repository.addHistory(nextWallpaper.id)
                
                // Update last change time
                repository.updateSettings(settings.copy(lastChangeTimeMillis = System.currentTimeMillis()))
            }
            
            // If it's a timed interval, schedule the next exact alarm
            if (settings.intervalMinutes > 0) {
                AlarmScheduler.scheduleNextAlarm(context, settings.intervalMinutes)
            }
            
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
