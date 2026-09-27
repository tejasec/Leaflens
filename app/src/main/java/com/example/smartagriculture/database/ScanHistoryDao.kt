package com.example.smartagriculture.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.smartagriculture.model.ScanHistoryItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScan(scan: ScanHistoryItem): Long

    @Update
    suspend fun updateScan(scan: ScanHistoryItem)

    @Query("SELECT * FROM scan_history ORDER BY timestamp DESC")
    fun getAllScans(): Flow<List<ScanHistoryItem>>

    @Query("SELECT * FROM scan_history WHERE id = :id LIMIT 1")
    suspend fun getScanById(id: Long): ScanHistoryItem?

    @Query("DELETE FROM scan_history WHERE id = :id")
    suspend fun deleteScan(id: Long)
}
