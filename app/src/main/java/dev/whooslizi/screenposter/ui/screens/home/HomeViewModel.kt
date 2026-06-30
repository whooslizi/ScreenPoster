package dev.whooslizi.screenposter.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.whooslizi.screenposter.data.local.entity.AlbumEntity
import dev.whooslizi.screenposter.data.repository.WallpaperRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: WallpaperRepository
) : ViewModel() {

    val albums: StateFlow<List<AlbumEntity>> = repository.getAllAlbums()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun createAlbumAndAddImages(name: String, uris: List<String>) {
        viewModelScope.launch {
            val albumId = repository.createAlbum(name, uris.firstOrNull())
            repository.addWallpapers(albumId, uris)
        }
    }
}
