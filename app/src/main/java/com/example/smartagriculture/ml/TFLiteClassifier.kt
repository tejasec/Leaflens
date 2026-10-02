package com.example.smartagriculture.ml

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import kotlin.math.exp

/**
 * Result model for local TensorFlow Lite classification inference.
 *
 * @property diseaseName Identified plant disease label (e.g., "Tomato - Early blight").
 * @property confidence Prediction confidence score between 0.0f and 1.0f (e.g., 0.85f = 85%).
 * @property isHighConfidence True if confidence >= 0.70 threshold required for local diagnosis.
 * @property rawProbabilities Output probability vector across all supported target classes.
 */
data class ClassResult(
    val diseaseName: String,
    val confidence: Float,
    val isHighConfidence: Boolean,
    val rawProbabilities: FloatArray = floatArrayOf()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as ClassResult
        if (diseaseName != other.diseaseName) return false
        if (confidence != other.confidence) return false
        if (isHighConfidence != other.isHighConfidence) return false
        if (!rawProbabilities.contentEquals(other.rawProbabilities)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = diseaseName.hashCode()
        result = 31 * result + confidence.hashCode()
        result = 31 * result + isHighConfidence.hashCode()
        result = 31 * result + rawProbabilities.contentHashCode()
        return result
    }
}

/**
 * On-Device TensorFlow Lite Engine for Crop Disease Classification.
 *
 * Loads MobileNetV2 architecture model (`crop_disease_model.tflite`), performs Float32 RGB
 * normalization to [0.0, 1.0], runs inference via TFLite Interpreter, and applies Softmax
 * post-processing to return [ClassResult].
 */
class TFLiteClassifier(private val context: Context) {

    private var interpreter: Interpreter? = null
    private var labels: List<String> = emptyList()

    companion object {
        const val MODEL_FILE_NAME = "crop_disease_model.tflite"
        private const val FALLBACK_MODEL_PATH = "models/mobilenet_v2_crop_quant.tflite"
        private const val LABELS_PATH = "models/labels.txt"

        const val INPUT_SIZE = 224
        private const val NUM_CHANNELS = 3
        const val CONFIDENCE_THRESHOLD = 0.70f // Threshold >= 0.70 for Local Diagnosis
        private const val BATCH_SIZE = 1
    }

    init {
        setupClassifier()
    }

    /**
     * Initializes TFLite interpreter and loads mapped model buffer & target label list.
     */
    private fun setupClassifier() {
        try {
            val modelBuffer = loadModelBuffer()
            val options = Interpreter.Options().apply {
                setNumThreads(4)
            }
            interpreter = Interpreter(modelBuffer, options)

            // Load target disease class labels
            labels = try {
                FileUtil.loadLabels(context, LABELS_PATH)
            } catch (_: Exception) {
                FileUtil.loadLabels(context, "labels.txt")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Loads `crop_disease_model.tflite` mapped byte buffer from app assets.
     */
    private fun loadModelBuffer(): ByteBuffer {
        return try {
            FileUtil.loadMappedFile(context, MODEL_FILE_NAME)
        } catch (_: Exception) {
            try {
                FileUtil.loadMappedFile(context, FALLBACK_MODEL_PATH)
            } catch (_: Exception) {
                val fileDescriptor = context.assets.openFd(MODEL_FILE_NAME)
                val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
                val fileChannel = inputStream.channel
                fileChannel.map(FileChannel.MapMode.READ_ONLY, fileDescriptor.startOffset, fileDescriptor.declaredLength)
            }
        }
    }

    /**
     * Executes local TFLite classification on input bitmap.
     *
     * Pre-processes bitmap: Resizes to 224x224, converts to Float32 RGB normalized to [0.0, 1.0].
     * Post-processes: Applies Softmax, extracts top disease label, confidence score, and checks threshold >= 0.70.
     *
     * @param bitmap Input crop leaf image.
     * @return [ClassResult] containing disease name, confidence score, and high-confidence flag.
     */
    fun classifyImage(bitmap: Bitmap): ClassResult {
        val currentInterpreter = interpreter ?: return ClassResult(
            diseaseName = "Unknown / Model Error",
            confidence = 0.0f,
            isHighConfidence = false
        )

        val numClasses = if (labels.isNotEmpty()) labels.size else 38

        // Step 1: Pre-process input Bitmap -> 224x224 Float32 RGB normalized to [0.0, 1.0]
        val inputBuffer = preprocessBitmapToFloatBuffer(bitmap)

        // Step 2: Prepare output tensor buffer
        val outputBuffer = Array(BATCH_SIZE) { FloatArray(numClasses) }

        // Step 3: Run TFLite inference
        currentInterpreter.run(inputBuffer, outputBuffer)

        val rawOutput = outputBuffer[0]

        // Step 4: Apply Softmax post-processing if output array is raw logits
        val probabilities = applySoftmax(rawOutput)

        // Step 5: Identify top predicted class label & confidence
        var maxIndex = 0
        var maxConfidence = -1.0f

        for (i in probabilities.indices) {
            if (probabilities[i] > maxConfidence) {
                maxConfidence = probabilities[i]
                maxIndex = i
            }
        }

        val rawLabel = if (maxIndex < labels.size) labels[maxIndex] else "Class $maxIndex"
        val formattedDiseaseName = cleanLabelName(rawLabel)
        val isHighConfidence = maxConfidence >= CONFIDENCE_THRESHOLD

        return ClassResult(
            diseaseName = formattedDiseaseName,
            confidence = maxConfidence,
            isHighConfidence = isHighConfidence,
            rawProbabilities = probabilities
        )
    }

    /**
     * Converts bitmap into Float32 ByteBuffer normalized to [0.0, 1.0] for shape [1, 224, 224, 3].
     */
    private fun preprocessBitmapToFloatBuffer(bitmap: Bitmap): ByteBuffer {
        val safeBitmap = if (bitmap.config == Bitmap.Config.HARDWARE) {
            bitmap.copy(Bitmap.Config.ARGB_8888, false)
        } else {
            bitmap
        }

        // Resize bitmap to 224x224 RGB
        val resizedBitmap = Bitmap.createScaledBitmap(safeBitmap, INPUT_SIZE, INPUT_SIZE, true)

        val byteBuffer = ByteBuffer.allocateDirect(BATCH_SIZE * INPUT_SIZE * INPUT_SIZE * NUM_CHANNELS * 4) // 4 bytes per Float32
        byteBuffer.order(ByteOrder.nativeOrder())

        val intPixels = IntArray(INPUT_SIZE * INPUT_SIZE)
        resizedBitmap.getPixels(intPixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE)

        val totalPixels = INPUT_SIZE * INPUT_SIZE
        for (index in 0 until totalPixels) {
            val pixel = intPixels[index]
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF

            // Float32 RGB Normalization to [0.0, 1.0]
            byteBuffer.putFloat(r / 255.0f)
            byteBuffer.putFloat(g / 255.0f)
            byteBuffer.putFloat(b / 255.0f)
        }
        return byteBuffer
    }

    /**
     * Applies Softmax normalization: P_i = exp(z_i - max(z)) / sum(exp(z_j - max(z))).
     */
    private fun applySoftmax(logits: FloatArray): FloatArray {
        var maxLogit = Float.NEGATIVE_INFINITY
        for (logit in logits) {
            if (logit > maxLogit) maxLogit = logit
        }

        var sumExp = 0.0
        val expValues = FloatArray(logits.size)
        for (i in logits.indices) {
            val expVal = exp((logits[i] - maxLogit).toDouble()).toFloat()
            expValues[i] = expVal
            sumExp += expVal.toDouble()
        }

        val probabilities = FloatArray(logits.size)
        val denominator = if (sumExp > 0.0) sumExp else 1.0
        for (i in expValues.indices) {
            probabilities[i] = (expValues[i] / denominator).toFloat()
        }
        return probabilities
    }

    /**
     * Cleans raw label string (e.g. "Tomato___Early_blight" -> "Tomato - Early blight").
     */
    private fun cleanLabelName(rawLabel: String): String {
        return rawLabel
            .replace("___", " - ")
            .replace("_", " ")
            .trim()
    }

    /**
     * Releases TFLite interpreter native resources.
     */
    fun close() {
        interpreter?.close()
        interpreter = null
    }
}
