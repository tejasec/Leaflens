package com.example.smartagriculture.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SchemeDao {

    @Query("SELECT * FROM cached_schemes ORDER BY title ASC")
    fun getAllSchemes(): Flow<List<SchemeEntity>>

    @Query("SELECT * FROM cached_schemes ORDER BY title ASC")
    suspend fun getSchemesList(): List<SchemeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(schemes: List<SchemeEntity>)

    @Query("DELETE FROM cached_schemes")
    suspend fun clearAll()
}
