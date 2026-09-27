package com.example.smartagriculture.model

import java.io.Serializable

data class DiseaseAnalysisResult(
    val diseaseName: String,
    val scientificName: String,
    val confidence: Int,
    val isLowConfidence: Boolean,
    val aiExplanation: String,
    val organicCare: String,
    val chemicalCare: String
) : Serializable
