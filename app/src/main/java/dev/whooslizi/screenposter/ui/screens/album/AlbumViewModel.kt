package dev.whooslizi.screenposter.ui.screens.album

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.whooslizi.screenposter.data.local.entity.AlbumEntity
import dev.whooslizi.screenposter.data.local.entity.WallpaperEntity
import dev.whooslizi.screenposter.data.repository.WallpaperRepository
import dev.whooslizi.screenposter.util.WallpaperHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlbumViewModel @Inject constructor(
    private val repository: WallpaperRepository,
    private val wallpaperHelper: WallpaperHelper,
    @ApplicationContext private val context: Context,
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

    fun setWallpaper(wallpaperId: Long, target: Int) {
        viewModelScope.launch {
            val wallpaper = repository.getWallpaperById(wallpaperId) ?: return@launch
            val uriStr = wallpaper.editedUri ?: wallpaper.uri
            val settings = repository.getSettings()
            wallpaperHelper.setWallpaper(uriStr, target, settings.homeScreenBlurPercent)
        }
    }

    fun toggleFavorite(wallpaperIds: Set<Long>) {
        viewModelScope.launch {
            val wallpapersList = wallpaperIds.mapNotNull { repository.getWallpaperById(it) }
            // If any are not favorited, favorite all. If all favorited, unfavorite all.
            val allFavorited = wallpapersList.all { it.favorite }
            wallpapersList.forEach { wallpaper ->
                repository.updateWallpaper(wallpaper.copy(favorite = !allFavorited))
            }
        }
    }

    fun deleteWallpapers(wallpaperIds: Set<Long>) {
        viewModelScope.launch {
            wallpaperIds.forEach { id ->
                val wallpaper = repository.getWallpaperById(id)
                if (wallpaper != null) {
                    repository.deleteWallpaper(wallpaper)
                }
            }
        }
    }

    fun shareWallpapers(wallpaperIds: Set<Long>): Intent? {
        val currentWallpapers = wallpapers.value.filter { it.id in wallpaperIds }
        if (currentWallpapers.isEmpty()) return null

        return if (currentWallpapers.size == 1) {
            val uri = Uri.parse(currentWallpapers.first().editedUri ?: currentWallpapers.first().uri)
            Intent(Intent.ACTION_SEND).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        } else {
            val uris = ArrayList(currentWallpapers.map {
                Uri.parse(it.editedUri ?: it.uri)
            })
            Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = "image/*"
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }
    }
}
