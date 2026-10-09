package com.example.smartagriculture.ml

import android.content.Context
import android.util.Log
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil

data class ModelVerificationResult(
    val isAssetLoaded: Boolean,
    val isInputShapeValid: Boolean,
    val isOutputShapeValid: Boolean,
    val isLabelsLoaded: Boolean,
    val verificationSummary: String
)

/**
 * MobileNetV2 On-Device Implementation Verification Protocol.
 *
 * Audits:
 * 1. Asset existence (mobilenet_v2_crop_quant.tflite or fallback)
 * 2. Input tensor bounds ([1, 224, 224, 3])
 * 3. Output tensor dimensions (38 classes or matching labels length)
 * 4. Labels existence and parsing
 *
 * Logs status under Logcat tag "MobileNetV2Status".
 */
object MobileNetV2Verifier {

    private const val TAG = "MobileNetV2Status"

    private val MODEL_CANDIDATE_PATHS = listOf(
        "models/mobilenet_v2_crop_quant.tflite",
        "crop_disease_model.tflite"
    )

    fun verifyImplementation(context: Context): ModelVerificationResult {
        var interpreter: Interpreter? = null
        var assetLoaded = false
        var loadedModelPath = ""

        for (path in MODEL_CANDIDATE_PATHS) {
            try {
                val modelBuffer = FileUtil.loadMappedFile(context, path)
                val options = Interpreter.Options().apply {
                    setNumThreads(2)
                }
                interpreter = Interpreter(modelBuffer, options)
                assetLoaded = true
                loadedModelPath = path
                break
            } catch (_: Throwable) {
                // Try next path
            }
        }

        val labels = try {
            FileUtil.loadLabels(context, "models/labels.txt")
        } catch (_: Throwable) {
            try {
                FileUtil.loadLabels(context, "labels.txt")
            } catch (_: Throwable) {
                emptyList()
            }
        }
        val labelsLoaded = labels.isNotEmpty()

        var inputShapeValid = false
        var outputShapeValid = false

        interpreter?.let { tflite ->
            try {
                val inputTensor = tflite.getInputTensor(0)
                val outputTensor = tflite.getOutputTensor(0)

                val inputShape = inputTensor.shape() // Expected: [1, 224, 224, 3]
                val outputShape = outputTensor.shape() // Expected: [1, 38]

                inputShapeValid = inputShape.size == 4 &&
                        inputShape[0] == 1 &&
                        inputShape[1] == 224 &&
                        inputShape[2] == 224 &&
                        inputShape[3] == 3

                val expectedClasses = if (labels.isNotEmpty()) labels.size else 38
                outputShapeValid = outputShape.size == 2 &&
                        outputShape[0] == 1 &&
                        outputShape[1] == expectedClasses
            } catch (e: Throwable) {
                Log.e(TAG, "Error inspecting tensor shapes", e)
            } finally {
                try {
                    tflite.close()
                } catch (_: Throwable) {}
            }
        }

        val isFullyImplemented = assetLoaded && labelsLoaded && inputShapeValid && outputShapeValid
        val summary = if (isFullyImplemented) {
            "✅ MobileNetV2 fully implemented and operational on-device ($loadedModelPath, ${labels.size} classes, input [1, 224, 224, 3])."
        } else {
            "⚠️ Verification Status: AssetLoaded=$assetLoaded, LabelsLoaded=$labelsLoaded (${labels.size} items), InputShapeValid=$inputShapeValid, OutputShapeValid=$outputShapeValid"
        }

        Log.i(TAG, summary)

        return ModelVerificationResult(
            isAssetLoaded = assetLoaded,
            isInputShapeValid = inputShapeValid,
            isOutputShapeValid = outputShapeValid,
            isLabelsLoaded = labelsLoaded,
            verificationSummary = summary
        )
    }
}
