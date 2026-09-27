package com.example.smartagriculture.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PrototypeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrototype(prototype: PrototypeEntity): Long

    @Query("SELECT * FROM few_shot_prototypes")
    suspend fun getAllPrototypes(): List<PrototypeEntity>

    @Query("SELECT * FROM few_shot_prototypes WHERE className = :className")
    suspend fun getPrototypesByClass(className: String): List<PrototypeEntity>

    @Query("DELETE FROM few_shot_prototypes WHERE className = :className")
    suspend fun deletePrototypesByClass(className: String)

    @Query("DELETE FROM few_shot_prototypes")
    suspend fun clearAllPrototypes()
}
