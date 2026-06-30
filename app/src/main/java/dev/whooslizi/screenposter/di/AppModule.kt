package dev.whooslizi.screenposter.di

import android.content.Context
import androidx.room.Room
import dev.whooslizi.screenposter.data.local.AppDatabase
import dev.whooslizi.screenposter.data.local.dao.AlbumDao
import dev.whooslizi.screenposter.data.local.dao.HistoryDao
import dev.whooslizi.screenposter.data.local.dao.SettingsDao
import dev.whooslizi.screenposter.data.local.dao.WallpaperDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "screenposter_db"
        ).build()
    }

    @Provides
    fun provideAlbumDao(database: AppDatabase): AlbumDao = database.albumDao()

    @Provides
    fun provideWallpaperDao(database: AppDatabase): WallpaperDao = database.wallpaperDao()

    @Provides
    fun provideHistoryDao(database: AppDatabase): HistoryDao = database.historyDao()

    @Provides
    fun provideSettingsDao(database: AppDatabase): SettingsDao = database.settingsDao()
}
