package com.example.smartagriculture.fragments

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.smartagriculture.R
import com.example.smartagriculture.databinding.FragmentScanCareGuideBinding
import com.example.smartagriculture.model.DiseaseAnalysisResult

class ScanCareGuideFragment : Fragment(R.layout.fragment_scan_care_guide) {

    private var binding: FragmentScanCareGuideBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentScanCareGuideBinding.bind(view)

        @Suppress("DEPRECATION")
        val result = arguments?.getSerializable("analysisResult") as? DiseaseAnalysisResult

        val organicText = result?.organicCare ?: "• Remove affected leaves and dispose of them properly\n• Improve air circulation and avoid overhead watering\n• Use neem oil spray (5 ml per litre of water)\n• Apply compost to improve soil health"
        val chemicalText = result?.chemicalCare ?: "• Apply copper-based fungicide at initial symptom appearance\n• Spray Chlorothalonil or Mancozeb at 7-10 day intervals\n• Rotate fungicide classes to prevent resistance"

        binding?.tvCareAdvice?.text = organicText

        binding?.tabOrganic?.setOnClickListener {
            binding?.tabOrganic?.setBackgroundResource(R.drawable.rounded_button)
            binding?.tabOrganic?.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#10B981"))
            binding?.tabOrganic?.setTextColor(Color.WHITE)

            binding?.tabChemical?.background = null
            binding?.tabChemical?.setTextColor(Color.parseColor("#9CA3AF"))

            binding?.tvCareAdvice?.text = organicText
        }

        binding?.tabChemical?.setOnClickListener {
            binding?.tabChemical?.setBackgroundResource(R.drawable.rounded_button)
            binding?.tabChemical?.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#10B981"))
            binding?.tabChemical?.setTextColor(Color.WHITE)

            binding?.tabOrganic?.background = null
            binding?.tabOrganic?.setTextColor(Color.parseColor("#9CA3AF"))

            binding?.tvCareAdvice?.text = chemicalText
        }

        binding?.btnBack?.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
