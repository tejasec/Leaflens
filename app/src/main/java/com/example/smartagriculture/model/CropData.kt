package com.example.smartagriculture.model

import java.io.Serializable

data class CropDetailItem(
    val id: Int,
    val name: String,
    val scientificName: String,
    val category: String, // "Vegetables", "Cereals", "Fruits"
    val growingSeason: String,
    val idealTemp: String,
    val soilType: String,
    val waterRequirement: String,
    val about: String,
    val proTip: String,
    val imageRes: Int,
    val diseases: List<CropDiseaseItem>,
    var isFavorite: Boolean = false,
    var isTracked: Boolean = false
) : Serializable {
    val season: String get() = growingSeason
    val waterNeed: String get() = waterRequirement
    val description: String get() = about
}

data class CropDiseaseItem(
    val id: Int,
    val diseaseName: String,
    val scientificName: String,
    val description: String,
    val symptoms: List<String>,
    val organicCare: String,
    val chemicalCare: String,
    val prevention: String,
    val similarDiseases: List<String>
) : Serializable

data class MyCropItem(
    val id: Int,
    val cropName: String,
    val status: String, // "Growing", "Vegetative", "Flowering", "Harvesting"
    val plantedDate: String,
    val imageRes: Int,
    val timeline: List<CropTimelineStep>
) : Serializable

data class CropTimelineStep(
    val stageName: String,
    val date: String,
    val isCompleted: Boolean,
    val isCurrent: Boolean
) : Serializable
