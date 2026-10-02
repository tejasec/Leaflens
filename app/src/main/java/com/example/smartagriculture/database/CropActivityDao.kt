package com.example.smartagriculture.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CropActivityDao {

    @Query("SELECT * FROM crop_activities ORDER BY scheduledDate ASC")
    fun getAllActivities(): Flow<List<CropActivityEntity>>

    @Query("SELECT * FROM crop_activities WHERE isCompleted = 0 AND scheduledDate > :currentTime ORDER BY scheduledDate ASC")
    suspend fun getUpcomingActivities(currentTime: Long): List<CropActivityEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: CropActivityEntity): Long

    @Update
    suspend fun updateActivity(activity: CropActivityEntity)

    @Delete
    suspend fun deleteActivity(activity: CropActivityEntity)

    @Query("DELETE FROM crop_activities WHERE id = :id")
    suspend fun deleteById(id: Long)
}
