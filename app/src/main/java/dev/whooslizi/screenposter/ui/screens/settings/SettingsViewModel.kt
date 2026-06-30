package dev.whooslizi.screenposter.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.whooslizi.screenposter.data.local.entity.SettingsEntity
import dev.whooslizi.screenposter.data.repository.WallpaperRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: WallpaperRepository
) : ViewModel() {

    val settings: StateFlow<SettingsEntity?> = repository.getSettingsFlow()
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    fun updateInterval(intervalMinutes: Int) {
        viewModelScope.launch {
            val current = repository.getSettings()
            repository.updateSettings(current.copy(intervalMinutes = intervalMinutes))
            // TODO: Enqueue WorkManager here
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
