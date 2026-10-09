package com.example.smartagriculture.fragments

import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.smartagriculture.R
import com.example.smartagriculture.databinding.FragmentScanHeatmapBinding
import com.example.smartagriculture.ml.GradCamEngine
import com.example.smartagriculture.model.DiseaseAnalysisResult
import com.example.smartagriculture.utils.ImageStorageManager

/**
 * Fragment displaying explainable AI (XAI) Grad-CAM visual attention overlays.
 * Features an interactive continuous opacity blending slider (0% to 100%),
 * preset buttons, Jet colormap legend, and on-device saliency map rendering.
 */
class ScanHeatmapFragment : Fragment(R.layout.fragment_scan_heatmap) {

    private var binding: FragmentScanHeatmapBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentScanHeatmapBinding.bind(view)

        val imageUriStr = arguments?.getString("imageUri")
        @Suppress("DEPRECATION")
        val result = arguments?.getSerializable("analysisResult") as? DiseaseAnalysisResult

        // 1. Decode software leaf bitmap from URI
        val leafBitmap = loadBitmapFromUri(imageUriStr)

        // 2. Load preview into background ImageView
        if (!imageUriStr.isNullOrBlank()) {
            binding?.ivOriginalLeaf?.let {
                val model = ImageStorageManager.getImageModel(imageUriStr) ?: imageUriStr
                Glide.with(this)
                    .load(model)
                    .placeholder(R.drawable.rounded_button)
                    .error(R.drawable.rounded_button)
                    .into(it)
            }
        }

        // 3. Extract or compute on-device Grad-CAM activation matrix
        @Suppress("UNCHECKED_CAST")
        val activationMatrix = (arguments?.getSerializable("activationMatrix") as? Array<FloatArray>)
            ?: result?.activationMatrix
            ?: leafBitmap?.let { GradCamEngine.generateActivationMatrix(it) }

        // 4. Bind to GradCamView
        binding?.gradCamView?.setGradCamData(leafBitmap, activationMatrix)

        if (result != null) {
            binding?.tvHeatmapNoteText?.text =
                "These highlighted areas most influenced the AI's diagnosis of ${result.diseaseName}."
        }

        // 5. Initialize Opacity Slider (Default 70%)
        val initialProgress = 70
        binding?.sliderOpacity?.progress = initialProgress
        binding?.tvOpacityValue?.text = "$initialProgress%"
        binding?.gradCamView?.setAlphaOpacity(initialProgress / 100.0f)
        updateControlsState(initialProgress)

        binding?.sliderOpacity?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val alpha = progress / 100.0f
                binding?.tvOpacityValue?.text = "$progress%"
                binding?.gradCamView?.setAlphaOpacity(alpha)
                updateControlsState(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // 6. Preset Chips Listeners
        binding?.btnPreset0?.setOnClickListener {
            binding?.sliderOpacity?.progress = 0
        }
        binding?.btnPreset50?.setOnClickListener {
            binding?.sliderOpacity?.progress = 50
        }
        binding?.btnPreset100?.setOnClickListener {
            binding?.sliderOpacity?.progress = 100
        }

        // 7. Top Quick Tabs Listeners
        binding?.btnToggleOriginal?.setOnClickListener {
            binding?.sliderOpacity?.progress = 0
        }
        binding?.btnToggleHeatmap?.setOnClickListener {
            binding?.sliderOpacity?.progress = 100
        }

        binding?.btnBack?.setOnClickListener {
            findNavController().navigateUp()
        }

        binding?.btnBackToResult?.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    /**
     * Synchronizes styling of quick toggle tabs and preset buttons with current slider progress.
     */
    private fun updateControlsState(progress: Int) {
        val b = binding ?: return

        // Update Top Tabs
        if (progress == 0) {
            b.btnToggleOriginal.setBackgroundResource(R.drawable.rounded_button)
            b.btnToggleOriginal.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#10B981"))
            b.btnToggleOriginal.setTextColor(Color.WHITE)

            b.btnToggleHeatmap.background = null
            b.btnToggleHeatmap.setTextColor(Color.parseColor("#9CA3AF"))
        } else if (progress >= 80) {
            b.btnToggleHeatmap.setBackgroundResource(R.drawable.rounded_button)
            b.btnToggleHeatmap.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#10B981"))
            b.btnToggleHeatmap.setTextColor(Color.WHITE)

            b.btnToggleOriginal.background = null
            b.btnToggleOriginal.setTextColor(Color.parseColor("#9CA3AF"))
        } else {
            b.btnToggleOriginal.background = null
            b.btnToggleOriginal.setTextColor(Color.parseColor("#9CA3AF"))
            b.btnToggleHeatmap.background = null
            b.btnToggleHeatmap.setTextColor(Color.parseColor("#9CA3AF"))
        }

        // Update Preset Chips
        val activeBg = ColorStateList.valueOf(Color.parseColor("#10B981"))
        val defaultBg = ColorStateList.valueOf(Color.parseColor("#263228"))

        b.btnPreset0.backgroundTintList = if (progress == 0) activeBg else defaultBg
        b.btnPreset0.setTextColor(if (progress == 0) Color.WHITE else Color.parseColor("#9CA3AF"))

        b.btnPreset50.backgroundTintList = if (progress in 40..60) activeBg else defaultBg
        b.btnPreset50.setTextColor(if (progress in 40..60) Color.WHITE else Color.parseColor("#9CA3AF"))

        b.btnPreset100.backgroundTintList = if (progress >= 95) activeBg else defaultBg
        b.btnPreset100.setTextColor(if (progress >= 95) Color.WHITE else Color.parseColor("#9CA3AF"))
    }

    private fun loadBitmapFromUri(uriStr: String?): Bitmap? {
        if (uriStr.isNullOrBlank()) return null
        if (uriStr.startsWith("/")) {
            val file = java.io.File(uriStr)
            if (file.exists()) {
                val decoded = BitmapFactory.decodeFile(uriStr)
                if (decoded != null) return decoded
            }
        }
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
            e.printStackTrace()
            null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
