package dev.whooslizi.screenposter.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "history",
    foreignKeys = [
        ForeignKey(
            entity = WallpaperEntity::class,
            parentColumns = ["id"],
            childColumns = ["wallpaperId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("wallpaperId")]
)
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val wallpaperId: Long,
    val timestamp: Long = System.currentTimeMillis()
)
