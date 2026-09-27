package com.example.smartagriculture.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.smartagriculture.R
import com.example.smartagriculture.databinding.FragmentCropProgressBinding
import com.example.smartagriculture.model.MyCropItem

class CropProgressFragment : Fragment(R.layout.fragment_crop_progress) {

    private var binding: FragmentCropProgressBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCropProgressBinding.bind(view)

        @Suppress("DEPRECATION")
        val myCrop = arguments?.getSerializable("myCrop") as? MyCropItem

        val cropName = myCrop?.cropName ?: "Tomato"
        val plantedDate = myCrop?.plantedDate ?: "12 Jul 2026"

        binding?.tvCropName?.text = cropName
        binding?.tvPlantedDate?.text = "Planted on $plantedDate"
        binding?.ivCropThumb?.setImageResource(myCrop?.imageRes ?: R.drawable.bg_1)

        binding?.btnViewCareTips?.setOnClickListener {
            findNavController().navigate(R.id.action_cropProgressFragment_to_cropInsightsFragment)
        }

        binding?.btnSetReminder?.setOnClickListener {
            Toast.makeText(requireContext(), "Reminder set for upcoming stage!", Toast.LENGTH_SHORT).show()
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
