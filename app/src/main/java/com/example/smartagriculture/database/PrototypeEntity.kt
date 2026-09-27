package com.example.smartagriculture.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters

class VectorTypeConverter {
    @TypeConverter
    fun fromDoubleList(list: List<Double>): String {
        return list.joinToString(",")
    }

    @TypeConverter
    fun toDoubleList(data: String): List<Double> {
        if (data.isBlank()) return emptyList()
        return data.split(",").mapNotNull { it.toDoubleOrNull() }
    }
}

/**
 * Enrolled class prototype storing 128-dimensional bottleneck embedding vector (MOD-04, Feature 6).
 */
@Entity(tableName = "few_shot_prototypes")
@TypeConverters(VectorTypeConverter::class)
data class PrototypeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val className: String,
    val embedding: List<Double>,
    val timestamp: Long = System.currentTimeMillis(),
)
