package dev.whooslizi.screenposter.data.repository

import dev.whooslizi.screenposter.data.local.dao.AlbumDao
import dev.whooslizi.screenposter.data.local.dao.HistoryDao
import dev.whooslizi.screenposter.data.local.dao.SettingsDao
import dev.whooslizi.screenposter.data.local.dao.WallpaperDao
import dev.whooslizi.screenposter.data.local.entity.AlbumEntity
import dev.whooslizi.screenposter.data.local.entity.HistoryEntity
import dev.whooslizi.screenposter.data.local.entity.SettingsEntity
import dev.whooslizi.screenposter.data.local.entity.WallpaperEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class WallpaperRepository @Inject constructor(
    private val albumDao: AlbumDao,
    private val wallpaperDao: WallpaperDao,
    private val historyDao: HistoryDao,
    private val settingsDao: SettingsDao
) {
    
    // Albums
    fun getAllAlbums(): Flow<List<AlbumEntity>> = albumDao.getAllAlbums()
    suspend fun getAlbumById(id: Long) = albumDao.getAlbumById(id)
    suspend fun createAlbum(name: String, coverImageUri: String? = null): Long {
        return albumDao.insertAlbum(AlbumEntity(name = name, coverImageUri = coverImageUri))
    }
    suspend fun updateAlbum(album: AlbumEntity) = albumDao.updateAlbum(album)
    suspend fun deleteAlbum(album: AlbumEntity) = albumDao.deleteAlbum(album)
    fun searchAlbums(query: String) = albumDao.searchAlbums(query)

    // Wallpapers
    fun getWallpapersForAlbum(albumId: Long): Flow<List<WallpaperEntity>> = wallpaperDao.getWallpapersForAlbum(albumId)
    suspend fun getWallpaperById(id: Long) = wallpaperDao.getWallpaperById(id)
    suspend fun addWallpapers(albumId: Long, uris: List<String>) {
        val wallpapers = uris.map { uri ->
            WallpaperEntity(albumId = albumId, uri = uri)
        }
        wallpaperDao.insertWallpapers(wallpapers)
        
        // Update album cover if needed
        val album = albumDao.getAlbumById(albumId)
        if (album?.coverImageUri == null && uris.isNotEmpty()) {
            albumDao.updateAlbum(album!!.copy(coverImageUri = uris.first()))
        }
    }
    suspend fun updateWallpaper(wallpaper: WallpaperEntity) = wallpaperDao.updateWallpaper(wallpaper)
    suspend fun deleteWallpaper(wallpaper: WallpaperEntity) = wallpaperDao.deleteWallpaper(wallpaper)
    suspend fun getNextWallpaper(): WallpaperEntity? {
        val settings = settingsDao.getSettings()
        return if (settings?.randomMode == true) {
            wallpaperDao.getRandomWallpaper()
        } else {
            wallpaperDao.getNextWallpaperInSequence()
        }
    }

    // History
    fun getRecentHistory(limit: Int = 50) = historyDao.getRecentHistory(limit)
    suspend fun addHistory(wallpaperId: Long) {
        historyDao.insertHistory(HistoryEntity(wallpaperId = wallpaperId))
        val wallpaper = wallpaperDao.getWallpaperById(wallpaperId)
        if (wallpaper != null) {
            wallpaperDao.updateWallpaper(wallpaper.copy(lastUsed = System.currentTimeMillis()))
        }
    }

    // Settings
    fun getSettingsFlow() = settingsDao.getSettingsFlow()
    suspend fun getSettings(): SettingsEntity {
        var settings = settingsDao.getSettings()
        if (settings == null) {
            settings = SettingsEntity()
            settingsDao.insertSettings(settings)
        }
        return settings
    }
    suspend fun updateSettings(settings: SettingsEntity) = settingsDao.updateSettings(settings)
}
