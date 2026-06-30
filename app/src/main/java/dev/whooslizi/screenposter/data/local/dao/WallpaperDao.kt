package dev.whooslizi.screenposter.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import dev.whooslizi.screenposter.data.local.entity.WallpaperEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WallpaperDao {
    @Query("SELECT * FROM wallpapers WHERE albumId = :albumId")
    fun getWallpapersForAlbum(albumId: Long): Flow<List<WallpaperEntity>>
    
    @Query("SELECT * FROM wallpapers WHERE favorite = 1")
    fun getFavoriteWallpapers(): Flow<List<WallpaperEntity>>

    @Query("SELECT * FROM wallpapers WHERE id = :id LIMIT 1")
    suspend fun getWallpaperById(id: Long): WallpaperEntity?

    @Query("SELECT * FROM wallpapers ORDER BY lastUsed ASC LIMIT 1")
    suspend fun getNextWallpaperInSequence(): WallpaperEntity?

    @Query("SELECT * FROM wallpapers ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomWallpaper(): WallpaperEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallpapers(wallpapers: List<WallpaperEntity>)

    @Update
    suspend fun updateWallpaper(wallpaper: WallpaperEntity)

    @Delete
    suspend fun deleteWallpaper(wallpaper: WallpaperEntity)
    
    @Query("DELETE FROM wallpapers WHERE albumId = :albumId")
    suspend fun deleteWallpapersForAlbum(albumId: Long)
}
