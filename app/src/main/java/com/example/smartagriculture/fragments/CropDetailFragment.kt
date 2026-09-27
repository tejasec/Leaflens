package com.example.smartagriculture.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.smartagriculture.R
import com.example.smartagriculture.databinding.FragmentCropDetailBinding
import com.example.smartagriculture.repository.CropRepository

class CropDetailFragment : Fragment(R.layout.fragment_crop_detail) {

    private var binding: FragmentCropDetailBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCropDetailBinding.bind(view)

        val cropId = arguments?.getInt("cropId") ?: -1
        val crop = CropRepository.getCropById(cropId)

        if (crop != null) {
            binding?.tvDetailName?.text = crop.name
            binding?.tvDetailSeason?.text = "Season: ${crop.season}"
            binding?.tvDetailSoil?.text = "Soil Type: ${crop.soilType}"
            binding?.tvDetailWater?.text = "Water Need: ${crop.waterNeed}"
            binding?.tvDetailDescription?.text = crop.description
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}