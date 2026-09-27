package com.example.smartagriculture.model

data class Crop(
    val id: Int,
    val name: String,
    val season: String,
    val soilType: String,
    val waterNeed: String,
    val description: String
)