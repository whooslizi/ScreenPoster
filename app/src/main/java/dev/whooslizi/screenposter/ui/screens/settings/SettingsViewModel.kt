package dev.whooslizi.screenposter.ui.screens.settings

import android.app.AlarmManager
import android.content.Context
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import dev.whooslizi.screenposter.data.local.entity.SettingsEntity
import dev.whooslizi.screenposter.data.repository.WallpaperRepository
import dev.whooslizi.screenposter.util.AlarmScheduler
import dev.whooslizi.screenposter.util.BatteryOptimizationHelper
import dev.whooslizi.screenposter.worker.WallpaperWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: WallpaperRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    companion object {
        const val WORK_NAME = "wallpaper_auto_change"
    }

    val settings: StateFlow<SettingsEntity?> = repository.getSettingsFlow()
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val workInfos: kotlinx.coroutines.flow.Flow<List<WorkInfo>> = 
        WorkManager.getInstance(context).getWorkInfosForUniqueWorkFlow(WORK_NAME)

    private val _isBatteryOptimized = MutableStateFlow(!BatteryOptimizationHelper.isIgnoringBatteryOptimizations(context))
    val isBatteryOptimized: StateFlow<Boolean> = _isBatteryOptimized

    private val _hasExactAlarmPermission = MutableStateFlow(checkExactAlarmPermission())
    val hasExactAlarmPermission: StateFlow<Boolean> = _hasExactAlarmPermission

    val isChineseOem: Boolean = BatteryOptimizationHelper.isChineseOem()

    fun refreshPermissionsStatus() {
        _isBatteryOptimized.value = !BatteryOptimizationHelper.isIgnoringBatteryOptimizations(context)
        _hasExactAlarmPermission.value = checkExactAlarmPermission()
    }

    private fun checkExactAlarmPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            return alarmManager.canScheduleExactAlarms()
        }
        return true // Always true on older Android versions
    }

    fun requestBatteryOptimizationExemption() {
        BatteryOptimizationHelper.requestIgnoreBatteryOptimizations(context)
    }

    fun openAutoStartSettings(): Boolean {
        return BatteryOptimizationHelper.openAutoStartSettings(context)
    }

    fun updateInterval(intervalMinutes: Int) {
        viewModelScope.launch {
            val current = repository.getSettings()
            repository.updateSettings(current.copy(intervalMinutes = intervalMinutes))

            val workManager = WorkManager.getInstance(context)

            if (intervalMinutes > 0) {
                // Schedule periodic wallpaper change via WorkManager (fallback)
                val workRequest = PeriodicWorkRequestBuilder<WallpaperWorker>(
                    intervalMinutes.toLong(), TimeUnit.MINUTES
                ).build()

                workManager.enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.UPDATE,
                    workRequest
                )

                AlarmScheduler.scheduleNextAlarm(context, intervalMinutes)
            } else {
                // -1 = Every Unlock (handled by UnlockReceiver)
                // -2 = On Device Boot (handled by UnlockReceiver)
                // Cancel any periodic work and alarms
                workManager.cancelUniqueWork(WORK_NAME)
                AlarmScheduler.cancelAlarm(context)
            }
        }
    }

    fun setRandomMode(enabled: Boolean) {
        viewModelScope.launch {
            val current = repository.getSettings()
            repository.updateSettings(
                current.copy(
                    randomMode = enabled,
                    sequentialMode = !enabled,
                    noRepeatShuffle = if (!enabled) false else current.noRepeatShuffle
                )
            )
        }
    }

    fun setNoRepeatShuffle(enabled: Boolean) {
        viewModelScope.launch {
            val current = repository.getSettings()
            repository.updateSettings(current.copy(noRepeatShuffle = enabled))
        }
    }

    fun setHomeScreenBlurPercent(percent: Int) {
        viewModelScope.launch {
            val current = repository.getSettings()
            repository.updateSettings(current.copy(homeScreenBlurPercent = percent.coerceIn(0, 100)))
        }
    }
}
