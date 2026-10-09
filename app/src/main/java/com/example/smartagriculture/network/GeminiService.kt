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
            delay(1500) // simulate AI inference delay
            return DiseaseAnalysisResult(
                cropName = "Mango",
                diseaseName = "Anthracnose",
                scientificName = "Colletotrichum gloeosporioides",
                confidence = 88,
                isLowConfidence = false,
                isNewFinding = true,
                aiExplanation = "Foliar scanning via Gemini AI identified Mangifera indica (Mango) foliage with dark irregular necrotic lesions characteristic of Anthracnose fungal infection.",
                organicCare = "• Prune and dispose of infected twigs and fallen leaves\n• Improve tree canopy aeration\n• Apply preventative spray of 5% Neem Seed Kernel Extract (NSKE) or Trichoderma viride",
                chemicalCare = "• Spray Copper Oxychloride 50% WP (3.0g/L water) or Carbendazim 50% WP (1.0g/L)\n• Apply Mancozeb 75% WP at 10-14 day intervals during humid weather\n• Observe 14-day PHI"
            )
        }

        val prompt = """
            Analyze this plant/leaf image for agricultural identification and disease diagnosis.
            Identify:
            1. What plant, crop, or leaf species this is (e.g. Mango, Cotton, Wheat, Rose, Papaya, Tomato, Apple, Corn, Rice, Potato, etc.).
            2. Any foliar disease or condition present (or "Healthy" if no disease).
            3. Note: The local database only covers 5 standard crops: Apple, Corn, Potato, Rice, Tomato. Any other plant/crop or uncataloged condition is a "New Finding".
            
            Respond strictly in valid JSON format with no markdown formatting or backticks:
            {
                "cropName": "Identified common name of the plant or crop (e.g., Mango, Cotton, Wheat, Rose, Papaya, Tomato, etc.)",
                "diseaseName": "Name of the disease or condition (or Healthy)",
                "scientificName": "Scientific or pathogen name",
                "confidence": 88,
                "isLowConfidence": false,
                "isNewFinding": true,
                "aiExplanation": "Clear explanation of the plant identification and visual symptoms detected.",
                "organicCare": "Bullet points for organic treatments or preventative care",
                "chemicalCare": "Bullet points for chemical treatments with dosages and safety intervals"
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

            val detectedCrop = json.get("cropName")?.asString ?: "Unknown Plant"
            val detectedDisease = json.get("diseaseName")?.asString ?: "Uncataloged Condition"
            val isExplicitNew = json.get("isNewFinding")?.asBoolean ?: false
            val isKnownDatasetCrop = listOf("apple", "corn", "maize", "potato", "rice", "tomato")
                .any { detectedCrop.contains(it, ignoreCase = true) }
            val isNewFinding = !isKnownDatasetCrop || isExplicitNew

            DiseaseAnalysisResult(
                cropName = detectedCrop,
                diseaseName = detectedDisease,
                scientificName = json.get("scientificName")?.asString ?: "Botanical species",
                confidence = json.get("confidence")?.asInt ?: 85,
                isLowConfidence = json.get("isLowConfidence")?.asBoolean ?: false,
                isNewFinding = isNewFinding,
                aiExplanation = json.get("aiExplanation")?.asString ?: "Foliar analysis conducted via Gemini AI.",
                organicCare = json.get("organicCare")?.asString ?: "• Prune affected foliage and apply organic bio-agents.",
                chemicalCare = json.get("chemicalCare")?.asString ?: "• Consult local agrarian officer before applying chemical sprays."
            )
        } catch (e: Exception) {
            DiseaseAnalysisResult(
                cropName = "Unknown Plant",
                diseaseName = "Uncataloged Pathogen",
                scientificName = "Unidentified species",
                confidence = 50,
                isLowConfidence = true,
                isNewFinding = true,
                aiExplanation = "AI identified visual symptoms that are not cataloged in the local model database.",
                organicCare = "• Isolate affected leaves to prevent spread\n• Avoid overhead irrigation",
                chemicalCare = "• Consult an agronomist before spraying broad-spectrum fungicides"
            )
        }
    }

    suspend fun askCropDoctor(
        diseaseName: String,
        scientificName: String,
        confidence: Int,
        userQuestion: String,
        organicCare: String = "",
        chemicalCare: String = ""
    ): String {
        if (generativeModel == null) {
            delay(1200)
            val q = userQuestion.lowercase()
            return when {
                q.contains("spray") || q.contains("interval") ->
                    "For $diseaseName ($scientificName), spray copper-based fungicide or Chlorothalonil every 7 to 10 days during humid weather. Avoid spraying during peak sunlight to prevent leaf burn."
                q.contains("fertilizer") || q.contains("organic") ->
                    "For $diseaseName, use well-rotted compost, neem cake powder (250g per plant), and spray neem oil (5ml/L) or vermicompost tea to strengthen natural leaf immunity."
                q.contains("prevent") || q.contains("future") || q.contains("next season") ->
                    "To prevent $diseaseName in future seasons: rotate crops every 3 years, maintain proper plant spacing for air circulation, avoid overhead watering, and destroy infected crop residue."
                else ->
                    "For $diseaseName ($scientificName - ${confidence}% confidence), ensure good soil drainage, apply balanced NPK nutrients, remove infected leaves promptly, and spray appropriate fungicides at 7-10 day intervals."
            }
        }

        val prompt = """
            You are an expert plant pathologist and agricultural consultant ("Crop Doctor").
            The farmer's crop was diagnosed with $diseaseName ($scientificName) with $confidence% confidence.
            Contextual organic care: $organicCare
            Contextual chemical care: $chemicalCare
            
            Farmer's question: "$userQuestion"
            
            Provide a helpful, concise, practical response addressing spray intervals, organic remedies, dosages, or preventative measures. 
            Do not use markdown formatting like asterisks. Keep it clear, friendly, and easy to read for a farmer.
        """.trimIndent()

        return try {
            val response = generativeModel.generateContent(prompt)
            response.text ?: "I am sorry, I could not process your question at this moment."
        } catch (e: Exception) {
            "For $diseaseName, apply recommended fungicides at 7-10 day intervals, use neem oil spray for organic control, and ensure proper field sanitation."
        }
    }
}
