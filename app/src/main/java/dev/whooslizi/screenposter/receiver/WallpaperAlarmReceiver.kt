package dev.whooslizi.screenposter.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import dev.whooslizi.screenposter.service.WallpaperForegroundService

/**
 * Receives exact alarm broadcasts and starts the foreground service
 * to change the wallpaper. This ensures reliable execution even on
 * aggressive OEM ROMs (Vivo, OPPO, Xiaomi, etc.).
 */
class WallpaperAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val serviceIntent = Intent(context, WallpaperForegroundService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }
}
