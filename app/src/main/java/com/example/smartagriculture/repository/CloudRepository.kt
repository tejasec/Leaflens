package com.example.smartagriculture.repository

import android.graphics.Bitmap
import com.example.smartagriculture.network.ApiService
import com.example.smartagriculture.network.CloudPredictionResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

/**
 * Repository handling automatic Cloud Fallback REST network calls to FastAPI server.
 *
 * Used when local on-device diagnosis confidence falls below the 0.70 threshold.
 */
class CloudRepository(
    private var baseUrl: String = "http://10.0.2.2:8000/" // Default local emulator / FastAPI server URL
) {

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    /**
     * Converts bitmap to Multipart JPEG file and submits to FastAPI `/predict` endpoint.
     *
     * @param bitmap Low-confidence leaf scan image.
     * @return [Result] wrapping [CloudPredictionResponse] on success or Exception on failure.
     */
    suspend fun uploadImageForCloudAnalysis(bitmap: Bitmap): Result<CloudPredictionResponse> = withContext(Dispatchers.IO) {
        try {
            // Compress bitmap into JPEG byte array
            val outputStream = ByteArrayOutputStream()
            val safeBitmap = if (bitmap.config == Bitmap.Config.HARDWARE) {
                bitmap.copy(Bitmap.Config.ARGB_8888, false)
            } else {
                bitmap
            }
            safeBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            val byteArray = outputStream.toByteArray()

            // Construct RequestBody & MultipartBody.Part compatible across OkHttp versions
            val mediaType = "image/jpeg".toMediaTypeOrNull()
            val requestBody = byteArray.toRequestBody(mediaType)
            val imagePart = MultipartBody.Part.createFormData("file", "leaf_scan.jpg", requestBody)

            // Execute network call to FastAPI backend
            val response = apiService.predictCropHealth(imagePart)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Cloud API HTTP error ${response.code()}"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Dynamically updates FastAPI server base URL (e.g. custom IP address).
     */
    fun updateServerUrl(newServerIp: String) {
        val formattedUrl = if (newServerIp.startsWith("http://") || newServerIp.startsWith("https://")) {
            if (newServerIp.endsWith("/")) newServerIp else "$newServerIp/"
        } else {
            "http://$newServerIp:8000/"
        }
        this.baseUrl = formattedUrl
    }
}
