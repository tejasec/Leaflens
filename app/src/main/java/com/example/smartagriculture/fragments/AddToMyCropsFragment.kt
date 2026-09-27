package com.example.smartagriculture.fragments

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.smartagriculture.R
import com.example.smartagriculture.databinding.FragmentAddToMyCropsBinding
import com.example.smartagriculture.repository.CropRepository

class AddToMyCropsFragment : Fragment(R.layout.fragment_add_to_my_crops) {

    private var binding: FragmentAddToMyCropsBinding? = null
    private val selectedCrops = mutableListOf("Tomato", "Chilli", "Brinjal")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAddToMyCropsBinding.bind(view)

        populateSuggestedCrops()
        renderDockChips()

        binding?.btnViewAllCrops?.setOnClickListener {
            findNavController().navigate(R.id.action_addToMyCropsFragment_to_allCropsFragment)
        }

        binding?.btnSaveChanges?.setOnClickListener {
            Toast.makeText(requireContext(), "Saved changes to My Crops!", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_addToMyCropsFragment_to_myCropsFragment)
        }

        binding?.btnBack?.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun populateSuggestedCrops() {
        binding?.containerSuggested?.removeAllViews()
        val suggestions = listOf("Tomato", "Chilli", "Brinjal", "Cucumber", "Okra")

        for (cropName in suggestions) {
            val itemView = LayoutInflater.from(requireContext()).inflate(R.layout.item_crop_list, binding?.containerSuggested, false)
            val tvTitle = itemView.findViewById<TextView>(R.id.tvCropTitle)
            val tvSub = itemView.findViewById<TextView>(R.id.tvCropScientific)
            val ivThumb = itemView.findViewById<ImageView>(R.id.ivCropThumb)

            val detail = CropRepository.allCropsList.find { it.name.equals(cropName, ignoreCase = true) }
            tvTitle.text = cropName
            tvSub.text = detail?.scientificName ?: "Crop"
            ivThumb.setImageResource(detail?.imageRes ?: R.drawable.bg_1)

            val btnAdd = TextView(requireContext()).apply {
                text = " ⊕ "
                setTextColor(Color.parseColor("#10B981"))
                textSize = 24f
                setOnClickListener {
                    if (!selectedCrops.contains(cropName)) {
                        selectedCrops.add(cropName)
                        CropRepository.addMyCrop(cropName)
                        renderDockChips()
                    }
                }
            }
            (itemView as LinearLayout).addView(btnAdd)

            binding?.containerSuggested?.addView(itemView)
        }
    }

    private fun renderDockChips() {
        binding?.rowDockChips?.removeAllViews()
        binding?.tvDockTitle?.text = "My Crops (${selectedCrops.size})"

        for (crop in selectedCrops) {
            val chip = TextView(requireContext()).apply {
                text = "$crop ✕"
                setTextColor(Color.WHITE)
                textSize = 12f
                setBackgroundResource(R.drawable.rounded_button)
                backgroundTintList = ColorStateList.valueOf(Color.parseColor("#2E3B31"))
                setPadding(24, 12, 24, 12)
                val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                params.setMargins(0, 0, 12, 0)
                layoutParams = params

                setOnClickListener {
                    selectedCrops.remove(crop)
                    CropRepository.removeMyCrop(crop)
                    renderDockChips()
                }
            }
            binding?.rowDockChips?.addView(chip)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
