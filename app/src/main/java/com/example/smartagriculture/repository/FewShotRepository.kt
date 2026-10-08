package com.example.smartagriculture.repository

import com.example.smartagriculture.database.PrototypeDao
import com.example.smartagriculture.database.PrototypeEntity
import kotlin.math.sqrt

/**
 * Result model for Few-Shot prototype vector match.
 */
data class PrototypeMatch(
    val className: String,
    val similarityScore: Double,
    val prototypeId: Long,
)

/**
 * On-device Few-Shot Adaptation Repository using local vector persistence (MOD-04, Feature 6).
 */
class FewShotRepository(private val prototypeDao: PrototypeDao) {

    /**
     * Enrolls a new class prototype by calculating the centroid mean vector across 3 to 5 reference sample embeddings:
     * c = (1/N) * sum(z_i), normalized to ||c||_2 = 1.0.
     * Persists to local Room DB and returns the generated prototype ID.
     */
    suspend fun enrollPathogen(
        className: String,
        sampleEmbeddings: List<List<Double>>,
    ): Long {
        require(className.isNotBlank()) { "Class name cannot be blank." }
        require(sampleEmbeddings.size in 3..5) { "Enrollment requires between 3 and 5 sample photos." }
        val dim = sampleEmbeddings[0].size
        require(dim > 0) { "Embedding dimension must be greater than zero." }
        require(sampleEmbeddings.all { it.size == dim }) { "All sample embeddings must have matching dimensions." }

        val centroid = DoubleArray(dim)
        for (embedding in sampleEmbeddings) {
            for (i in 0 until dim) {
                centroid[i] += embedding[i] / sampleEmbeddings.size
            }
        }

        val normalizedCentroid = normalizeVector(centroid.toList())
        return savePrototype(className.trim(), normalizedCentroid)
    }

    /**
     * Normalizes an N-dimensional vector to unit Euclidean length (||v|| = 1.0).
     */
    fun normalizeVector(vector: List<Double>): List<Double> {
        var sumSquares = 0.0
        for (v in vector) {
            sumSquares += v * v
        }
        val norm = sqrt(sumSquares)
        if (norm == 0.0) return vector
        return vector.map { it / norm }
    }

    /**
     * Enrolls a new class prototype into local Room storage.
     */
    suspend fun savePrototype(className: String, embedding: List<Double>): Long {
        require(embedding.isNotEmpty()) { "Embedding vector cannot be empty." }
        val entity = PrototypeEntity(
            className = className,
            embedding = embedding,
        )
        return prototypeDao.insertPrototype(entity)
    }

    /**
     * Classifies a sample 128-dimensional embedding against all stored prototypes using Cosine Similarity.
     * Returns closest match if similarity exceeds threshold (default 0.85); otherwise returns null.
     */
    suspend fun classifyFewShot(
        sampleEmbedding: List<Double>,
        threshold: Double = 0.85,
    ): PrototypeMatch? {
        if (sampleEmbedding.isEmpty()) return null

        val storedPrototypes = prototypeDao.getAllPrototypes()
        if (storedPrototypes.isEmpty()) return null

        var bestMatch: PrototypeMatch? = null
        var maxSimilarity = -1.0

        for (prototype in storedPrototypes) {
            val similarity = computeCosineSimilarity(sampleEmbedding, prototype.embedding)
            if (similarity > maxSimilarity) {
                maxSimilarity = similarity
                bestMatch = PrototypeMatch(
                    className = prototype.className,
                    similarityScore = similarity,
                    prototypeId = prototype.id,
                )
            }
        }

        return if (maxSimilarity >= threshold) {
            bestMatch
        } else {
            null
        }
    }

    /**
     * Computes Cosine Similarity between two N-dimensional embedding vectors:
     * CosineSim(A, B) = (A dot B) / (||A|| * ||B||)
     */
    fun computeCosineSimilarity(vectorA: List<Double>, vectorB: List<Double>): Double {
        val dim = minOf(vectorA.size, vectorB.size)
        if (dim == 0) return 0.0

        var dotProduct = 0.0
        var normA = 0.0
        var normB = 0.0

        for (i in 0 until dim) {
            val a = vectorA[i]
            val b = vectorB[i]
            dotProduct += a * b
            normA += a * a
            normB += b * b
        }

        val denominator = sqrt(normA) * sqrt(normB)
        if (denominator == 0.0) return 0.0

        return dotProduct / denominator
    }

    suspend fun getAllPrototypes(): List<PrototypeEntity> {
        return prototypeDao.getAllPrototypes()
    }

    suspend fun deletePrototypesByClass(className: String) {
        prototypeDao.deletePrototypesByClass(className)
    }
}
