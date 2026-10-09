package com.example.smartagriculture.model

import java.io.Serializable

data class DiseaseAnalysisResult(
    val diseaseName: String,
    val scientificName: String,
    val confidence: Int,
    val isLowConfidence: Boolean,
    val aiExplanation: String,
    val organicCare: String,
    val chemicalCare: String,
    val activationMatrix: Array<FloatArray>? = null,
    val cropName: String = "Crop",
    val isNewFinding: Boolean = false
) : Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DiseaseAnalysisResult

        if (diseaseName != other.diseaseName) return false
        if (scientificName != other.scientificName) return false
        if (confidence != other.confidence) return false
        if (isLowConfidence != other.isLowConfidence) return false
        if (aiExplanation != other.aiExplanation) return false
        if (organicCare != other.organicCare) return false
        if (chemicalCare != other.chemicalCare) return false
        if (cropName != other.cropName) return false
        if (isNewFinding != other.isNewFinding) return false
        if (activationMatrix != null) {
            if (other.activationMatrix == null) return false
            if (!activationMatrix.contentDeepEquals(other.activationMatrix)) return false
        } else if (other.activationMatrix != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = diseaseName.hashCode()
        result = 31 * result + scientificName.hashCode()
        result = 31 * result + confidence
        result = 31 * result + isLowConfidence.hashCode()
        result = 31 * result + aiExplanation.hashCode()
        result = 31 * result + organicCare.hashCode()
        result = 31 * result + chemicalCare.hashCode()
        result = 31 * result + cropName.hashCode()
        result = 31 * result + isNewFinding.hashCode()
        result = 31 * result + (activationMatrix?.contentDeepHashCode() ?: 0)
        return result
    }
}
