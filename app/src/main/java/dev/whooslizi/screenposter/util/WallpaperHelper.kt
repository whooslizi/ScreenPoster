package dev.whooslizi.screenposter.util

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WallpaperHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val TARGET_HOME = 1
        const val TARGET_LOCK = 2
        const val TARGET_BOTH = 3
    }

    suspend fun setWallpaper(uriString: String, target: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            val uri = Uri.parse(uriString)
            val wallpaperManager = WallpaperManager.getInstance(context)
            
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            if (inputStream != null) {
                val bitmap = BitmapFactory.decodeStream(inputStream)
                
                when (target) {
                    TARGET_HOME -> {
                        wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM)
                    }
                    TARGET_LOCK -> {
                        if (wallpaperManager.isLockscreenLiveWallpaperEnabled) {
                            wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
                        } else {
                            // Fallback for devices that don't support separate lock screen
                            wallpaperManager.setBitmap(bitmap)
                        }
                    }
                    TARGET_BOTH -> {
                        wallpaperManager.setBitmap(bitmap)
                    }
                }
                inputStream.close()
                return@withContext true
            }
            return@withContext false
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }
}
