package com.example.smartagriculture.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.smartagriculture.R
import com.example.smartagriculture.databinding.FragmentDiseaseDetailsBinding
import com.example.smartagriculture.model.CropDiseaseItem

class DiseaseDetailsFragment : Fragment(R.layout.fragment_disease_details) {

    private var binding: FragmentDiseaseDetailsBinding? = null
    private var diseaseItem: CropDiseaseItem? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDiseaseDetailsBinding.bind(view)

        @Suppress("DEPRECATION")
        diseaseItem = arguments?.getSerializable("diseaseItem") as? CropDiseaseItem

        val disease = diseaseItem ?: return

        binding?.tvDiseaseName?.text = disease.diseaseName
        binding?.tvScientificName?.text = disease.scientificName
        binding?.tvOverviewText?.text = disease.description

        if (disease.similarDiseases.isNotEmpty()) {
            binding?.tvSimilarName?.text = disease.similarDiseases.first()
        }

        binding?.btnViewCareGuide?.setOnClickListener {
            val bundle = bundleOf("diseaseItem" to disease)
            findNavController().navigate(R.id.action_diseaseDetailsFragment_to_careGuideFragment, bundle)
        }

        binding?.btnBack?.setOnClickListener {
            findNavController().navigateUp()
        }

        binding?.btnFavorite?.setOnClickListener {
            Toast.makeText(requireContext(), "Added disease to saved items", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
