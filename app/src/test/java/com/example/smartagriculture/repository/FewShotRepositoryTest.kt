package com.example.smartagriculture.repository

import com.example.smartagriculture.database.PrototypeDao
import com.example.smartagriculture.database.PrototypeEntity
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

class FewShotRepositoryTest {

    private lateinit var prototypeDao: PrototypeDao
    private lateinit var repository: FewShotRepository

    @Before
    fun setUp() {
        prototypeDao = mock(PrototypeDao::class.java)
        repository = FewShotRepository(prototypeDao)
    }

    @Test
    fun testComputeCosineSimilarity_identicalVectors_returnsOne() {
        val vec1 = listOf(0.5, 0.5, 0.5, 0.5)
        val vec2 = listOf(0.5, 0.5, 0.5, 0.5)

        val similarity = repository.computeCosineSimilarity(vec1, vec2)

        assertEquals(1.0, similarity, 0.0001)
    }

    @Test
    fun testComputeCosineSimilarity_orthogonalVectors_returnsZero() {
        val vec1 = listOf(1.0, 0.0, 0.0)
        val vec2 = listOf(0.0, 1.0, 0.0)

        val similarity = repository.computeCosineSimilarity(vec1, vec2)

        assertEquals(0.0, similarity, 0.0001)
    }

    @Test
    fun testComputeCosineSimilarity_vectorNormalizationAndScaling_returnsOne() {
        // Scaled vectors have same direction; cosine similarity normalizes length via magnitudes
        val unitVector = listOf(0.6, 0.8, 0.0)
        val scaledVector = listOf(6.0, 8.0, 0.0)

        val similarity = repository.computeCosineSimilarity(unitVector, scaledVector)

        assertEquals(1.0, similarity, 0.0001)
    }

    @Test
    fun testCosineDistance_identicalVectors_returnsZero() {
        val vec1 = listOf(0.5, 0.5, 0.5, 0.5)
        val vec2 = listOf(0.5, 0.5, 0.5, 0.5)

        val similarity = repository.computeCosineSimilarity(vec1, vec2)
        val distance = 1.0 - similarity

        assertEquals(0.0, distance, 0.0001)
    }

    @Test
    fun testCosineDistance_orthogonalVectors_returnsOne() {
        val vec1 = listOf(1.0, 0.0, 0.0)
        val vec2 = listOf(0.0, 1.0, 0.0)

        val similarity = repository.computeCosineSimilarity(vec1, vec2)
        val distance = 1.0 - similarity

        assertEquals(1.0, distance, 0.0001)
    }

    @Test
    fun testClassifyFewShot_matchingPrototypeAboveThreshold_returnsBestMatch() = runTest {
        val prototypes = listOf(
            PrototypeEntity(
                id = 101L,
                className = "Tomato - Early blight",
                embedding = listOf(1.0, 0.0, 0.0),
            ),
            PrototypeEntity(
                id = 102L,
                className = "Potato - Late blight",
                embedding = listOf(0.0, 1.0, 0.0),
            ),
        )
        whenever(prototypeDao.getAllPrototypes()).thenReturn(prototypes)

        // Query vector close to Tomato Early Blight (1.0, 0.0, 0.0)
        val sampleEmbedding = listOf(0.98, 0.02, 0.0)
        val match = repository.classifyFewShot(sampleEmbedding, threshold = 0.85)

        assertNotNull(match)
        assertEquals("Tomato - Early blight", match?.className)
        assertEquals(101L, match?.prototypeId)
        assertEquals(0.98, match?.similarityScore ?: 0.0, 0.05)
    }

    @Test
    fun testClassifyFewShot_belowThreshold_returnsNull() = runTest {
        val prototypes = listOf(
            PrototypeEntity(
                id = 101L,
                className = "Tomato - Early blight",
                embedding = listOf(1.0, 0.0, 0.0),
            ),
        )
        whenever(prototypeDao.getAllPrototypes()).thenReturn(prototypes)

        // Query vector with low similarity (~0.577) to prototype
        val sampleEmbedding = listOf(0.577, 0.577, 0.577)
        val match = repository.classifyFewShot(sampleEmbedding, threshold = 0.85)

        assertNull(match)
    }

    @Test
    fun testSavePrototype_insertsIntoDao() = runTest {
        whenever(prototypeDao.insertPrototype(any())).thenReturn(1L)

        val id = repository.savePrototype("Corn - Common rust", listOf(0.1, 0.2, 0.3))

        assertEquals(1L, id)
        verify(prototypeDao).insertPrototype(any())
    }

    @Test
    fun testEnrollPathogen_valid3Shots_computesNormalizedCentroidAndSaves() = runTest {
        whenever(prototypeDao.insertPrototype(any())).thenReturn(42L)

        val shot1 = listOf(1.0, 0.0, 0.0)
        val shot2 = listOf(0.8, 0.2, 0.0)
        val shot3 = listOf(0.9, 0.1, 0.0)

        val id = repository.enrollPathogen("Tomato - Novel Mosaic", listOf(shot1, shot2, shot3))

        assertEquals(42L, id)
        verify(prototypeDao).insertPrototype(org.mockito.kotlin.check { entity ->
            assertEquals("Tomato - Novel Mosaic", entity.className)
            assertEquals(3, entity.embedding.size)
            // L2 norm must be 1.0
            val norm = Math.sqrt(entity.embedding.sumOf { it * it })
            assertEquals(1.0, norm, 0.0001)
        })
    }

    @Test(expected = IllegalArgumentException::class)
    fun testEnrollPathogen_fewerThan3Shots_throwsException() = runTest {
        val shot1 = listOf(1.0, 0.0, 0.0)
        val shot2 = listOf(0.8, 0.2, 0.0)
        repository.enrollPathogen("Tomato - Novel Mosaic", listOf(shot1, shot2))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testEnrollPathogen_moreThan5Shots_throwsException() = runTest {
        val shot = listOf(1.0, 0.0, 0.0)
        repository.enrollPathogen("Tomato - Novel Mosaic", listOf(shot, shot, shot, shot, shot, shot))
    }
}

