package com.example.smartagriculture.pdf

data class DiagnosisPayload(
    val cropName: String,
    val diseaseName: String,
    val confidenceScore: Float, // 0.0 to 1.0
    val isHealthy: Boolean,
    val organicRemedy: String,
    val chemicalRemedy: String = ""
)
