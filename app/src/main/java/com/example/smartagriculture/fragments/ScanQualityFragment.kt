package com.example.smartagriculture.fragments

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.smartagriculture.R
import com.example.smartagriculture.databinding.FragmentScanQualityBinding
import com.example.smartagriculture.quality.QualityGate

/**
 * Pre-Analysis Validation Screen (Step 1 of New Workflow).
 * Evaluates BOTH Image Quality AND Leaf/Scanner Suitability before deep learning inference.
 */
class ScanQualityFragment : Fragment(R.layout.fragment_scan_quality) {

    private var binding: FragmentScanQualityBinding? = null
    private var imageUriStr: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentScanQualityBinding.bind(view)

        imageUriStr = arguments?.getString("imageUri")

        if (!imageUriStr.isNullOrBlank()) {
            binding?.ivCapturedLeaf?.let {
                Glide.with(this)
                    .load(Uri.parse(imageUriStr))
                    .placeholder(R.drawable.bg_1)
                    .into(it)
            }
        } else {
            binding?.ivCapturedLeaf?.setImageResource(R.drawable.bg_1)
        }

        // Decode bitmap and run local pre-analysis validation
        val bitmap = loadBitmapFromUri(imageUriStr)
        val qualityDetails = QualityGate.evaluateDetailedQuality(bitmap)

        // Bind Section A: Image Quality Check
        binding?.tvResolutionStatus?.text = qualityDetails.resolutionStatus
        binding?.tvLightingStatus?.text = qualityDetails.lightingStatus
        binding?.tvSharpnessStatus?.text = qualityDetails.sharpnessStatus
        binding?.tvLeafVisibilityStatus?.text = qualityDetails.leafVisibilityStatus

        // Color code status text
        binding?.tvResolutionStatus?.setTextColor(if (qualityDetails.resolutionStatus.contains("✓")) Color.parseColor("#10B981") else Color.parseColor("#F59E0B"))
        binding?.tvLightingStatus?.setTextColor(if (qualityDetails.lightingStatus.contains("✓")) Color.parseColor("#10B981") else Color.parseColor("#F59E0B"))
        binding?.tvSharpnessStatus?.setTextColor(if (qualityDetails.sharpnessStatus.contains("✓")) Color.parseColor("#10B981") else Color.parseColor("#EF4444"))
        binding?.tvLeafVisibilityStatus?.setTextColor(if (qualityDetails.leafVisibilityStatus.contains("✓")) Color.parseColor("#10B981") else Color.parseColor("#EF4444"))

        // Bind Section B: Scanner Suitability Check
        val leafDetectedText = if (qualityDetails.leafDetected) "Yes ✓" else "No ⚠"
        binding?.tvLeafDetectedStatus?.text = leafDetectedText
        binding?.tvLeafDetectedStatus?.setTextColor(if (qualityDetails.leafDetected) Color.parseColor("#10B981") else Color.parseColor("#EF4444"))

        val coverageText = "${qualityDetails.foliageCoveragePct}%"
        binding?.tvFoliageCoverageStatus?.text = coverageText
        val isCoverageOk = qualityDetails.foliageCoveragePct >= qualityDetails.minRequiredPct
        binding?.tvFoliageCoverageStatus?.setTextColor(if (isCoverageOk) Color.parseColor("#10B981") else Color.parseColor("#EF4444"))

        // Overall Header Status
        binding?.tvStatusHeader?.text = qualityDetails.overallStatus
        binding?.tvStatusSub?.text = qualityDetails.feedbackMessage

        if (qualityDetails.isValid) {
            binding?.tvStatusHeader?.setTextColor(Color.parseColor("#10B981"))
            binding?.btnAnalyzeLeaf?.visibility = View.VISIBLE
            binding?.btnRetakePhoto?.visibility = View.GONE
        } else {
            binding?.tvStatusHeader?.setTextColor(Color.parseColor("#EF4444"))
            binding?.btnAnalyzeLeaf?.visibility = View.GONE
            binding?.btnRetakePhoto?.visibility = View.VISIBLE
        }

        binding?.btnBack?.setOnClickListener {
            findNavController().navigateUp()
        }

        binding?.btnEditImage?.setOnClickListener {
            findNavController().navigateUp()
        }

        binding?.btnRetakePhoto?.setOnClickListener {
            findNavController().navigateUp()
        }

        binding?.btnAnalyzeLeaf?.setOnClickListener {
            val bundle = bundleOf("imageUri" to imageUriStr)
            findNavController().navigate(R.id.action_scanQualityFragment_to_scanAnalyzingFragment, bundle)
        }
    }

    private fun loadBitmapFromUri(uriStr: String?): Bitmap {
        if (uriStr.isNullOrBlank()) return createFallbackBitmap()
        return try {
            val uri = Uri.parse(uriStr)
            val decoded = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(requireContext().contentResolver, uri)) { decoder, _, _ ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    decoder.isMutableRequired = true
                }
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
            }
            if (decoded.config == Bitmap.Config.HARDWARE) {
                decoded.copy(Bitmap.Config.ARGB_8888, false)
            } else {
                decoded
            }
        } catch (e: Exception) {
            createFallbackBitmap()
        }
    }

    private fun createFallbackBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.bg_1)
        drawable?.setBounds(0, 0, canvas.width, canvas.height)
        drawable?.draw(canvas)
        return bitmap
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
