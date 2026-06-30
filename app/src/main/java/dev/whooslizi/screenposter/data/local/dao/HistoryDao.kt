package dev.whooslizi.screenposter.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import dev.whooslizi.screenposter.data.local.entity.HistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentHistory(limit: Int): Flow<List<HistoryEntity>>

    @Insert
    suspend fun insertHistory(history: HistoryEntity)
    
    @Query("DELETE FROM history")
    suspend fun clearHistory()
}
