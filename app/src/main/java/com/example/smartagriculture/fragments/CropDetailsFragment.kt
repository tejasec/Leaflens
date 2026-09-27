package com.example.smartagriculture.fragments

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartagriculture.R
import com.example.smartagriculture.adapter.CropDiseaseAdapter
import com.example.smartagriculture.databinding.FragmentCropDetailsBinding
import com.example.smartagriculture.model.CropDetailItem
import com.example.smartagriculture.repository.CropRepository

class CropDetailsFragment : Fragment(R.layout.fragment_crop_details) {

    private var binding: FragmentCropDetailsBinding? = null
    private var cropItem: CropDetailItem? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCropDetailsBinding.bind(view)

        @Suppress("DEPRECATION")
        cropItem = arguments?.getSerializable("cropItem") as? CropDetailItem ?: CropRepository.allCropsList.first()

        val crop = cropItem ?: return

        // Populate header
        binding?.tvCropDetailName?.text = crop.name
        binding?.tvCropDetailScientific?.text = crop.scientificName
        binding?.ivCropHero?.setImageResource(crop.imageRes)

        // Populate Stats
        binding?.tvStatSeason?.text = crop.growingSeason
        binding?.tvStatTemp?.text = crop.idealTemp
        binding?.tvStatSoil?.text = crop.soilType
        binding?.tvStatWater?.text = crop.waterRequirement
        binding?.tvAboutText?.text = crop.about
        binding?.tvProTipText?.text = crop.proTip

        // Setup Diseases RecyclerView
        val diseaseAdapter = CropDiseaseAdapter(crop.diseases) { diseaseItem ->
            val bundle = bundleOf("diseaseItem" to diseaseItem)
            findNavController().navigate(R.id.action_cropDetailsFragment_to_diseaseDetailsFragment, bundle)
        }
        binding?.rvDiseases?.layoutManager = LinearLayoutManager(requireContext())
        binding?.rvDiseases?.adapter = diseaseAdapter

        // Tab Switching
        setupTabSwitching(crop)

        binding?.btnFavorite?.setOnClickListener {
            crop.isFavorite = !crop.isFavorite
            binding?.btnFavorite?.text = if (crop.isFavorite) "♥" else "♡"
            Toast.makeText(requireContext(), if (crop.isFavorite) "Added to favorites" else "Removed from favorites", Toast.LENGTH_SHORT).show()
        }

        binding?.btnBack?.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupTabSwitching(crop: CropDetailItem) {
        val selectedBg = ColorStateList.valueOf(Color.parseColor("#10B981"))

        binding?.tabOverview?.setOnClickListener {
            binding?.tabOverview?.background = ContextCompat.getDrawable(requireContext(), R.drawable.rounded_button)
            binding?.tabOverview?.backgroundTintList = selectedBg
            binding?.tabOverview?.setTextColor(Color.WHITE)

            binding?.tabDiseases?.background = null
            binding?.tabDiseases?.setTextColor(Color.parseColor("#9CA3AF"))

            binding?.tabCareGuide?.background = null
            binding?.tabCareGuide?.setTextColor(Color.parseColor("#9CA3AF"))

            binding?.containerOverview?.visibility = View.VISIBLE
            binding?.containerDiseases?.visibility = View.GONE
        }

        binding?.tabDiseases?.setOnClickListener {
            binding?.tabDiseases?.background = ContextCompat.getDrawable(requireContext(), R.drawable.rounded_button)
            binding?.tabDiseases?.backgroundTintList = selectedBg
            binding?.tabDiseases?.setTextColor(Color.WHITE)

            binding?.tabOverview?.background = null
            binding?.tabOverview?.setTextColor(Color.parseColor("#9CA3AF"))

            binding?.tabCareGuide?.background = null
            binding?.tabCareGuide?.setTextColor(Color.parseColor("#9CA3AF"))

            binding?.containerOverview?.visibility = View.GONE
            binding?.containerDiseases?.visibility = View.VISIBLE
        }

        binding?.tabCareGuide?.setOnClickListener {
            val firstDisease = crop.diseases.firstOrNull()
            if (firstDisease != null) {
                val bundle = bundleOf("diseaseItem" to firstDisease)
                findNavController().navigate(R.id.action_cropDetailsFragment_to_careGuideFragment, bundle)
            } else {
                Toast.makeText(requireContext(), "No disease care guide available for this crop.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
