package dev.whooslizi.screenposter.ui.screens.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.whooslizi.screenposter.data.local.entity.WallpaperEntity
import dev.whooslizi.screenposter.data.repository.WallpaperRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditorViewModel @Inject constructor(
    private val repository: WallpaperRepository,
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

    fun saveEdits(editedUri: String) {
        viewModelScope.launch {
            val current = _wallpaper.value
            if (current != null) {
                val updated = current.copy(editedUri = editedUri)
                repository.updateWallpaper(updated)
                _wallpaper.value = updated
            }
        }
    }
}
