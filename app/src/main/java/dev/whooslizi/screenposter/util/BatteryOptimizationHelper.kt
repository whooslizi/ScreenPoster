package dev.whooslizi.screenposter.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings

/**
 * Helper to deal with Chinese OEM battery optimization restrictions.
 * Vivo OriginOS, OPPO ColorOS, Xiaomi MIUI, Huawei EMUI, and Samsung OneUI
 * all have proprietary app-killing mechanisms beyond standard Android Doze.
 */
object BatteryOptimizationHelper {

    /**
     * Check if the app is already exempt from battery optimization.
     */
    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return pm.isIgnoringBatteryOptimizations(context.packageName)
    }

    /**
     * Request the system to whitelist this app from battery optimization.
     * This opens the standard Android dialog asking the user to confirm.
     */
    fun requestIgnoreBatteryOptimizations(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${context.packageName}")
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to general battery optimization settings
            openBatteryOptimizationSettings(context)
        }
    }

    /**
     * Open general battery optimization settings page.
     */
    fun openBatteryOptimizationSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Try to open the OEM-specific auto-start / background management settings.
     * These are the proprietary settings that Chinese OEM ROMs use to kill apps.
     */
    fun openAutoStartSettings(context: Context): Boolean {
        val autoStartIntents = listOf(
            // Vivo OriginOS / FuntouchOS
            Intent().setComponent(ComponentName(
                "com.vivo.permissionmanager",
                "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"
            )),
            // Vivo alternative
            Intent().setComponent(ComponentName(
                "com.iqoo.secure",
                "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager"
            )),
            // Vivo iManager
            Intent().setComponent(ComponentName(
                "com.vivo.abe",
                "com.vivo.applicationbehaviorengine.ui.ExcessivePowerManagerActivity"
            )),
            // Xiaomi MIUI
            Intent().setComponent(ComponentName(
                "com.miui.securitycenter",
                "com.miui.permcenter.autostart.AutoStartManagementActivity"
            )),
            // OPPO ColorOS
            Intent().setComponent(ComponentName(
                "com.coloros.safecenter",
                "com.coloros.safecenter.startupapp.StartupAppListActivity"
            )),
            // OPPO alternative
            Intent().setComponent(ComponentName(
                "com.oppo.safe",
                "com.oppo.safe.permission.startup.StartupAppListActivity"
            )),
            // Huawei EMUI
            Intent().setComponent(ComponentName(
                "com.huawei.systemmanager",
                "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"
            )),
            // Samsung OneUI
            Intent().setComponent(ComponentName(
                "com.samsung.android.lool",
                "com.samsung.android.sm.ui.battery.BatteryActivity"
            )),
            // OnePlus
            Intent().setComponent(ComponentName(
                "com.oneplus.security",
                "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity"
            ))
        )

        for (intent in autoStartIntents) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                if (context.packageManager.resolveActivity(intent, 0) != null) {
                    context.startActivity(intent)
                    return true
                }
            } catch (_: Exception) {
                continue
            }
        }
        return false
    }

    /**
     * Detect if running on a known Chinese OEM ROM.
     */
    fun isChineseOem(): Boolean {
        val manufacturer = Build.MANUFACTURER.lowercase()
        return manufacturer in listOf(
            "vivo", "oppo", "xiaomi", "redmi", "huawei",
            "honor", "realme", "oneplus", "meizu", "zte",
            "lenovo", "iqoo"
        )
    }
}
