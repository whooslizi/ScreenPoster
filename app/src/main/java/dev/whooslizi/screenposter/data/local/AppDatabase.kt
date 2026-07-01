package dev.whooslizi.screenposter.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun albumDao(): AlbumDao
    abstract fun wallpaperDao(): WallpaperDao
    abstract fun historyDao(): HistoryDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE settings ADD COLUMN homeScreenBlurPercent INTEGER NOT NULL DEFAULT 0")
            }
        }
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE settings ADD COLUMN lastChangeTimeMillis INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}
