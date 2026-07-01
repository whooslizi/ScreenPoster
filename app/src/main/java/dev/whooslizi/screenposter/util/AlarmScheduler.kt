package dev.whooslizi.screenposter.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import dev.whooslizi.screenposter.receiver.WallpaperAlarmReceiver
import java.util.concurrent.TimeUnit

/**
 * Utility to schedule exact alarms for wallpaper changes.
 * Uses AlarmManager.setExactAndAllowWhileIdle() to reliably fire
 * on Chinese OEM ROMs (Vivo OriginOS, OPPO ColorOS, Xiaomi MIUI, etc.)
 * that aggressively kill background WorkManager tasks.
 */
object AlarmScheduler {

    private const val ALARM_REQUEST_CODE = 1001

    fun scheduleNextAlarm(context: Context, intervalMinutes: Int) {
        if (intervalMinutes <= 0) {
            cancelAlarm(context)
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, WallpaperAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAtMillis = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(intervalMinutes.toLong())

        // Use setExactAndAllowWhileIdle for reliable execution on all devices
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // On API 31+, check canScheduleExactAlarms
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                // Fallback to inexact alarm if exact alarm permission not granted
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    fun cancelAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, WallpaperAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
