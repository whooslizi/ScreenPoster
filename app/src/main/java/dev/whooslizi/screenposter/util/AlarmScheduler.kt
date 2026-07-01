package dev.whooslizi.screenposter.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import dev.whooslizi.screenposter.receiver.WallpaperAlarmReceiver
import java.util.concurrent.TimeUnit

/**
 * Utility to schedule alarms for wallpaper changes.
 * 
 * Uses AlarmManager.setAlarmClock() which is the ONLY alarm type that
 * aggressive Chinese OEM ROMs (Vivo OriginOS, OPPO ColorOS, Xiaomi MIUI)
 * cannot suppress or defer. These OEMs treat alarm clock alarms as user-facing
 * alarms that must fire on time.
 * 
 * setExactAndAllowWhileIdle() is NOT sufficient — it can still be deferred
 * by up to 15 minutes in Doze and Chinese OEMs may defer it indefinitely.
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

        // Use setAlarmClock — the nuclear option.
        // This is treated as a user-facing alarm clock, which even Vivo OriginOS
        // and other aggressive OEMs cannot suppress.
        // The "show intent" (2nd param) can be null — it just means no UI is shown
        // when the user taps the alarm icon in the status bar.
        val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerAtMillis, null)
        alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
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
