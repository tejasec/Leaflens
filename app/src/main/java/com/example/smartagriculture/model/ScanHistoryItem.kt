package com.example.smartagriculture.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "scan_history")
data class ScanHistoryItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val imagePath: String,
    val cropName: String = "Tomato",
    val diseaseName: String,
    val status: String = "Diseased", // "Diseased", "Healthy", "Uncertain"
    val scientificName: String,
    val confidence: Int,
    val isLowConfidence: Boolean,
    val aiExplanation: String,
    val organicCare: String,
    val chemicalCare: String,
    val timestamp: Long = System.currentTimeMillis()
) : Serializable
