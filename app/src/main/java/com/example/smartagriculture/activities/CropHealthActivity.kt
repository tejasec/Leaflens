package com.example.smartagriculture.activities

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.smartagriculture.databinding.ActivityMainBinding
import com.example.smartagriculture.viewmodel.CropHealthUiState
import com.example.smartagriculture.viewmodel.CropHealthViewModel
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Example Production Activity demonstrating CameraX frame capture feeding directly into
 * [CropHealthViewModel] for hybrid offline-first crop disease analysis.
 */
class CropHealthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: CropHealthViewModel by viewModels()

    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startCamera()
        } else {
            Toast.makeText(this, "Camera permission required for leaf scanning.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemStatusBar()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraExecutor = Executors.newSingleThreadExecutor()

        checkPermissionsAndStartCamera()
        observeViewModelState()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemStatusBar()
        }
    }

    private fun hideSystemStatusBar() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.statusBars())
    }

    private fun checkPermissionsAndStartCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build()

                imageCapture = ImageCapture.Builder().build()

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    /**
     * Triggered when user clicks the camera shutter button.
     * Captures leaf frame as [Bitmap] and submits to [CropHealthViewModel].
     */
    fun captureAndAnalyzeLeaf() {
        val capture = imageCapture ?: return

        capture.takePicture(
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(imageProxy: ImageProxy) {
                    val bitmap = imageProxyToBitmap(imageProxy)
                    imageProxy.close()

                    if (bitmap != null) {
                        // Submit captured leaf bitmap to Hybrid ViewModel Pipeline
                        viewModel.analyzeCropLeaf(this@CropHealthActivity, bitmap)
                    } else {
                        Toast.makeText(this@CropHealthActivity, "Failed to decode camera frame.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(this@CropHealthActivity, "Camera Capture Error: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    /**
     * Observes [CropHealthViewModel.uiState] and updates UI based on diagnosis lifecycle.
     */
    private fun observeViewModelState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is CropHealthUiState.Idle -> {
                            // UI prompt: "Align leaf in green box and tap capture"
                        }
                        is CropHealthUiState.Loading -> {
                            // Display progress spinner: "Analyzing crop leaf..."
                            Toast.makeText(this@CropHealthActivity, "Analyzing crop leaf...", Toast.LENGTH_SHORT).show()
                        }
                        is CropHealthUiState.QualityError -> {
                            // Step A Quality Gate Failed (Severe blur or extreme lighting)
                            val feedback = state.qualityResult.feedbackMessage
                            Toast.makeText(this@CropHealthActivity, "Quality Check Failed: $feedback", Toast.LENGTH_LONG).show()
                        }
                        is CropHealthUiState.LocalSuccess -> {
                            // Step C: On-Device TFLite Diagnosis Confident (>= 0.70)
                            val result = state.result
                            val msg = String.format(
                                Locale.US,
                                "Local Diagnosis: %s (Confidence: %.1f%%)",
                                result.diseaseName,
                                result.confidence * 100f
                            )
                            Toast.makeText(this@CropHealthActivity, msg, Toast.LENGTH_LONG).show()
                        }
                        is CropHealthUiState.CloudSuccess -> {
                            // Step D: Low Confidence Fallback to FastAPI Cloud Server Succeeded
                            val response = state.response
                            val msg = String.format(
                                Locale.US,
                                "Cloud Re-Analysis: %s (Confidence: %.1f%%)",
                                response.diseaseName,
                                response.confidence * 100f
                            )
                            Toast.makeText(this@CropHealthActivity, msg, Toast.LENGTH_LONG).show()
                        }
                        is CropHealthUiState.Error -> {
                            Toast.makeText(this@CropHealthActivity, "Error: ${state.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    /**
     * Converts [ImageProxy] from CameraX to software [Bitmap].
     */
    private fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap? {
        val buffer = imageProxy.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
