package dev.whooslizi.screenposter.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dev.whooslizi.screenposter.data.local.entity.SettingsEntity
import dev.whooslizi.screenposter.data.repository.WallpaperRepository
import dev.whooslizi.screenposter.worker.WallpaperWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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

    fun updateInterval(intervalMinutes: Int) {
        viewModelScope.launch {
            val current = repository.getSettings()
            repository.updateSettings(current.copy(intervalMinutes = intervalMinutes))

            val workManager = WorkManager.getInstance(context)

            if (intervalMinutes > 0) {
                // Schedule periodic wallpaper change
                val workRequest = PeriodicWorkRequestBuilder<WallpaperWorker>(
                    intervalMinutes.toLong(), TimeUnit.MINUTES
                ).build()

                workManager.enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.UPDATE,
                    workRequest
                )
            } else {
                // -1 = Every Unlock
                // -2 = On Device Boot
                // Cancel any periodic work
                workManager.cancelUniqueWork(WORK_NAME)
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
}

