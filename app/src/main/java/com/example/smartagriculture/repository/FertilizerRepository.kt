package com.example.smartagriculture.repository

import com.example.smartagriculture.model.FertilizerRule

object FertilizerRepository {

    private val rules = listOf(
        FertilizerRule("Wheat", "Loamy", "Urea + DAP", "100 kg/acre Urea, 50 kg/acre DAP"),
        FertilizerRule("Rice", "Clayey", "Urea + Potash", "120 kg/acre Urea, 40 kg/acre Potash"),
        FertilizerRule("Maize", "Sandy Loam", "NPK 12:32:16", "80 kg/acre"),
        FertilizerRule("Sugarcane", "Loamy", "NPK 10:26:26", "150 kg/acre"),
        FertilizerRule("Cotton", "Black Soil", "Urea + SSP", "90 kg/acre Urea, 60 kg/acre SSP"),
        FertilizerRule("Mustard", "Loamy", "DAP + MOP", "50 kg/acre DAP, 20 kg/acre MOP")
    )

    fun recommend(cropName: String, soilType: String): FertilizerRule? {
        for (rule in rules) {
            if (rule.cropName.equals(cropName, ignoreCase = true) &&
                rule.soilType.equals(soilType, ignoreCase = true)
            ) {
                return rule
            }
        }
        return null
    }

    fun getAllCropNames(): List<String> = rules.map { it.cropName }.distinct()
}