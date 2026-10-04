package com.example.smartagriculture.viewmodel

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartagriculture.database.AppDatabase
import com.example.smartagriculture.database.ScanHistoryDao
import com.example.smartagriculture.ml.ClassResult
import com.example.smartagriculture.ml.TFLiteClassifier
import com.example.smartagriculture.model.ScanHistoryItem
import com.example.smartagriculture.network.CloudPredictionResponse
import com.example.smartagriculture.quality.ImageQualityChecker
import com.example.smartagriculture.quality.QualityResult
import com.example.smartagriculture.repository.CloudRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * UI State sealed hierarchy for Crop Health Diagnosis Pipeline.
 */
sealed class CropHealthUiState {
    object Idle : CropHealthUiState()
    object Loading : CropHealthUiState()
    data class QualityError(val qualityResult: QualityResult) : CropHealthUiState()
    data class LocalSuccess(val result: ClassResult, val scanId: Long) : CropHealthUiState()
    data class CloudSuccess(val response: CloudPredictionResponse, val scanId: Long) : CropHealthUiState()
    data class Error(val message: String) : CropHealthUiState()
}

/**
 * Hybrid Orchestration ViewModel for Offline-First Mobile Crop Health Assistant (LeafLens AI).
 *
 * Implements the complete hybrid decision pipeline:
 * Step A: Pre-inference OpenCV Image Quality Check (Blur & Extreme Lighting).
 * Step B: On-Device TensorFlow Lite MobileNetV2 Local Classification.
 * Step C: High Confidence Check (Confidence >= 0.70 -> Immediate Local Success).
 * Step D: Low Confidence Fallback (< 0.70 -> Automatic FastAPI Server-Side Re-analysis).
 * Step E: Diagnosis Persistence to Local SQLite Database via Room [ScanHistoryDao].
 */
class CropHealthViewModel(
    private var scanHistoryDao: ScanHistoryDao? = null,
    private val cloudRepository: CloudRepository = CloudRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<CropHealthUiState>(CropHealthUiState.Idle)
    val uiState: StateFlow<CropHealthUiState> = _uiState.asStateFlow()

    /**
     * Executes the hybrid crop health analysis pipeline on input captured leaf image.
     *
     * @param context Application/Activity context used to load TFLite model and access DB.
     * @param bitmap Input captured leaf image.
     */
    fun analyzeCropLeaf(context: Context, bitmap: Bitmap, forceOverride: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = CropHealthUiState.Loading

            // Ensure ScanHistoryDao initialization
            if (scanHistoryDao == null) {
                scanHistoryDao = AppDatabase.getDatabase(context.applicationContext).scanHistoryDao()
            }

            try {
                // -------------------------------------------------------------------------
                // STEP A: Validate Image Quality (OpenCV Blur & Extreme Lighting Check)
                // -------------------------------------------------------------------------
                val qualityResult = withContext(Dispatchers.Default) {
                    ImageQualityChecker.validateImage(bitmap)
                }

                // If quality gate fails and no bypass flag is dispatched, emit error
                if (!qualityResult.isValid && !forceOverride) {
                    _uiState.value = CropHealthUiState.QualityError(qualityResult)
                    return@launch
                }

                // Save captured bitmap to internal storage for local persistence
                val imagePath = withContext(Dispatchers.IO) {
                    saveBitmapToInternalStorage(context, bitmap)
                }

                // -------------------------------------------------------------------------
                // STEP B: Run Local On-Device TFLite Classification
                // -------------------------------------------------------------------------
                val classifier = TFLiteClassifier(context.applicationContext)
                val localResult = withContext(Dispatchers.Default) {
                    try {
                        classifier.classifyImage(bitmap)
                    } finally {
                        classifier.close()
                    }
                }

                // -------------------------------------------------------------------------
                // STEP C: High Confidence Check (Confidence >= 0.70)
                // -------------------------------------------------------------------------
                if (localResult.isHighConfidence) {
                    // Local Diagnosis confident -> Persist to Room DB & return local success
                    val scanItem = ScanHistoryItem(
                        imagePath = imagePath,
                        cropName = extractCropName(localResult.diseaseName),
                        diseaseName = localResult.diseaseName,
                        status = if (localResult.diseaseName.contains("healthy", ignoreCase = true)) "Healthy" else "Diseased",
                        scientificName = "",
                        confidence = (localResult.confidence * 100).toInt(),
                        isLowConfidence = false,
                        aiExplanation = "Local on-device MobileNetV2 diagnosis completed with high confidence (${(localResult.confidence * 100).toInt()}%).",
                        organicCare = "Maintain proper irrigation and clean farm tools.",
                        chemicalCare = "Apply standard protective fungicide if early symptoms persist."
                    )

                    val scanId = withContext(Dispatchers.IO) {
                        scanHistoryDao?.insertScan(scanItem) ?: 0L
                    }

                    _uiState.value = CropHealthUiState.LocalSuccess(localResult, scanId)
                    return@launch
                }

                // -------------------------------------------------------------------------
                // STEP D: Low Confidence (< 0.70) -> Automatic FastAPI Cloud Fallback
                // -------------------------------------------------------------------------
                val cloudResult = cloudRepository.uploadImageForCloudAnalysis(bitmap)

                if (cloudResult.isSuccess) {
                    val cloudResponse = cloudResult.getOrThrow()

                    val cloudScanItem = ScanHistoryItem(
                        imagePath = imagePath,
                        cropName = extractCropName(cloudResponse.diseaseName),
                        diseaseName = cloudResponse.diseaseName,
                        status = if (cloudResponse.diseaseName.contains("healthy", ignoreCase = true)) "Healthy" else "Diseased",
                        scientificName = "",
                        confidence = (cloudResponse.confidence * 100).toInt(),
                        isLowConfidence = false,
                        aiExplanation = cloudResponse.aiExplanation ?: "Server-side re-analysis completed via FastAPI backend.",
                        organicCare = cloudResponse.organicCare ?: "Apply recommended organic treatments.",
                        chemicalCare = cloudResponse.chemicalCare ?: "Follow server prescribed chemical protocol."
                    )

                    val scanId = withContext(Dispatchers.IO) {
                        scanHistoryDao?.insertScan(cloudScanItem) ?: 0L
                    }

                    _uiState.value = CropHealthUiState.CloudSuccess(cloudResponse, scanId)
                } else {
                    // Cloud fallback failed (e.g. offline) -> Fallback to local result with low-confidence flag
                    val fallbackScanItem = ScanHistoryItem(
                        imagePath = imagePath,
                        cropName = extractCropName(localResult.diseaseName),
                        diseaseName = localResult.diseaseName,
                        status = "Uncertain",
                        scientificName = "",
                        confidence = (localResult.confidence * 100).toInt(),
                        isLowConfidence = true,
                        aiExplanation = "Local diagnosis confidence is low (${(localResult.confidence * 100).toInt()}%). Cloud server unreachable (${cloudResult.exceptionOrNull()?.message}).",
                        organicCare = "Inspect crop manually or re-scan in daylight.",
                        chemicalCare = "Consult local agronomist before applying chemical treatments."
                    )

                    val scanId = withContext(Dispatchers.IO) {
                        scanHistoryDao?.insertScan(fallbackScanItem) ?: 0L
                    }

                    _uiState.value = CropHealthUiState.LocalSuccess(localResult, scanId)
                }

            } catch (e: Exception) {
                _uiState.value = CropHealthUiState.Error(e.message ?: "An unexpected error occurred during crop health analysis.")
            }
        }
    }

    /**
     * Saves bitmap to internal app storage and returns absolute file path.
     */
    private fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap): String {
        return try {
            val directory = File(context.filesDir, "scan_images")
            if (!directory.exists()) {
                directory.mkdirs()
            }
            val file = File(directory, "scan_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { out ->
                val safeBitmap = if (bitmap.config == Bitmap.Config.HARDWARE) {
                    bitmap.copy(Bitmap.Config.ARGB_8888, false)
                } else {
                    bitmap
                }
                safeBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            file.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            ""
        }
    }

    /**
     * Extracts primary crop name from formatted disease label string (e.g., "Tomato - Early blight" -> "Tomato").
     */
    private fun extractCropName(fullDiseaseName: String): String {
        return if (fullDiseaseName.contains("-")) {
            fullDiseaseName.split("-")[0].trim()
        } else {
            "Crop"
        }
    }
}
