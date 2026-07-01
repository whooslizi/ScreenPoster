package dev.whooslizi.screenposter.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dev.whooslizi.screenposter.data.repository.WallpaperRepository
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

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val settings = repository.getSettings()
            if (settings.intervalMinutes < 15 && settings.intervalMinutes > 0) {
                return@withContext Result.success()
            }
            
            val nextWallpaper = repository.getNextWallpaper()
            
            if (nextWallpaper != null) {
                // Determine URI to use
                val uriStr = nextWallpaper.editedUri ?: nextWallpaper.uri
                
                // Set wallpaper to both by default for auto change, with blur
                wallpaperHelper.setWallpaper(
                    uriStr,
                    WallpaperHelper.TARGET_BOTH,
                    settings.homeScreenBlurPercent
                )
                
                repository.addHistory(nextWallpaper.id)
            }
            
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
