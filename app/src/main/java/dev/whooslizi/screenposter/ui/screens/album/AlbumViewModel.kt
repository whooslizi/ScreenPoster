package dev.whooslizi.screenposter.ui.screens.album

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.whooslizi.screenposter.data.local.entity.AlbumEntity
import dev.whooslizi.screenposter.data.local.entity.WallpaperEntity
import dev.whooslizi.screenposter.data.repository.WallpaperRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlbumViewModel @Inject constructor(
    private val repository: WallpaperRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val albumId: Long = savedStateHandle.get<Long>("albumId") ?: -1L

    private val _album = MutableStateFlow<AlbumEntity?>(null)
    val album: StateFlow<AlbumEntity?> = _album

    val wallpapers: StateFlow<List<WallpaperEntity>> = repository.getWallpapersForAlbum(albumId)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        viewModelScope.launch {
            if (albumId != -1L) {
                _album.value = repository.getAlbumById(albumId)
            }
        }
    }

    fun addImages(uris: List<String>) {
        viewModelScope.launch {
            repository.addWallpapers(albumId, uris)
        }
    }
}
