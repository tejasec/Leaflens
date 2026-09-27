package com.example.smartagriculture.model

data class FertilizerRule(
    val cropName: String,
    val soilType: String,
    val recommendedFertilizer: String,
    val dosage: String
)