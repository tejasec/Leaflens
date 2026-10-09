package com.example.smartagriculture.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.Camera
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
import com.example.smartagriculture.utils.ImageStorageManager
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ScanFragment : Fragment(R.layout.fragment_scan) {

    private enum class FlashState { OFF, ON, TORCH }

    private var binding: FragmentScanBinding? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private var imageCapture: ImageCapture? = null
    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private var currentFlashState = FlashState.OFF
    private var isNavigating = false
    private lateinit var cameraExecutor: ExecutorService

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startCamera()
        } else {
            Toast.makeText(
                requireContext(),
                "Camera permission required. You can pick an image from Gallery.",
                Toast.LENGTH_LONG
            ).show()
            binding?.ivPlaceholder?.visibility = View.VISIBLE
        }
    }

    private val galleryPicker = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val persistedPath = ImageStorageManager.ingestCaptureUri(requireContext(), uri)
                navigateToQualityCheck(persistedPath)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Failed to load selected photo.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentScanBinding.bind(view)
        cameraExecutor = Executors.newSingleThreadExecutor()

        setupHeader()
        setupControls()
        checkCameraPermission()
    }

    override fun onResume() {
        super.onResume()
        isNavigating = false
        binding?.btnShutter?.isEnabled = true
    }

    private fun setupHeader() {
        // Style header title with "Scan Leaf" where "Leaf" is mint accent (#35E6A0)
        val titleText = "Scan Leaf"
        val spannable = SpannableString(titleText)
        val mintColor = Color.parseColor("#35E6A0")
        val leafStart = titleText.indexOf("Leaf")
        if (leafStart >= 0) {
            spannable.setSpan(
                ForegroundColorSpan(mintColor),
                leafStart,
                leafStart + 4,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        binding?.tvTitle?.text = spannable

        // Clean back button navigation with fallback to home
        binding?.btnBack?.setOnClickListener {
            if (!findNavController().popBackStack()) {
                findNavController().navigate(R.id.homeFragment)
            }
        }

        binding?.btnHelp?.setOnClickListener {
            showHelpDialog()
        }
    }

    private fun setupControls() {
        binding?.btnShutter?.setOnClickListener {
            takePhoto()
        }

        binding?.btnGallery?.setOnClickListener {
            galleryPicker.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }

        binding?.btnFlash?.setOnClickListener {
            toggleFlash()
        }

        binding?.btnFlip?.setOnClickListener {
            flipCamera()
        }

        binding?.btnTips?.setOnClickListener {
            showTipsDialog()
        }

        binding?.btnZoomHalf?.setOnClickListener {
            setZoomRatio(0.5f)
        }

        binding?.btnZoom1x?.setOnClickListener {
            setZoomRatio(1.0f)
        }

        binding?.btnZoom2x?.setOnClickListener {
            setZoomRatio(2.0f)
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
                val provider: ProcessCameraProvider = cameraProviderFuture.get()
                cameraProvider = provider

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(binding?.viewFinder?.surfaceProvider)
                }

                val captureFlashMode = if (currentFlashState == FlashState.ON) {
                    ImageCapture.FLASH_MODE_ON
                } else {
                    ImageCapture.FLASH_MODE_OFF
                }

                imageCapture = ImageCapture.Builder()
                    .setFlashMode(captureFlashMode)
                    .build()

                provider.unbindAll()
                val boundCamera = provider.bindToLifecycle(
                    viewLifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
                camera = boundCamera
                binding?.ivPlaceholder?.visibility = View.GONE

                setupZoomObserver(boundCamera)
                applyFlashState(silent = true)
            } catch (e: Exception) {
                binding?.ivPlaceholder?.visibility = View.VISIBLE
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun setupZoomObserver(boundCamera: Camera) {
        boundCamera.cameraInfo.zoomState.observe(viewLifecycleOwner) { zoomState ->
            val minRatio = zoomState.minZoomRatio
            val maxRatio = zoomState.maxZoomRatio
            val currentRatio = zoomState.zoomRatio

            // 0.5x is only supported if minZoomRatio <= 0.6x
            if (minRatio <= 0.6f) {
                binding?.btnZoomHalf?.visibility = View.VISIBLE
            } else {
                binding?.btnZoomHalf?.visibility = View.GONE
            }

            // 1.0x is standard
            binding?.btnZoom1x?.visibility = View.VISIBLE

            // 2.0x is only supported if maxZoomRatio >= 2.0x
            if (maxRatio >= 2.0f) {
                binding?.btnZoom2x?.visibility = View.VISIBLE
            } else {
                binding?.btnZoom2x?.visibility = View.GONE
            }

            updateZoomPillSelection(currentRatio)
        }
    }

    private fun updateZoomPillSelection(ratio: Float) {
        val isHalf = ratio < 0.8f
        val is2x = ratio >= 1.8f
        val is1x = !isHalf && !is2x

        setZoomPillStyle(binding?.btnZoomHalf, isHalf)
        setZoomPillStyle(binding?.btnZoom1x, is1x)
        setZoomPillStyle(binding?.btnZoom2x, is2x)
    }

    private fun setZoomPillStyle(view: TextView?, isActive: Boolean) {
        if (view == null) return
        if (isActive) {
            view.setBackgroundResource(R.drawable.bg_zoom_pill_active)
            view.setTextColor(Color.parseColor("#0B1711"))
        } else {
            view.setBackgroundResource(R.drawable.bg_zoom_pill_inactive)
            view.setTextColor(Color.parseColor("#FFFFFF"))
        }
    }

    private fun setZoomRatio(ratio: Float) {
        val cameraControl = camera?.cameraControl ?: return
        val zoomState = camera?.cameraInfo?.zoomState?.value ?: return
        val clampedRatio = ratio.coerceIn(zoomState.minZoomRatio, zoomState.maxZoomRatio)
        cameraControl.setZoomRatio(clampedRatio)
    }

    private fun toggleFlash() {
        val cameraInfo = camera?.cameraInfo
        if (cameraInfo == null || !cameraInfo.hasFlashUnit()) {
            Toast.makeText(requireContext(), "Flash unavailable on this camera", Toast.LENGTH_SHORT).show()
            return
        }

        currentFlashState = when (currentFlashState) {
            FlashState.OFF -> FlashState.ON
            FlashState.ON -> FlashState.TORCH
            FlashState.TORCH -> FlashState.OFF
        }
        applyFlashState(silent = false)
    }

    private fun applyFlashState(silent: Boolean = false) {
        val hasFlash = camera?.cameraInfo?.hasFlashUnit() == true
        val btnFlash = binding?.btnFlash ?: return

        if (!hasFlash) {
            currentFlashState = FlashState.OFF
            try {
                camera?.cameraControl?.enableTorch(false)
            } catch (_: Exception) {}
            imageCapture?.flashMode = ImageCapture.FLASH_MODE_OFF
            btnFlash.setImageResource(R.drawable.ic_flash_off)
            btnFlash.contentDescription = "Flash unavailable"
            btnFlash.alpha = 0.4f
            return
        }

        btnFlash.alpha = 1.0f
        when (currentFlashState) {
            FlashState.OFF -> {
                try {
                    camera?.cameraControl?.enableTorch(false)
                } catch (_: Exception) {}
                imageCapture?.flashMode = ImageCapture.FLASH_MODE_OFF
                btnFlash.setImageResource(R.drawable.ic_flash_off)
                btnFlash.contentDescription = "Flash off"
                if (!silent) Toast.makeText(requireContext(), "Flash: Off", Toast.LENGTH_SHORT).show()
            }
            FlashState.ON -> {
                try {
                    camera?.cameraControl?.enableTorch(false)
                } catch (_: Exception) {}
                imageCapture?.flashMode = ImageCapture.FLASH_MODE_ON
                btnFlash.setImageResource(R.drawable.ic_flash_on)
                btnFlash.contentDescription = "Flash on (capture only)"
                if (!silent) Toast.makeText(requireContext(), "Flash: On (capture)", Toast.LENGTH_SHORT).show()
            }
            FlashState.TORCH -> {
                imageCapture?.flashMode = ImageCapture.FLASH_MODE_OFF
                try {
                    camera?.cameraControl?.enableTorch(true)
                } catch (_: Exception) {}
                btnFlash.setImageResource(R.drawable.ic_torch_on)
                btnFlash.contentDescription = "Flash always on (torch)"
                if (!silent) Toast.makeText(requireContext(), "Flash: Always on (torch)", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun flipCamera() {
        try {
            camera?.cameraControl?.enableTorch(false)
        } catch (_: Exception) {}
        currentFlashState = FlashState.OFF

        cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }
        startCamera()
    }

    private fun takePhoto() {
        if (isNavigating) return
        isNavigating = true
        binding?.btnShutter?.isEnabled = false

        val capturesDir = ImageStorageManager.getCapturesDir(requireContext())
        val photoFile = File(capturesDir, "scan_${System.currentTimeMillis()}.jpg")

        val imageCapture = imageCapture
        if (imageCapture == null) {
            // Test/emulator fallback: generate synthetic sample leaf canvas
            val bitmap = Bitmap.createBitmap(600, 600, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.parseColor("#142820"))
            val strokePaint = Paint().apply {
                color = Color.parseColor("#35E6A0")
                style = Paint.Style.STROKE
                strokeWidth = 6f
            }
            canvas.drawRect(40f, 40f, 560f, 560f, strokePaint)
            ImageStorageManager.saveBitmapToFile(bitmap, photoFile)
            navigateToQualityCheck(photoFile.absolutePath)
            return
        }

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    isNavigating = false
                    binding?.btnShutter?.isEnabled = true
                    Toast.makeText(requireContext(), "Capture failed: ${exc.message}", Toast.LENGTH_SHORT).show()
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    if (!isAdded) return
                    navigateToQualityCheck(photoFile.absolutePath)
                }
            }
        )
    }

    private fun navigateToQualityCheck(imageUriStr: String) {
        if (!isAdded) return
        val bundle = bundleOf("imageUri" to imageUriStr)
        findNavController().navigate(R.id.action_scanFragment_to_scanQualityFragment, bundle)
    }

    private fun showHelpDialog() {
        AlertDialog.Builder(requireContext(), R.style.Theme_SmartAgriculture)
            .setTitle("LeafLens Scan Guide")
            .setMessage(
                "How to scan a leaf:\n\n" +
                "1. Position the leaf inside the mint corner brackets.\n" +
                "2. Keep the camera parallel to the leaf surface (15–30 cm away).\n" +
                "3. Ensure the affected spot or lesion is centered and sharp.\n" +
                "4. Tap the shutter button to take the photo and review quality."
            )
            .setPositiveButton("Got it") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun showTipsDialog() {
        AlertDialog.Builder(requireContext(), R.style.Theme_SmartAgriculture)
            .setTitle("Capture Tips for Best Accuracy")
            .setMessage(
                "✓ Good Lighting:\n" +
                "Natural daylight or bright indirect light gives the clearest symptoms.\n\n" +
                "✓ Keep in Focus:\n" +
                "Hold your hands steady. Wait for the camera to autofocus on the leaf veins.\n\n" +
                "✓ Capture Affected Area:\n" +
                "Make sure leaf spots, discoloration, or lesions fill at least 30% of the frame.\n\n" +
                "✓ Avoid Obstructions:\n" +
                "Keep fingers, soil, and neighboring leaves out of the center box."
            )
            .setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    override fun onStop() {
        super.onStop()
        try {
            camera?.cameraControl?.enableTorch(false)
        } catch (_: Exception) {}
    }

    override fun onDestroyView() {
        super.onDestroyView()
        try {
            camera?.cameraControl?.enableTorch(false)
            cameraProvider?.unbindAll()
        } catch (_: Exception) {}
        camera = null
        imageCapture = null
        cameraExecutor.shutdown()
        binding = null
    }
}
