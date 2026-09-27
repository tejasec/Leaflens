package com.example.smartagriculture.fragments

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartagriculture.R
import com.example.smartagriculture.adapter.PopularCropAdapter
import com.example.smartagriculture.databinding.FragmentCropGuideBinding
import com.example.smartagriculture.repository.CropRepository

class CropGuideFragment : Fragment(R.layout.fragment_crop_guide) {

    private var binding: FragmentCropGuideBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCropGuideBinding.bind(view)

        // Popular crops horizontal list
        val popularCrops = CropRepository.getPopularCrops()
        val adapter = PopularCropAdapter(popularCrops) { cropItem ->
            val bundle = bundleOf("cropItem" to cropItem)
            findNavController().navigate(R.id.action_cropGuideFragment_to_cropDetailsFragment, bundle)
        }

        binding?.rvPopularCrops?.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding?.rvPopularCrops?.adapter = adapter

        // Search card click -> AllCrops
        binding?.searchCard?.setOnClickListener {
            findNavController().navigate(R.id.action_cropGuideFragment_to_allCropsFragment)
        }

        // See all -> AllCrops
        binding?.btnSeeAll?.setOnClickListener {
            findNavController().navigate(R.id.action_cropGuideFragment_to_allCropsFragment)
        }

        // Manage Your Crops -> MyCrops
        binding?.cardManageCrops?.setOnClickListener {
            findNavController().navigate(R.id.action_cropGuideFragment_to_myCropsFragment)
        }

        // Seasonal recommendations -> CropInsights
        binding?.cardSeasonal?.setOnClickListener {
            findNavController().navigate(R.id.action_cropGuideFragment_to_cropInsightsFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
