package com.example.smartagriculture.fragments

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.smartagriculture.R
import com.example.smartagriculture.database.AppDatabase
import com.example.smartagriculture.databinding.FragmentScanResultBinding
import com.example.smartagriculture.model.DiseaseAnalysisResult
import com.example.smartagriculture.model.ScanHistoryItem
import kotlinx.coroutines.launch

class ScanResultFragment : Fragment(R.layout.fragment_scan_result) {

    private var binding: FragmentScanResultBinding? = null
    private var imageUriStr: String? = null
    private var analysisResult: DiseaseAnalysisResult? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentScanResultBinding.bind(view)

        imageUriStr = arguments?.getString("imageUri")
        @Suppress("DEPRECATION")
        analysisResult = arguments?.getSerializable("analysisResult") as? DiseaseAnalysisResult

        val result = analysisResult ?: DiseaseAnalysisResult(
            diseaseName = "Tomato Early Blight",
            scientificName = "Alternaria solani",
            confidence = 87,
            isLowConfidence = false,
            aiExplanation = "The highlighted regions show brown concentric spots on leaves indicating fungal infection typical of Early Blight.",
            organicCare = "• Remove affected leaves and dispose of them properly\n• Improve air circulation and avoid overhead watering\n• Use neem oil spray (5 ml per litre of water)\n• Apply compost to improve soil health",
            chemicalCare = "• Apply copper-based fungicide at initial symptom appearance\n• Spray Chlorothalonil or Mancozeb at 7-10 day intervals\n• Rotate fungicide classes to prevent resistance"
        )

        // Load leaf image
        if (!imageUriStr.isNullOrBlank()) {
            binding?.ivResultLeaf?.let {
                Glide.with(this)
                    .load(Uri.parse(imageUriStr))
                    .placeholder(R.drawable.bg_1)
                    .into(it)
            }
        }

        // Bind data
        binding?.tvDiseaseName?.text = result.diseaseName
        binding?.tvScientificName?.text = result.scientificName
        binding?.tvConfidenceValue?.text = "${result.confidence}%"
        binding?.pbConfidence?.progress = result.confidence
        binding?.tvAiExplanation?.text = result.aiExplanation

        binding?.btnBack?.setOnClickListener {
            findNavController().navigateUp()
        }

        binding?.btnViewHeatmap?.setOnClickListener {
            val bundle = bundleOf(
                "imageUri" to imageUriStr,
                "analysisResult" to result
            )
            findNavController().navigate(R.id.action_scanResultFragment_to_scanHeatmapFragment, bundle)
        }

        binding?.btnViewCareGuide?.setOnClickListener {
            val bundle = bundleOf("analysisResult" to result)
            findNavController().navigate(R.id.action_scanResultFragment_to_scanCareGuideFragment, bundle)
        }

        binding?.btnSaveHistory?.setOnClickListener {
            saveToHistory(result)
        }

        binding?.btnScanAgain?.setOnClickListener {
            findNavController().popBackStack(R.id.scanFragment, false)
        }
    }

    private fun saveToHistory(result: DiseaseAnalysisResult) {
        val historyItem = ScanHistoryItem(
            imagePath = imageUriStr ?: "",
            diseaseName = result.diseaseName,
            scientificName = result.scientificName,
            confidence = result.confidence,
            isLowConfidence = result.isLowConfidence,
            aiExplanation = result.aiExplanation,
            organicCare = result.organicCare,
            chemicalCare = result.chemicalCare
        )

        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            db.scanHistoryDao().insertScan(historyItem)
            Toast.makeText(requireContext(), "Saved to scan history!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
