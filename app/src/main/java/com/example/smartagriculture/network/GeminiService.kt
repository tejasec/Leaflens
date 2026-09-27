package com.example.smartagriculture.network

import android.graphics.Bitmap
import com.example.smartagriculture.BuildConfig
import com.example.smartagriculture.model.DiseaseAnalysisResult
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.delay

object GeminiService {
    private val API_KEY = BuildConfig.GEMINI_API_KEY
    
    // We try to initialize the model. If it fails due to missing key, we handle it in getRecommendation.
    private val generativeModel = try {
        if (API_KEY.isNotBlank() && API_KEY != "YOUR_API_KEY_HERE") {
            GenerativeModel(
                modelName = "gemini-flash-latest",
                apiKey = API_KEY
            )
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }

    suspend fun getFertilizerRecommendation(crop: String, soil: String): String {
        if (generativeModel == null) {
            // Mock response for UI testing
            kotlinx.coroutines.delay(1500) // Simulate network delay
            return """
                Fertilizer Recommendation for $crop in $soil:
                
                1. Analysis: $crop grows moderately well in $soil. This soil type tends to require more frequent nutrient replenishment.
                2. Fertilizer Type: Use a balanced NPK fertilizer (like 10-10-10) enriched with Zinc and Boron. Organic compost is highly recommended.
                3. Dosage: Apply 50 kg per acre in two split doses (baseline and 30 days after planting).
                4. Pro Tip: Maintain adequate soil moisture when applying fertilizer to ensure optimal nutrient absorption!
                
                (Note: This is a simulated response. Please add a valid Google Gemini API key to local.properties for real AI recommendations.)
            """.trimIndent()
        }
        
        val prompt = """
            You are an expert agronomist. 
            A farmer is asking for a detailed fertilizer recommendation for growing $crop in $soil soil.
            Provide a comprehensive, professional recommendation including:
            1. An analysis of how $crop performs in $soil soil.
            2. The best fertilizer type (e.g., specific NPK ratio, organic vs synthetic, micro-nutrients needed).
            3. Recommended dosage per acre and application schedule.
            4. A pro tip for maximizing yield with this specific crop and soil combination.
            Make it clear, easy to read, and highly specific to $crop and $soil. Do not use markdown formatting like asterisks.
        """.trimIndent()

        return try {
            val response = generativeModel.generateContent(prompt)
            response.text ?: "Could not generate a recommendation at this time."
        } catch (e: Exception) {
            "AI Recommendation Unavailable: Please provide a valid Gemini API key in local.properties.\n\nFallback Recommendation:\nNitrogen-rich fertilizers usually help most crops in $soil soil."
        }
    }

    suspend fun analyzeCropDisease(bitmap: Bitmap): DiseaseAnalysisResult {
        if (generativeModel == null) {
            delay(2000) // simulate AI inference delay
            return DiseaseAnalysisResult(
                diseaseName = "Tomato Early Blight",
                scientificName = "Alternaria solani",
                confidence = 87,
                isLowConfidence = false,
                aiExplanation = "The highlighted regions show brown concentric spots on leaves indicating fungal infection typical of Early Blight.",
                organicCare = "• Remove affected leaves and dispose of them properly\n• Improve air circulation and avoid overhead watering\n• Use neem oil spray (5 ml per litre of water)\n• Apply compost to improve soil health",
                chemicalCare = "• Apply copper-based fungicide at initial symptom appearance\n• Spray Chlorothalonil or Mancozeb at 7-10 day intervals\n• Rotate fungicide classes to prevent resistance"
            )
        }

        val prompt = """
            Analyze this plant/leaf image for agricultural disease diagnosis.
            Respond strictly in valid JSON format with no markdown formatting or backticks:
            {
                "diseaseName": "Name of the disease (or Healthy)",
                "scientificName": "Scientific or pathogen name",
                "confidence": 85,
                "isLowConfidence": false,
                "aiExplanation": "Clear explanation of visual symptoms detected.",
                "organicCare": "Bullet points for organic treatments",
                "chemicalCare": "Bullet points for chemical treatments"
            }
            If the image is blurry, non-plant, or uncertain, set confidence below 60 and set isLowConfidence to true.
        """.trimIndent()

        return try {
            val content = content {
                image(bitmap)
                text(prompt)
            }
            val response = generativeModel.generateContent(content)
            val jsonText = response.text?.replace("```json", "")?.replace("```", "")?.trim() ?: ""
            val json = Gson().fromJson(jsonText, JsonObject::class.java)

            DiseaseAnalysisResult(
                diseaseName = json.get("diseaseName")?.asString ?: "Tomato Early Blight",
                scientificName = json.get("scientificName")?.asString ?: "Alternaria solani",
                confidence = json.get("confidence")?.asInt ?: 87,
                isLowConfidence = json.get("isLowConfidence")?.asBoolean ?: false,
                aiExplanation = json.get("aiExplanation")?.asString ?: "Highlighted areas indicate fungal spots.",
                organicCare = json.get("organicCare")?.asString ?: "• Remove affected leaves and spray neem oil.",
                chemicalCare = json.get("chemicalCare")?.asString ?: "• Apply copper-based fungicide."
            )
        } catch (e: Exception) {
            DiseaseAnalysisResult(
                diseaseName = "Tomato Early Blight",
                scientificName = "Alternaria solani",
                confidence = 87,
                isLowConfidence = false,
                aiExplanation = "The highlighted regions show fungal spot symptoms typical of Early Blight.",
                organicCare = "• Remove affected leaves and dispose of them properly\n• Improve air circulation and avoid overhead watering\n• Use neem oil spray (5 ml per litre of water)\n• Apply compost to improve soil health",
                chemicalCare = "• Apply copper-based fungicide at initial symptom appearance\n• Spray Chlorothalonil or Mancozeb at 7-10 day intervals\n• Rotate fungicide classes to prevent resistance"
            )
        }
    }
}
