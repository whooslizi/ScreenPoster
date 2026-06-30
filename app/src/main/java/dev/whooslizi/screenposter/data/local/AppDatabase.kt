package dev.whooslizi.screenposter.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import dev.whooslizi.screenposter.data.local.dao.AlbumDao
import dev.whooslizi.screenposter.data.local.dao.HistoryDao
import dev.whooslizi.screenposter.data.local.dao.SettingsDao
import dev.whooslizi.screenposter.data.local.dao.WallpaperDao
import dev.whooslizi.screenposter.data.local.entity.AlbumEntity
import dev.whooslizi.screenposter.data.local.entity.HistoryEntity
import dev.whooslizi.screenposter.data.local.entity.SettingsEntity
import dev.whooslizi.screenposter.data.local.entity.WallpaperEntity

@Database(
    entities = [
        AlbumEntity::class, 
        WallpaperEntity::class, 
        HistoryEntity::class,
        SettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun albumDao(): AlbumDao
    abstract fun wallpaperDao(): WallpaperDao
    abstract fun historyDao(): HistoryDao
    abstract fun settingsDao(): SettingsDao
}
