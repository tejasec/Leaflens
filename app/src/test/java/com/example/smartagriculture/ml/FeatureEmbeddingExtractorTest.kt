package com.example.smartagriculture.ml

import android.graphics.Bitmap
import android.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.math.sqrt

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class FeatureEmbeddingExtractorTest {

    @Test
    fun testExtractEmbedding_returns128Dimensions() {
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val embedding = FeatureEmbeddingExtractor.extractEmbedding(bitmap)

        assertEquals(128, embedding.size)
    }

    @Test
    fun testExtractEmbedding_isL2Normalized() {
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(100 * 100) { Color.rgb(40, 160, 50) }
        bitmap.setPixels(pixels, 0, 100, 0, 0, 100, 100)

        val embedding = FeatureEmbeddingExtractor.extractEmbedding(bitmap)

        val norm = sqrt(embedding.sumOf { it * it })
        assertEquals(1.0, norm, 0.0001)
    }

    @Test
    fun testExtractEmbedding_identicalImages_haveCosineSimilarityNearOne() {
        val bitmap1 = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888)
        val pixels1 = IntArray(64 * 64) { Color.rgb(60, 140, 60) }
        bitmap1.setPixels(pixels1, 0, 64, 0, 0, 64, 64)

        val bitmap2 = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888)
        bitmap2.setPixels(pixels1, 0, 64, 0, 0, 64, 64)

        val emb1 = FeatureEmbeddingExtractor.extractEmbedding(bitmap1)
        val emb2 = FeatureEmbeddingExtractor.extractEmbedding(bitmap2)

        var dot = 0.0
        for (i in emb1.indices) {
            dot += emb1[i] * emb2[i]
        }

        assertEquals(1.0, dot, 0.001)
    }

    @Test
    fun testExtractEmbedding_distinctImages_haveDivergentEmbeddings() {
        val greenBitmap = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888)
        val greenPixels = IntArray(64 * 64) { Color.rgb(20, 180, 30) }
        greenBitmap.setPixels(greenPixels, 0, 64, 0, 0, 64, 64)

        val yellowBlightBitmap = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888)
        val yellowPixels = IntArray(64 * 64) { Color.rgb(220, 160, 20) }
        yellowBlightBitmap.setPixels(yellowPixels, 0, 64, 0, 0, 64, 64)

        val greenEmb = FeatureEmbeddingExtractor.extractEmbedding(greenBitmap)
        val yellowEmb = FeatureEmbeddingExtractor.extractEmbedding(yellowBlightBitmap)

        var dot = 0.0
        for (i in greenEmb.indices) {
            dot += greenEmb[i] * yellowEmb[i]
        }

        assertTrue("Expected similarity between green and yellow-spotted leaves to be < 0.95, got $dot", dot < 0.95)
    }
}
