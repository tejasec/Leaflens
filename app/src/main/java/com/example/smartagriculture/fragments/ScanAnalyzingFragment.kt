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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.smartagriculture.R
import com.example.smartagriculture.databinding.FragmentScanAnalyzingBinding
import com.example.smartagriculture.network.GeminiService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ScanAnalyzingFragment : Fragment(R.layout.fragment_scan_analyzing) {

    private var binding: FragmentScanAnalyzingBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentScanAnalyzingBinding.bind(view)

        val imageUriStr = arguments?.getString("imageUri")

        viewLifecycleOwner.lifecycleScope.launch {
            // Animate progress steps
            delay(500)
            binding?.icStep2?.text = "✓"
            binding?.tvStep2?.setTextColor(Color.WHITE)

            delay(600)
            binding?.icStep3?.text = "✓"
            binding?.tvStep3?.setTextColor(Color.WHITE)

            // Load bitmap
            val bitmap = loadBitmapFromUri(imageUriStr)

            // Perform AI Inference
            val result = GeminiService.analyzeCropDisease(bitmap)

            delay(400)
            binding?.icStep4?.text = "✓"
            binding?.tvStep4?.setTextColor(Color.WHITE)

            delay(300)
            val bundle = bundleOf(
                "imageUri" to imageUriStr,
                "analysisResult" to result
            )

            if (result.isLowConfidence || result.confidence < 60) {
                findNavController().navigate(R.id.action_scanAnalyzingFragment_to_scanLowConfidenceFragment, bundle)
            } else {
                findNavController().navigate(R.id.action_scanAnalyzingFragment_to_scanResultFragment, bundle)
            }
        }
    }

    private fun loadBitmapFromUri(uriStr: String?): Bitmap {
        if (uriStr.isNullOrBlank()) {
            return createFallbackBitmap()
        }
        return try {
            val uri = Uri.parse(uriStr)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(requireContext().contentResolver, uri))
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
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
