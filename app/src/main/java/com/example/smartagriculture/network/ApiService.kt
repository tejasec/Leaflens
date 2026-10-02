package com.example.smartagriculture.network

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

/**
 * Data response model from FastAPI cloud backend re-analysis server.
 *
 * @property diseaseName Server-side predicted crop disease name.
 * @property confidence Prediction confidence score (0.0 to 1.0).
 * @property gradCamBase64 Base64-encoded PNG/JPEG string for server-generated Grad-CAM heatmap.
 * @property aiExplanation Detailed agronomic AI explanation and diagnosis context.
 * @property organicCare Recommended organic remedy steps.
 * @property chemicalCare Recommended chemical treatment protocols.
 */
data class CloudPredictionResponse(
    val diseaseName: String,
    val confidence: Float,
    val gradCamBase64: String? = null,
    val aiExplanation: String? = null,
    val organicCare: String? = null,
    val chemicalCare: String? = null
)

/**
 * Retrofit REST Interface for Cloud Fallback FastAPI Backend.
 */
interface ApiService {

    /**
     * Uploads low-confidence leaf image to FastAPI `/predict` endpoint for server-side re-analysis
     * and Grad-CAM explainability generation.
     *
     * @param image Multipart file payload containing compressed leaf image JPEG.
     * @return [Response] containing [CloudPredictionResponse] payload.
     */
    @Multipart
    @POST("predict")
    suspend fun predictCropHealth(
        @Part image: MultipartBody.Part
    ): Response<CloudPredictionResponse>
}
