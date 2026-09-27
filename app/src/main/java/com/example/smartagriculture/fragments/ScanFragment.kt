package com.example.smartagriculture.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
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
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ScanFragment : Fragment(R.layout.fragment_scan) {

    private var binding: FragmentScanBinding? = null
    private var imageCapture: ImageCapture? = null
    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private var flashMode = ImageCapture.FLASH_MODE_OFF
    private lateinit var cameraExecutor: ExecutorService

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startCamera()
        } else {
            Toast.makeText(requireContext(), "Camera permission required. You can pick from Gallery.", Toast.LENGTH_LONG).show()
            binding?.ivPlaceholder?.visibility = View.VISIBLE
        }
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            navigateToQualityCheck(it.toString())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentScanBinding.bind(view)
        cameraExecutor = Executors.newSingleThreadExecutor()

        checkCameraPermission()

        binding?.btnShutter?.setOnClickListener {
            takePhoto()
        }

        binding?.btnGallery?.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        binding?.btnFlash?.setOnClickListener {
            toggleFlash()
        }

        binding?.btnFlip?.setOnClickListener {
            flipCamera()
        }

        binding?.btnHelp?.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "Position the leaf inside the green frame and make sure it is in good focus.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
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

                imageCapture = ImageCapture.Builder()
                    .setFlashMode(flashMode)
                    .build()

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
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
        val toastMsg = when (flashMode) {
            ImageCapture.FLASH_MODE_ON -> "Flash ON"
            ImageCapture.FLASH_MODE_AUTO -> "Flash AUTO"
            else -> "Flash OFF"
        }
        Toast.makeText(requireContext(), toastMsg, Toast.LENGTH_SHORT).show()
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
        val imageCapture = imageCapture
        if (imageCapture == null) {
            // Fallback: create mock image from placeholder
            val photoFile = File(requireContext().cacheDir, "scan_${System.currentTimeMillis()}.jpg")
            val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.bg_1)
            val bitmap = Bitmap.createBitmap(600, 600, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable?.setBounds(0, 0, canvas.width, canvas.height)
            drawable?.draw(canvas)
            FileOutputStream(photoFile).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it) }
            navigateToQualityCheck(Uri.fromFile(photoFile).toString())
            return
        }

        val photoFile = File(requireContext().cacheDir, "scan_${System.currentTimeMillis()}.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Toast.makeText(requireContext(), "Capture failed: ${exc.message}", Toast.LENGTH_SHORT).show()
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    navigateToQualityCheck(savedUri.toString())
                }
            }
        )
    }

    private fun navigateToQualityCheck(imageUriStr: String) {
        val bundle = bundleOf("imageUri" to imageUriStr)
        findNavController().navigate(R.id.action_scanFragment_to_scanQualityFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
        binding = null
    }
}
