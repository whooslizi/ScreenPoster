package dev.whooslizi.screenposter.ui.screens.preview

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.whooslizi.screenposter.data.local.entity.WallpaperEntity
import dev.whooslizi.screenposter.data.repository.WallpaperRepository
import dev.whooslizi.screenposter.util.WallpaperHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PreviewViewModel @Inject constructor(
    private val repository: WallpaperRepository,
    private val wallpaperHelper: WallpaperHelper,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val wallpaperId: Long = savedStateHandle.get<Long>("wallpaperId") ?: -1L

    private val _wallpaper = MutableStateFlow<WallpaperEntity?>(null)
    val wallpaper: StateFlow<WallpaperEntity?> = _wallpaper

    init {
        viewModelScope.launch {
            if (wallpaperId != -1L) {
                _wallpaper.value = repository.getWallpaperById(wallpaperId)
            }
        }
    }

    fun setWallpaper(target: Int) {
        val currentWallpaper = _wallpaper.value ?: return
        val uriStr = currentWallpaper.editedUri ?: currentWallpaper.uri

        viewModelScope.launch {
            val settings = repository.getSettings()
            wallpaperHelper.setWallpaper(uriStr, target, settings.homeScreenBlurPercent)
        }
    }
}
