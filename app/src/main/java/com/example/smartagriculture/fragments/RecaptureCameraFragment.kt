package com.example.smartagriculture.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.smartagriculture.R
import com.example.smartagriculture.databinding.FragmentScanBinding
import com.example.smartagriculture.model.ScanHistoryItem
import com.example.smartagriculture.utils.ImageStorageManager
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class RecaptureCameraFragment : Fragment(R.layout.fragment_scan) {

    private var binding: FragmentScanBinding? = null
    private var imageCapture: ImageCapture? = null
    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private var flashMode = ImageCapture.FLASH_MODE_OFF
    private lateinit var cameraExecutor: ExecutorService
    private var existingItem: ScanHistoryItem? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) startCamera()
        else binding?.ivPlaceholder?.visibility = View.VISIBLE
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val persistedPath = ImageStorageManager.ingestCaptureUri(requireContext(), it)
            navigateToReplace(persistedPath)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentScanBinding.bind(view)
        cameraExecutor = Executors.newSingleThreadExecutor()

        @Suppress("DEPRECATION")
        existingItem = arguments?.getSerializable("scanItem") as? ScanHistoryItem

        checkCameraPermission()

        binding?.btnShutter?.setOnClickListener { takePhoto() }
        binding?.btnGallery?.setOnClickListener { galleryLauncher.launch("image/*") }
        binding?.btnFlash?.setOnClickListener { toggleFlash() }
        binding?.btnFlip?.setOnClickListener { flipCamera() }
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            try {
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(binding?.viewFinder?.surfaceProvider)
                }

                imageCapture = ImageCapture.Builder().setFlashMode(flashMode).build()
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(viewLifecycleOwner, cameraSelector, preview, imageCapture)
                binding?.ivPlaceholder?.visibility = View.GONE
            } catch (e: Exception) {
                binding?.ivPlaceholder?.visibility = View.VISIBLE
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun toggleFlash() {
        flashMode = when (flashMode) {
            ImageCapture.FLASH_MODE_OFF -> ImageCapture.FLASH_MODE_ON
            ImageCapture.FLASH_MODE_ON -> ImageCapture.FLASH_MODE_AUTO
            else -> ImageCapture.FLASH_MODE_OFF
        }
        imageCapture?.flashMode = flashMode
    }

    private fun flipCamera() {
        cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }
        startCamera()
    }

    private fun takePhoto() {
        val capturesDir = ImageStorageManager.getCapturesDir(requireContext())
        val photoFile = File(capturesDir, "recapture_${System.currentTimeMillis()}.jpg")

        val imageCapture = imageCapture
        if (imageCapture == null) {
            val bitmap = Bitmap.createBitmap(600, 600, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.parseColor("#1B2E20"))
            val strokePaint = Paint().apply {
                color = Color.parseColor("#10B981")
                style = Paint.Style.STROKE
                strokeWidth = 6f
            }
            canvas.drawRect(40f, 40f, 560f, 560f, strokePaint)
            ImageStorageManager.saveBitmapToFile(bitmap, photoFile)
            navigateToReplace(photoFile.absolutePath)
            return
        }

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Toast.makeText(requireContext(), "Capture failed: ${exc.message}", Toast.LENGTH_SHORT).show()
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    navigateToReplace(photoFile.absolutePath)
                }
            }
        )
    }

    private fun navigateToReplace(newImageUriStr: String) {
        val bundle = bundleOf(
            "newImageUri" to newImageUriStr,
            "scanItem" to existingItem
        )
        findNavController().navigate(R.id.action_recaptureCameraFragment_to_replaceHistoryFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
        binding = null
    }
}
