package com.example.smartagriculture.ml

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.util.Locale
import kotlin.math.exp

/**
 * Diagnosis prediction data class representing top differential diagnosis item.
 */
data class Diagnosis(
    val label: String,
    val confidence: Float,
    val formattedPercentage: String,
)

/**
 * Comprehensive classification result containing top-3 differential diagnoses and AI guardrail flags.
 */
data class ClassificationResult(
    val predictions: List<Diagnosis>,
    val topConfidence: Float,
    val isUncertain: Boolean,
    val feedbackMessage: String,
)

/**
 * TFLite Classifier for Edge Crop Health Diagnosis (MOD-02 & MOD-06).
 * Handles MobileNetV2 INT8 quantized model inference with Temperature Scaling (T = 1.35)
 * and Responsible AI confidence guardrails (<70% threshold).
 */
class CropHealthClassifier(private val context: Context) {

    private var interpreter: Interpreter? = null
    private var labels: List<String> = emptyList()

    companion object {
        private const val MODEL_PATH = "models/mobilenet_v2_crop_quant.tflite"
        private const val LABELS_PATH = "models/labels.txt"

        private const val INPUT_SIZE = 224
        private const val NUM_CHANNELS = 3
        private const val TEMPERATURE = 1.35f
        private const val CONFIDENCE_GUARDRAIL_THRESHOLD = 0.70f

        // MobileNetV2 INT8 input tensor dimensions: [1, 224, 224, 3]
        private const val BATCH_SIZE = 1
    }

    init {
        setupClassifier()
    }

    private fun setupClassifier() {
        try {
            val modelBuffer = loadModelFile(MODEL_PATH)
            val options = Interpreter.Options().apply {
                setNumThreads(4)
            }
            interpreter = Interpreter(modelBuffer, options)
            labels = FileUtil.loadLabels(context, LABELS_PATH)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Loads TFLite model from assets folder as MappedByteBuffer.
     */
    private fun loadModelFile(modelPath: String): ByteBuffer {
        val fileDescriptor = context.assets.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    /**
     * Runs inference on the input bitmap and returns top-3 calibrated differential diagnoses.
     */
    fun classifyImage(bitmap: Bitmap): ClassificationResult {
        val currentInterpreter = interpreter ?: return ClassificationResult(
            predictions = emptyList(),
            topConfidence = 0.0f,
            isUncertain = true,
            feedbackMessage = "Classifier initialization failed or model not found.",
        )

        // 1. Image Resizing & Normalization matching MobileNetV2 standards
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true)
        val inputBuffer = convertBitmapToByteBuffer(resizedBitmap)

        // Determine output tensor shape and type from model metadata
        val numClasses = if (labels.isNotEmpty()) labels.size else 38
        val outputTensor = currentInterpreter.getOutputTensor(0)
        val outputDataType = outputTensor.dataType()

        // Buffer to receive raw output logits
        val rawLogits = FloatArray(numClasses)

        if ((outputDataType == DataType.INT8) || (outputDataType == DataType.UINT8)) {
            val outputBuffer = Array(BATCH_SIZE) { ByteArray(numClasses) }
            currentInterpreter.run(inputBuffer, outputBuffer)

            val quantizationParams = outputTensor.quantizationParams()
            val scale = if (quantizationParams.scale != 0f) quantizationParams.scale else 1.0f
            val zeroPoint = quantizationParams.zeroPoint

            // Dequantize INT8 output tensor to float logits: z = (q - zeroPoint) * scale
            for (i in 0 until numClasses) {
                val quantVal = outputBuffer[0][i].toInt()
                rawLogits[i] = (quantVal - zeroPoint) * scale
            }
        } else {
            val outputBuffer = Array(BATCH_SIZE) { FloatArray(numClasses) }
            currentInterpreter.run(inputBuffer, outputBuffer)
            for (i in 0 until numClasses) {
                rawLogits[i] = outputBuffer[0][i]
            }
        }

        // 2. Apply Temperature Scaling (T = 1.35) before softmax
        val calibratedProbabilities = applyTemperatureSoftmax(rawLogits, TEMPERATURE)

        // 4. Rank and extract top-3 differential diagnoses
        val indexedProbabilities = calibratedProbabilities.mapIndexed { index, prob ->
            val labelName = if (index < labels.size) labels[index] else "Class $index"
            Pair(cleanLabelName(labelName), prob)
        }.sortedByDescending { it.second }

        val top3Diagnoses = indexedProbabilities.take(3).map { (label, confidence) ->
            Diagnosis(
                label = label,
                confidence = confidence,
                formattedPercentage = String.format(Locale.US, "%.1f%%", confidence * 100f),
            )
        }

        val topConfidence = if (top3Diagnoses.isNotEmpty()) top3Diagnoses[0].confidence else 0.0f

        // 3. Responsible AI Guardrail: Flag predictions with top confidence < 0.70
        val isUncertain = topConfidence < CONFIDENCE_GUARDRAIL_THRESHOLD
        val feedbackMessage = if (isUncertain) {
            "Uncertain diagnosis: Top confidence score is below 70% (${String.format(Locale.US, "%.1f%%", topConfidence * 100f)}). Re-scan leaf under better lighting or consult an agronomist."
        } else {
            "Diagnosis confident (${String.format(Locale.US, "%.1f%%", topConfidence * 100f)})."
        }

        return ClassificationResult(
            predictions = top3Diagnoses,
            topConfidence = topConfidence,
            isUncertain = isUncertain,
            feedbackMessage = feedbackMessage,
        )
    }

    /**
     * Preprocesses Bitmap into INT8 DirectByteBuffer matching MobileNetV2 input shape [1, 224, 224, 3].
     */
    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val safeBitmap = if (bitmap.config == Bitmap.Config.HARDWARE) {
            bitmap.copy(Bitmap.Config.ARGB_8888, false)
        } else {
            bitmap
        }

        val byteBuffer = ByteBuffer.allocateDirect(BATCH_SIZE * INPUT_SIZE * INPUT_SIZE * NUM_CHANNELS)
        byteBuffer.order(ByteOrder.nativeOrder())

        val intValues = IntArray(INPUT_SIZE * INPUT_SIZE)
        safeBitmap.getPixels(intValues, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE)

        var pixelIndex = 0
        for (i in 0 until INPUT_SIZE) {
            for (j in 0 until INPUT_SIZE) {
                val pixel = intValues[pixelIndex++]
                val r = (pixel shr 16) and 0xFF
                val g = (pixel shr 8) and 0xFF
                val b = pixel and 0xFF

                // MobileNetV2 INT8 mapping: [0..255] -> [-128..127]
                byteBuffer.put((r - 128).toByte())
                byteBuffer.put((g - 128).toByte())
                byteBuffer.put((b - 128).toByte())
            }
        }
        return byteBuffer
    }

    /**
     * Applies Temperature Scaling z_i / T followed by Softmax:
     * P_i = exp((z_i / T) - max) / sum(exp((z_j / T) - max))
     */
    private fun applyTemperatureSoftmax(logits: FloatArray, temperature: Float): FloatArray {
        val scaledLogits = FloatArray(logits.size)
        var maxLogit = Float.NEGATIVE_INFINITY

        for (i in logits.indices) {
            val scaled = logits[i] / temperature
            scaledLogits[i] = scaled
            if (scaled > maxLogit) {
                maxLogit = scaled
            }
        }

        var sumExp = 0.0
        val expValues = DoubleArray(logits.size)
        for (i in scaledLogits.indices) {
            val expVal = exp((scaledLogits[i] - maxLogit).toDouble())
            expValues[i] = expVal
            sumExp += expVal
        }

        val probabilities = FloatArray(logits.size)
        for (i in expValues.indices) {
            probabilities[i] = (expValues[i] / sumExp).toFloat()
        }
        return probabilities
    }

    private fun cleanLabelName(rawLabel: String): String {
        return rawLabel.replace("___", " - ").replace("_", " ").trim()
    }

    fun close() {
        interpreter?.close()
        interpreter = null
    }
}
