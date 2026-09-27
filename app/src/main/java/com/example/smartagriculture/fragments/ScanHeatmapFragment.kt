package com.example.smartagriculture.fragments

import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.smartagriculture.R
import com.example.smartagriculture.databinding.FragmentScanHeatmapBinding
import com.example.smartagriculture.model.DiseaseAnalysisResult

class ScanHeatmapFragment : Fragment(R.layout.fragment_scan_heatmap) {

    private var binding: FragmentScanHeatmapBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentScanHeatmapBinding.bind(view)

        val imageUriStr = arguments?.getString("imageUri")
        @Suppress("DEPRECATION")
        val result = arguments?.getSerializable("analysisResult") as? DiseaseAnalysisResult

        if (!imageUriStr.isNullOrBlank()) {
            binding?.ivOriginalLeaf?.let {
                Glide.with(this)
                    .load(Uri.parse(imageUriStr))
                    .placeholder(R.drawable.bg_1)
                    .into(it)
            }
        }

        if (result != null) {
            binding?.tvHeatmapNoteText?.text = "These highlighted areas most influenced the AI's diagnosis of ${result.diseaseName}."
        }

        binding?.btnToggleOriginal?.setOnClickListener {
            binding?.btnToggleOriginal?.setBackgroundResource(R.drawable.rounded_button)
            binding?.btnToggleOriginal?.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#10B981"))
            binding?.btnToggleOriginal?.setTextColor(Color.WHITE)

            binding?.btnToggleHeatmap?.background = null
            binding?.btnToggleHeatmap?.setTextColor(Color.parseColor("#9CA3AF"))

            binding?.heatmapOverlay?.setHeatmapVisible(false)
        }

        binding?.btnToggleHeatmap?.setOnClickListener {
            binding?.btnToggleHeatmap?.setBackgroundResource(R.drawable.rounded_button)
            binding?.btnToggleHeatmap?.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#10B981"))
            binding?.btnToggleHeatmap?.setTextColor(Color.WHITE)

            binding?.btnToggleOriginal?.background = null
            binding?.btnToggleOriginal?.setTextColor(Color.parseColor("#9CA3AF"))

            binding?.heatmapOverlay?.setHeatmapVisible(true)
        }

        binding?.btnBack?.setOnClickListener {
            findNavController().navigateUp()
        }

        binding?.btnBackToResult?.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
