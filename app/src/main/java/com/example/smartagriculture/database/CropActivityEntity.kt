package com.example.smartagriculture.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "crop_activities")
data class CropActivityEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cropName: String,
    val activityTitle: String,
    val scheduledDate: Long, // Epoch Millis
    val isCompleted: Boolean = false,
    val activityType: String = "GENERAL" // e.g., "FERTILIZER", "WATERING", "SPRAY", "HARVEST"
)
