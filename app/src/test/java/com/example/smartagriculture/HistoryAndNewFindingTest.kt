package com.example.smartagriculture

import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.smartagriculture.database.AppDatabase
import com.example.smartagriculture.model.ChatMessage
import com.example.smartagriculture.model.DiseaseAnalysisResult
import com.example.smartagriculture.model.ScanHistoryItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class HistoryAndNewFindingTest {

    @Test
    fun testScanHistoryItem_supportsNewFindingStatus() {
        val item = ScanHistoryItem(
            id = 1L,
            imagePath = "/path/to/mango.jpg",
            cropName = "Mango",
            diseaseName = "Anthracnose",
            status = "New Finding",
            scientificName = "Colletotrichum gloeosporioides",
            confidence = 88,
            isLowConfidence = false,
            aiExplanation = "Detected Anthracnose lesions on Mango leaf.",
            organicCare = "Prune infected leaves",
            chemicalCare = "Copper fungicide",
            chatHistoryJson = null
        )

        assertEquals("New Finding", item.status)
        assertEquals("Mango", item.cropName)
        assertEquals("Anthracnose", item.diseaseName)
    }

    @Test
    fun testDiseaseAnalysisResult_withNewFindingAndCropName() {
        val result = DiseaseAnalysisResult(
            cropName = "Papaya",
            diseaseName = "Ringspot Virus",
            scientificName = "Papaya ringspot potyvirus",
            confidence = 85,
            isLowConfidence = false,
            isNewFinding = true,
            aiExplanation = "Identified Papaya foliage with circular chlorotic rings.",
            organicCare = "Isolate infected plants",
            chemicalCare = "No chemical cure for virus; vector control with neem oil"
        )

        assertEquals("Papaya", result.cropName)
        assertEquals("Ringspot Virus", result.diseaseName)
        assertTrue(result.isNewFinding)

        val copy = result.copy(isNewFinding = false)
        assertFalse(copy.isNewFinding)
        assertEquals("Papaya", copy.cropName)
    }

    @Test
    fun testChatMessage_serializationAndDeserialization() {
        val messages = listOf(
            ChatMessage("Hello! Diagnosed Mango Anthracnose.", isUser = false, timestamp = 1000L),
            ChatMessage("What should I spray?", isUser = true, timestamp = 2000L),
            ChatMessage("Apply copper-based fungicide at 10-14 day intervals.", isUser = false, timestamp = 3000L)
        )

        val gson = Gson()
        val json = gson.toJson(messages)
        assertNotNull(json)
        assertTrue(json.contains("Mango Anthracnose"))
        assertTrue(json.contains("copper-based fungicide"))

        val type = object : TypeToken<List<ChatMessage>>() {}.type
        val restored: List<ChatMessage> = gson.fromJson(json, type)
        assertEquals(3, restored.size)
        assertEquals("What should I spray?", restored[1].message)
        assertTrue(restored[1].isUser)
        assertFalse(restored[2].isUser)
    }

    @Test
    fun testHistoryFilterMatching_allFourStatuses() {
        val scans = listOf(
            ScanHistoryItem(id = 1, imagePath = "", cropName = "Tomato", diseaseName = "Healthy", status = "Healthy", scientificName = "", confidence = 95, isLowConfidence = false, aiExplanation = "", organicCare = "", chemicalCare = ""),
            ScanHistoryItem(id = 2, imagePath = "", cropName = "Tomato", diseaseName = "Early blight", status = "Diseased", scientificName = "", confidence = 85, isLowConfidence = false, aiExplanation = "", organicCare = "", chemicalCare = ""),
            ScanHistoryItem(id = 3, imagePath = "", cropName = "Unknown", diseaseName = "Foliar Spot", status = "Uncertain", scientificName = "", confidence = 45, isLowConfidence = true, aiExplanation = "", organicCare = "", chemicalCare = ""),
            ScanHistoryItem(id = 4, imagePath = "", cropName = "Cotton", diseaseName = "Bacterial Blight", status = "New Finding", scientificName = "", confidence = 90, isLowConfidence = false, aiExplanation = "", organicCare = "", chemicalCare = "")
        )

        // Filter: All
        val allFiltered = scans.filter { true }
        assertEquals(4, allFiltered.size)

        // Filter: Healthy
        val healthyFiltered = scans.filter { it.status.equals("Healthy", ignoreCase = true) }
        assertEquals(1, healthyFiltered.size)
        assertEquals("Tomato", healthyFiltered[0].cropName)

        // Filter: Diseased
        val diseasedFiltered = scans.filter { it.status.equals("Diseased", ignoreCase = true) }
        assertEquals(1, diseasedFiltered.size)
        assertEquals("Early blight", diseasedFiltered[0].diseaseName)

        // Filter: Uncertain
        val uncertainFiltered = scans.filter { it.status.equals("Uncertain", ignoreCase = true) }
        assertEquals(1, uncertainFiltered.size)
        assertEquals(45, uncertainFiltered[0].confidence)

        // Filter: New Finding
        val newFindingFiltered = scans.filter { it.status.equals("New Finding", ignoreCase = true) }
        assertEquals(1, newFindingFiltered.size)
        assertEquals("Cotton", newFindingFiltered[0].cropName)
        assertEquals("Bacterial Blight", newFindingFiltered[0].diseaseName)
    }

    @Test
    fun testRoomMigration_MIGRATION_5_6_altersTable() {
        val mockDb = mock(SupportSQLiteDatabase::class.java)
        AppDatabase.MIGRATION_5_6.migrate(mockDb)

        verify(mockDb).execSQL("ALTER TABLE scan_history ADD COLUMN chatHistoryJson TEXT DEFAULT NULL")
    }
}
