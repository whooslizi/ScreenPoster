package dev.whooslizi.screenposter.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dev.whooslizi.screenposter.data.repository.WallpaperRepository
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
        if (intent.action == Intent.ACTION_USER_PRESENT) {
            CoroutineScope(Dispatchers.IO).launch {
                val settings = repository.getSettings()
                // -1 implies Every Unlock
                if (settings?.intervalMinutes == -1) {
                    val nextWallpaper = repository.getNextWallpaper()
                    if (nextWallpaper != null) {
                        val uriStr = nextWallpaper.editedUri ?: nextWallpaper.uri
                        wallpaperHelper.setWallpaper(uriStr, WallpaperHelper.TARGET_BOTH)
                        repository.addHistory(nextWallpaper.id)
                    }
                }
            }
        }
    }
}
