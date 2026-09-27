package com.example.smartagriculture.fragments

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.smartagriculture.R
import com.example.smartagriculture.databinding.FragmentCareGuideBinding
import com.example.smartagriculture.model.CropDiseaseItem

class CareGuideFragment : Fragment(R.layout.fragment_care_guide) {

    private var binding: FragmentCareGuideBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCareGuideBinding.bind(view)

        @Suppress("DEPRECATION")
        val disease = arguments?.getSerializable("diseaseItem") as? CropDiseaseItem

        val title = if (disease != null) "${disease.diseaseName} (${disease.scientificName})" else "Early Blight (Alternaria solani)"
        binding?.tvTitle?.text = "🌿 Care Guide"
        binding?.tvSubtitle?.text = title

        val organicText = disease?.organicCare ?: "• Remove affected leaves and dispose of them properly\n• Apply neem oil spray (5 ml per litre of water)\n• Use compost to improve soil health\n• Maintain proper plant spacing for good air circulation\n• Use resistant varieties (where available)"
        val chemicalText = disease?.chemicalCare ?: "• Apply copper-based fungicide at initial symptom appearance\n• Spray Chlorothalonil or Mancozeb at 7-10 day intervals\n• Rotate fungicide classes to prevent resistance"
        val preventionText = disease?.prevention ?: "• Practice crop rotation with non-solanaceous crops\n• Mulch around plant base to prevent soil splash\n• Avoid overhead watering; use drip irrigation"

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
