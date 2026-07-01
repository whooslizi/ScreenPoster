package dev.whooslizi.screenposter.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey
    val id: Int = 1, // Only one settings row
    val shuffle: Boolean = false,
    val intervalMinutes: Int = 60, // 15, 30, 60, 360, 720, 1440
    val lastWallpaperId: Long = -1,
    val randomMode: Boolean = false,
    val sequentialMode: Boolean = true,
    val noRepeatShuffle: Boolean = false,
    val homeScreenBlurPercent: Int = 0 // 0-100, blur applied to home screen wallpaper
)
