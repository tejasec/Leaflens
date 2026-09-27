package com.example.smartagriculture.fragments

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.smartagriculture.R
import com.example.smartagriculture.databinding.FragmentScanLowConfidenceBinding
import com.example.smartagriculture.model.DiseaseAnalysisResult

class ScanLowConfidenceFragment : Fragment(R.layout.fragment_scan_low_confidence) {

    private var binding: FragmentScanLowConfidenceBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentScanLowConfidenceBinding.bind(view)

        val imageUriStr = arguments?.getString("imageUri")
        @Suppress("DEPRECATION")
        val result = arguments?.getSerializable("analysisResult") as? DiseaseAnalysisResult

        if (!imageUriStr.isNullOrBlank()) {
            binding?.ivLowConfLeaf?.let {
                Glide.with(this)
                    .load(Uri.parse(imageUriStr))
                    .placeholder(R.drawable.bg_1)
                    .into(it)
            }
        }

        if (result != null) {
            binding?.tvPossibleDisease?.text = result.diseaseName
            binding?.tvConfidenceValue?.text = "${result.confidence}%"
            if (result.aiExplanation.isNotBlank()) {
                binding?.tvSubtitle?.text = result.aiExplanation
            }
        }

        binding?.btnBack?.setOnClickListener {
            findNavController().navigateUp()
        }

        binding?.btnRetakePhoto?.setOnClickListener {
            findNavController().popBackStack(R.id.scanFragment, false)
        }

        binding?.btnContinueAnyway?.setOnClickListener {
            val bundle = bundleOf(
                "imageUri" to imageUriStr,
                "analysisResult" to result
            )
            findNavController().navigate(R.id.action_scanLowConfidenceFragment_to_scanResultFragment, bundle)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
